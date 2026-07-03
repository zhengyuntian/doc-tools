package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.DocumentParser;
import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.engine.model.ParsedImage;
import io.renren.modules.demo.engine.model.ParsedParagraph;
import io.renren.modules.demo.engine.model.ParsedPageSetup;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PdfDocumentParser implements DocumentParser {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfDocumentParser.class);

    @Override
    public ParsedDocument parse(String filePath) throws Exception {
        ParsedDocument parsedDoc = new ParsedDocument();
        parsedDoc.setFilePath(filePath);
        parsedDoc.setFileName(filePath.substring(filePath.lastIndexOf("/") + 1));

        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            parsedDoc.setPageCount(document.getNumberOfPages());
            logger.debug("[PDF解析] 开始解析，页数: {}", document.getNumberOfPages());
            
            parsePageSetup(document, parsedDoc);
            parseParagraphs(document, parsedDoc);
            parseImages(document, parsedDoc);
            buildFullText(parsedDoc);
            identifyFooterPageNumbers(document, parsedDoc);
            determineStartBodyPage(parsedDoc);
            
            logger.debug("[PDF解析] 解析完成，段落数: {}, 全文长度: {}, 正文开始页: {}", 
                parsedDoc.getParagraphs().size(), 
                parsedDoc.getFullText() != null ? parsedDoc.getFullText().length() : 0,
                parsedDoc.getStartBodyPage());
            
            if (parsedDoc.getParagraphs().size() > 0) {
                logger.debug("[PDF解析] 第一个段落内容: {}", parsedDoc.getParagraphs().get(0).getText());
            }
        }

        return parsedDoc;
    }

    private void parsePageSetup(PDDocument document, ParsedDocument parsedDoc) {
        ParsedPageSetup pageSetup = new ParsedPageSetup();

        if (document.getNumberOfPages() > 0) {
            PDPage firstPage = document.getPages().get(0);
            PDRectangle mediaBox = firstPage.getMediaBox();

            float widthMm = mediaBox.getWidth() * 25.4f / 72f;
            float heightMm = mediaBox.getHeight() * 25.4f / 72f;

            String paperSize = "A4";
            if (Math.abs(widthMm - 210) < 1 && Math.abs(heightMm - 297) < 1) paperSize = "A4";
            else if (Math.abs(widthMm - 216) < 1 && Math.abs(heightMm - 279) < 1) paperSize = "Letter";
            else if (Math.abs(widthMm - 297) < 1 && Math.abs(heightMm - 420) < 1) paperSize = "A3";
            else paperSize = String.format("%.0fx%.0fmm", widthMm, heightMm);

            pageSetup.setPaperSize(paperSize);

            PDRectangle cropBox = firstPage.getCropBox();
            if (cropBox != null && mediaBox != null) {
                pageSetup.setMarginLeft(Math.round((cropBox.getLowerLeftX() - mediaBox.getLowerLeftX()) * 25.4 / 72 * 100) / 100.0);
                pageSetup.setMarginRight(Math.round((mediaBox.getUpperRightX() - cropBox.getUpperRightX()) * 25.4 / 72 * 100) / 100.0);
                pageSetup.setMarginBottom(Math.round((cropBox.getLowerLeftY() - mediaBox.getLowerLeftY()) * 25.4 / 72 * 100) / 100.0);
                pageSetup.setMarginTop(Math.round((mediaBox.getUpperRightY() - cropBox.getUpperRightY()) * 25.4 / 72 * 100) / 100.0);
            }

            checkHeaderFooter(document, pageSetup);
        }

        parsedDoc.setPageSetup(pageSetup);
    }

    private void checkHeaderFooter(PDDocument document, ParsedPageSetup pageSetup) {
        boolean hasHeader = false;
        boolean hasFooter = false;

        try {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            for (int pageIndex = 0; pageIndex < Math.min(document.getNumberOfPages(), 3); pageIndex++) {
                PDPage page = document.getPages().get(pageIndex);
                PDRectangle mediaBox = page.getMediaBox();
                float pageHeight = mediaBox.getHeight();

                stripper.setStartPage(pageIndex + 1);
                stripper.setEndPage(pageIndex + 1);
                String pageText = stripper.getText(document);

                String[] lines = pageText.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String lineLower = line.toLowerCase();
                    if (lineLower.contains("页眉") || lineLower.contains("header") ||
                        lineLower.contains("页脚") || lineLower.contains("footer") ||
                        lineLower.contains("页码") || lineLower.contains("page") ||
                        lineLower.matches(".*\\d{1,3}\\s*[-–—]\\s*\\d{1,3}.*")) {
                        hasHeader = true;
                        hasFooter = true;
                        break;
                    }
                }
                if (hasHeader && hasFooter) break;
            }
        } catch (Exception e) {
            logger.error("[PDF解析] 页眉页脚检测失败: {}", e.getMessage());
        }

        pageSetup.setHasHeader(hasHeader);
        pageSetup.setHasFooter(hasFooter);
    }

    private void parseParagraphs(PDDocument document, ParsedDocument parsedDoc) throws Exception {
        List<ParsedParagraph> paragraphs = new ArrayList<>();
        
        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            PDPage page = document.getPages().get(pageIndex);
            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth = mediaBox.getWidth();
            
            FormatTextStripper stripper = new FormatTextStripper();
            stripper.setStartPage(pageIndex + 1);
            stripper.setEndPage(pageIndex + 1);
            stripper.getText(document);
            
            List<List<TextPosition>> lines = stripper.getLines();
            
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                List<TextPosition> linePositions = lines.get(lineIndex);
                if (linePositions.isEmpty()) continue;
                
                StringBuilder lineText = new StringBuilder();
                String fontFamily = null;
                Double fontSize = null;
                Boolean bold = false;
                Boolean italic = false;
                String color = null;
                float minX = Float.MAX_VALUE;
                float maxX = Float.MIN_VALUE;
                float baseY = 0;
                
                for (TextPosition tp : linePositions) {
                    lineText.append(tp.getUnicode());
                    
                    if (fontFamily == null) {
                        fontFamily = tp.getFont().getName();
                    }
                    if (fontSize == null) {
                        fontSize = (double) tp.getFontSizeInPt();
                    }
                    
                    String fontName = tp.getFont().getName().toLowerCase();
                    if (fontName.contains("bold") || fontName.contains("black") || fontName.contains("heavy")) {
                        bold = true;
                    }
                    if (fontName.contains("italic") || fontName.contains("oblique")) {
                        italic = true;
                    }
                    
                    minX = Math.min(minX, tp.getXDirAdj());
                    maxX = Math.max(maxX, tp.getXDirAdj() + tp.getWidthDirAdj());
                    baseY = tp.getYDirAdj();
                }
                
                String line = lineText.toString().trim();
                if (line.isEmpty()) continue;
                
                ParsedParagraph para = new ParsedParagraph();
                para.setIndex(paragraphs.size());
                para.setPageNo(pageIndex + 1);
                para.setText(line);
                para.setFontFamily(fontFamily);
                para.setFontSize(fontSize);
                para.setBold(bold);
                para.setItalic(italic);
                para.setColor(color != null ? color : "black");
                
                float lineWidth = maxX - minX;
                float leftMargin = minX;
                float rightMargin = pageWidth - maxX;
                
                if (leftMargin < rightMargin && leftMargin < pageWidth * 0.1) {
                    para.setAlignment("left");
                } else if (rightMargin < leftMargin && rightMargin < pageWidth * 0.1) {
                    para.setAlignment("right");
                } else {
                    float centerDiff = Math.abs(leftMargin - rightMargin);
                    if (centerDiff < pageWidth * 0.1) {
                        para.setAlignment("center");
                    } else {
                        para.setAlignment("left");
                    }
                }
                
                if (fontSize != null && fontSize >= 16) {
                    para.setIsTitle(true);
                }
                
                paragraphs.add(para);
            }
            
            logger.debug("[PDF解析] 页面 {} 解析完成，提取段落数: {}", pageIndex + 1, lines.size());
        }
        
        parsedDoc.getParagraphs().addAll(paragraphs);
    }
    
    private static class FormatTextStripper extends PDFTextStripper {
        private final List<List<TextPosition>> lines = new ArrayList<>();
        private List<TextPosition> currentLine = new ArrayList<>();
        private float lastY = -1;
        
        public FormatTextStripper() throws IOException {
            super();
            setSortByPosition(true);
        }
        
        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            for (TextPosition tp : textPositions) {
                float currentY = tp.getYDirAdj();
                if (lastY > 0 && Math.abs(currentY - lastY) > tp.getFontSizeInPt() * 0.5) {
                    if (!currentLine.isEmpty()) {
                        lines.add(new ArrayList<>(currentLine));
                        currentLine.clear();
                    }
                }
                currentLine.add(tp);
                lastY = currentY;
            }
        }
        
        @Override
        protected void endPage(PDPage page) throws IOException {
            if (!currentLine.isEmpty()) {
                lines.add(new ArrayList<>(currentLine));
                currentLine.clear();
            }
            lastY = -1;
            super.endPage(page);
        }
        
        public List<List<TextPosition>> getLines() {
            return lines;
        }
    }

    private void buildFullText(ParsedDocument parsedDoc) {
        StringBuilder sb = new StringBuilder();
        for (ParsedParagraph para : parsedDoc.getParagraphs()) {
            sb.append(para.getText()).append("\n");
        }
        parsedDoc.setFullText(sb.toString());
    }
    
    private void identifyFooterPageNumbers(PDDocument document, ParsedDocument parsedDoc) {
        try {
            int totalPages = document.getNumberOfPages();
            float footerAreaTop = 50;
            
            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDPage page = document.getPages().get(pageIndex);
                final int finalPageIndex = pageIndex + 1;
                final Integer[] detectedPageNum = {0};
                
                PDFTextStripper stripper = new PDFTextStripper() {
                    @Override
                    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
                        for (TextPosition tp : textPositions) {
                            if (tp.getY() <= footerAreaTop) {
                                String text = tp.getUnicode().trim();
                                String numStr = text.replaceAll("[^0-9]", "");
                                if (!numStr.isEmpty() && numStr.length() <= 3) {
                                    try {
                                        int pageNum = Integer.parseInt(numStr);
                                        if (pageNum > 0 && pageNum <= totalPages) {
                                            detectedPageNum[0] = pageNum;
                                        }
                                    } catch (NumberFormatException e) {
                                    }
                                }
                            }
                        }
                    }
                };
                
                stripper.setStartPage(pageIndex + 1);
                stripper.setEndPage(pageIndex + 1);
                stripper.getText(document);
                
                if (detectedPageNum[0] > 0) {
                    parsedDoc.getPageNumberMap().put(finalPageIndex, detectedPageNum[0]);
                    logger.debug("[PDF解析] 页面 {} 页脚页码: {}", finalPageIndex, detectedPageNum[0]);
                }
            }
        } catch (Exception e) {
            logger.error("[PDF解析] 页脚页码识别失败: {}", e.getMessage());
        }
    }
    
    private void determineStartBodyPage(ParsedDocument parsedDoc) {
        int startBodyPage = 1;
        java.util.Set<Integer> catalogPages = new java.util.HashSet<>();
        
        for (ParsedParagraph para : parsedDoc.getParagraphs()) {
            String text = para.getText();
            int pageNo = para.getPageNo();
            
            if (text.contains("目录")) {
                catalogPages.add(pageNo);
            }
            
            if (text.matches(".*[.。·]{6,}.*\\d+.*")) {
                catalogPages.add(pageNo);
            }
            
            if (text.matches(".*第[一二三四五六七八九十]+章.*") && pageNo <= 5) {
                catalogPages.add(pageNo);
            }
            
            if (text.matches(".*[一二三四五六七八九十]+、.*") && text.length() < 50 && pageNo <= 5) {
                catalogPages.add(pageNo);
            }
        }
        
        if (!catalogPages.isEmpty()) {
            int maxCatalogPage = catalogPages.stream().mapToInt(Integer::intValue).max().orElse(1);
            startBodyPage = maxCatalogPage + 1;
            logger.debug("[PDF解析] 检测到目录页: {}, 正文起始页: {}", catalogPages, startBodyPage);
        } else {
            startBodyPage = 1;
            logger.debug("[PDF解析] 未检测到目录，全部内容视为正文，起始页: {}", startBodyPage);
        }
        
        if (startBodyPage > parsedDoc.getPageCount()) {
            startBodyPage = 2;
        }
        
        parsedDoc.setStartBodyPage(startBodyPage);
        logger.debug("[PDF解析] 正文开始页确定为: {}", startBodyPage);
    }
    
    private boolean verifyConsecutivePageNumbers(ParsedDocument parsedDoc, int startPage) {
        int expectedPage = 1;
        for (int i = startPage; i < startPage + 5 && i <= parsedDoc.getPageCount(); i++) {
            Integer footerPageNo = parsedDoc.getFooterPageNo(i);
            if (footerPageNo != null && footerPageNo == expectedPage) {
                expectedPage++;
            } else if (footerPageNo != null) {
                return false;
            }
        }
        return expectedPage > 3;
    }

    private void parseImages(PDDocument document, ParsedDocument parsedDoc) {
        int[] imageIndexRef = {0};
        logger.debug("[PDF解析] 开始提取图片...");
        
        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            PDPage page = document.getPages().get(pageIndex);
            PDResources resources = page.getResources();
            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth = mediaBox.getWidth();
            
            if (resources != null) {
                extractImagesFromResources(resources, pageIndex + 1, pageWidth, imageIndexRef, parsedDoc);
            }
            
            extractImagesFromAnnotations(page, pageIndex + 1, pageWidth, imageIndexRef, parsedDoc);
        }
        
        logger.debug("[PDF解析] 图片提取完成，共提取 {} 张图片", parsedDoc.getImages().size());
    }
    
    private void extractImagesFromAnnotations(PDPage page, int pageNo, float pageWidth, 
                                              int[] imageIndexRef, ParsedDocument parsedDoc) {
        try {
            List<?> annotations = page.getAnnotations();
            if (annotations == null) return;
            
            for (Object annotationObj : annotations) {
                if (!(annotationObj instanceof org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation)) {
                    continue;
                }
                
                org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation annotation = 
                    (org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation) annotationObj;
                
                org.apache.pdfbox.cos.COSDictionary dict = annotation.getCOSObject();
                if (dict == null) continue;
                
                org.apache.pdfbox.cos.COSBase appearance = dict.getDictionaryObject(
                    org.apache.pdfbox.cos.COSName.AP);
                if (appearance == null || !(appearance instanceof org.apache.pdfbox.cos.COSDictionary)) {
                    continue;
                }
                
                org.apache.pdfbox.cos.COSDictionary appearanceDict = 
                    (org.apache.pdfbox.cos.COSDictionary) appearance;
                
                org.apache.pdfbox.cos.COSBase normal = appearanceDict.getDictionaryObject(
                    org.apache.pdfbox.cos.COSName.N);
                if (normal == null) continue;
                
                if (normal instanceof org.apache.pdfbox.cos.COSStream) {
                    try {
                        PDFormXObject form = new org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject(
                            (org.apache.pdfbox.cos.COSStream) normal);
                        PDResources formResources = form.getResources();
                        if (formResources != null) {
                            extractImagesFromResources(formResources, pageNo, pageWidth, imageIndexRef, parsedDoc);
                        }
                    } catch (Exception e) {
                        logger.debug("[PDF解析] 解析注释Form XObject失败: {}", e.getMessage());
                    }
                } else if (normal instanceof org.apache.pdfbox.cos.COSDictionary) {
                    org.apache.pdfbox.cos.COSDictionary normalDict = 
                        (org.apache.pdfbox.cos.COSDictionary) normal;
                    for (org.apache.pdfbox.cos.COSName key : normalDict.keySet()) {
                        org.apache.pdfbox.cos.COSBase obj = normalDict.getDictionaryObject(key);
                        if (obj instanceof org.apache.pdfbox.cos.COSStream) {
                            try {
                                PDFormXObject form = new org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject(
                                    (org.apache.pdfbox.cos.COSStream) obj);
                                PDResources formResources = form.getResources();
                                if (formResources != null) {
                                    extractImagesFromResources(formResources, pageNo, pageWidth, imageIndexRef, parsedDoc);
                                }
                            } catch (Exception e) {
                                logger.debug("[PDF解析] 解析注释Form XObject失败: {}", e.getMessage());
                            }
                        }
                    }
                } else if (normal instanceof org.apache.pdfbox.cos.COSName) {
                    PDResources resources = page.getResources();
                    if (resources != null) {
                        try {
                            PDXObject xobject = resources.getXObject((org.apache.pdfbox.cos.COSName) normal);
                            if (xobject instanceof PDImageXObject) {
                                PDImageXObject image = (PDImageXObject) xobject;
                                createParsedImage(image, pageNo, pageWidth, imageIndexRef, parsedDoc);
                            } else if (xobject instanceof PDFormXObject) {
                                PDFormXObject form = (PDFormXObject) xobject;
                                extractImagesFromResources(form.getResources(), pageNo, pageWidth, imageIndexRef, parsedDoc);
                            }
                        } catch (IOException e) {
                            logger.debug("[PDF解析] 获取注释XObject失败: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.debug("[PDF解析] 从注释提取图片失败: {}", e.getMessage());
        }
    }
    
    private void createParsedImage(PDImageXObject image, int pageNo, float pageWidth, 
                                   int[] imageIndexRef, ParsedDocument parsedDoc) throws IOException {
        ParsedImage parsedImage = new ParsedImage();
        parsedImage.setIndex(imageIndexRef[0]);
        parsedImage.setPageNo(pageNo);
        parsedImage.setFileName("page_" + pageNo + "_image_" + (imageIndexRef[0] + 1) + ".png");
        
        BufferedImage bufferedImage = image.getImage();
        parsedImage.setWidth(bufferedImage.getWidth());
        parsedImage.setHeight(bufferedImage.getHeight());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        parsedImage.setContent(baos.toByteArray());
        
        String suffix = image.getSuffix();
        parsedImage.setContentType(getContentType(suffix));
        parsedImage.setAlignment(calculateImageAlignment(image, pageWidth));
        
        parsedDoc.getImages().add(parsedImage);
        logger.debug("[PDF解析] 提取图片: {}", parsedImage.getFileName());
        imageIndexRef[0]++;
    }
    
    private void extractImagesFromResources(PDResources resources, int pageNo, float pageWidth, 
                                            int[] imageIndexRef, ParsedDocument parsedDoc) {
        if (resources == null) return;
        
        Iterable<COSName> xobjectNames = resources.getXObjectNames();
        if (xobjectNames != null) {
            for (COSName name : xobjectNames) {
                try {
                    PDXObject xobject = resources.getXObject(name);
                    if (xobject instanceof PDImageXObject) {
                        PDImageXObject image = (PDImageXObject) xobject;
                        
                        ParsedImage parsedImage = new ParsedImage();
                        parsedImage.setIndex(imageIndexRef[0]);
                        parsedImage.setPageNo(pageNo);
                        parsedImage.setFileName("page_" + pageNo + "_image_" + (imageIndexRef[0] + 1) + ".png");
                        
                        BufferedImage bufferedImage = image.getImage();
                        parsedImage.setWidth(bufferedImage.getWidth());
                        parsedImage.setHeight(bufferedImage.getHeight());
                        
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, "png", baos);
                        parsedImage.setContent(baos.toByteArray());
                        
                        String suffix = image.getSuffix();
                        parsedImage.setContentType(getContentType(suffix));
                        
                        String alignment = calculateImageAlignment(image, pageWidth);
                        parsedImage.setAlignment(alignment);
                        
                        parsedDoc.getImages().add(parsedImage);
                        logger.debug("[PDF解析] 提取图片: {}, 对齐方式: {}", parsedImage.getFileName(), alignment);
                        imageIndexRef[0]++;
                    } else if (xobject instanceof PDFormXObject) {
                        PDFormXObject form = (PDFormXObject) xobject;
                        extractImagesFromResources(form.getResources(), pageNo, pageWidth, imageIndexRef, parsedDoc);
                    }
                } catch (IOException e) {
                    logger.error("[PDF解析] 提取图片失败: {}", e.getMessage());
                }
            }
        }
    }

    private String calculateImageAlignment(PDImageXObject image, float pageWidth) {
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        float imageAspectRatio = imageWidth / imageHeight;
        float pageAspectRatio = pageWidth / 612f;
        
        float imagePageRatio = imageWidth / pageWidth;
        
        if (imagePageRatio > 0.8) {
            return "left";
        } else if (imagePageRatio <= 0.8 && imagePageRatio > 0.3) {
            return "center";
        } else {
            return "left";
        }
    }

    private String getContentType(String suffix) {
        if (suffix == null) return "image/png";
        return switch (suffix.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            default -> "image/png";
        };
    }

    @Override
    public boolean supports(String fileType) {
        return "pdf".equalsIgnoreCase(fileType) || "2".equals(fileType);
    }
}
