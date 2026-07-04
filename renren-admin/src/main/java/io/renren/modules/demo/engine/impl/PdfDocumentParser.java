package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.DocumentParser;
import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.engine.model.ParsedImage;
import io.renren.modules.demo.engine.model.ParsedParagraph;
import io.renren.modules.demo.engine.model.ParsedPageSetup;
import io.renren.modules.demo.engine.model.ParsedTable;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

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
            parseTables(document, parsedDoc);
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

    private void parseTables(PDDocument document, ParsedDocument parsedDoc) {
        logger.debug("[PDF表格解析] 开始解析表格...");
        List<ParsedTable> tables = new ArrayList<>();
        
        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            int pageNo = pageIndex + 1;
            
            List<List<TextPosition>> allLines = extractAllTextPositions(document, pageNo);
            
            if (allLines == null || allLines.isEmpty()) {
                continue;
            }
            
            List<ParsedTable> pageTables = detectTablesByTextClustering(allLines, pageNo);
            tables.addAll(pageTables);
            logger.debug("[PDF表格解析] 页面 {} 通过文本聚类识别到 {} 个表格", pageNo, pageTables.size());
        }
        
        parsedDoc.getTables().addAll(tables);
        logger.debug("[PDF表格解析] 解析完成，共识别到 {} 个表格", tables.size());
    }

    private List<List<TextPosition>> extractAllTextPositions(PDDocument document, int pageNo) {
        try {
            FormatTextStripper stripper = new FormatTextStripper();
            stripper.setStartPage(pageNo);
            stripper.setEndPage(pageNo);
            stripper.getText(document);
            return stripper.getLines();
        } catch (Exception e) {
            logger.debug("[PDF表格解析] 提取文本位置失败: {}", e.getMessage());
            return null;
        }
    }

    private List<ParsedTable> detectTablesByTextClustering(List<List<TextPosition>> allLines, int pageNo) {
        List<ParsedTable> tables = new ArrayList<>();
        
        List<TextPosition> allTextPositions = new ArrayList<>();
        for (List<TextPosition> line : allLines) {
            allTextPositions.addAll(line);
        }
        
        if (allTextPositions.size() < 6) {
            return tables;
        }
        
        List<List<TextPosition>> tableLines = new ArrayList<>();
        
        for (List<TextPosition> line : allLines) {
            if (line.size() < 2) continue;
            
            int largeGaps = 0;
            float prevX = line.get(0).getXDirAdj();
            for (int j = 1; j < line.size(); j++) {
                float gap = line.get(j).getXDirAdj() - (prevX + line.get(j-1).getWidthDirAdj());
                if (gap > 30) {
                    largeGaps++;
                }
                prevX = line.get(j).getXDirAdj();
            }
            
            if (largeGaps >= 1) {
                tableLines.add(line);
            }
        }
        
        if (tableLines.size() < 2) {
            return tables;
        }
        
        Map<Float, Integer> colStartCounts = new HashMap<>();
        for (List<TextPosition> line : tableLines) {
            float prevX = line.get(0).getXDirAdj();
            float colStart = Math.round(prevX);
            colStartCounts.put(colStart, colStartCounts.getOrDefault(colStart, 0) + 1);
            
            for (int j = 1; j < line.size(); j++) {
                float gap = line.get(j).getXDirAdj() - (prevX + line.get(j-1).getWidthDirAdj());
                if (gap > 30) {
                    colStart = Math.round(line.get(j).getXDirAdj());
                    colStartCounts.put(colStart, colStartCounts.getOrDefault(colStart, 0) + 1);
                }
                prevX = line.get(j).getXDirAdj();
            }
        }
        
        List<Float> colBoundaries = new ArrayList<>();
        int minOccurrence = tableLines.size() / 2;
        if (minOccurrence < 1) minOccurrence = 1;
        
        for (Map.Entry<Float, Integer> entry : colStartCounts.entrySet()) {
            if (entry.getValue() >= minOccurrence) {
                colBoundaries.add(entry.getKey());
            }
        }
        
        Collections.sort(colBoundaries);
        
        if (colBoundaries.size() < 2) {
            return tables;
        }
        
        List<List<Float>> colGroups = new ArrayList<>();
        for (int i = 0; i < colBoundaries.size(); i++) {
            List<Float> colGroup = new ArrayList<>();
            float left = colBoundaries.get(i);
            float right = (i < colBoundaries.size() - 1) ? colBoundaries.get(i + 1) - 1 : Float.MAX_VALUE;
            colGroup.add(left);
            colGroup.add(right);
            colGroups.add(colGroup);
        }
        
        TreeSet<Float> tableYPositions = new TreeSet<>();
        for (List<TextPosition> line : tableLines) {
            for (TextPosition tp : line) {
                tableYPositions.add(tp.getYDirAdj());
            }
        }
        
        List<Float> sortedY = new ArrayList<>(tableYPositions);
        List<List<Float>> rowGroups = new ArrayList<>();
        List<Float> currentGroup = new ArrayList<>();
        float prevY = -1;
        
        for (Float y : sortedY) {
            if (prevY < 0 || Math.abs(y - prevY) < 40) {
                currentGroup.add(y);
            } else {
                if (currentGroup.size() >= 1) {
                    rowGroups.add(currentGroup);
                }
                currentGroup = new ArrayList<>();
                currentGroup.add(y);
            }
            prevY = y;
        }
        if (currentGroup.size() >= 1) {
            rowGroups.add(currentGroup);
        }
        
        if (rowGroups.size() < 2) {
            return tables;
        }
        
        List<List<TableCell>> tableCells = new ArrayList<>();
        
        for (List<Float> rowY : rowGroups) {
            List<TableCell> rowCells = new ArrayList<>();
            float rowTop = Collections.max(rowY);
            float rowBottom = Collections.min(rowY);
            
            for (List<Float> colX : colGroups) {
                float colLeft = colX.get(0);
                float colRight = colX.get(1);
                
                TableCell cell = new TableCell(colLeft, rowBottom, colRight, rowTop);
                
                for (TextPosition tp : allTextPositions) {
                    float tpX = tp.getXDirAdj();
                    float tpY = tp.getYDirAdj();
                    float tpWidth = tp.getWidthDirAdj();
                    float tpHeight = tp.getHeightDir();
                    
                    if (tpX >= colLeft - 10 && tpX + tpWidth <= colRight + 10 && 
                        tpY >= rowBottom - 10 && tpY - tpHeight <= rowTop + 10) {
                        cell.addText(tp);
                    }
                }
                
                rowCells.add(cell);
            }
            tableCells.add(rowCells);
        }
        
        ParsedTable table = createParsedTable(tableCells, pageNo, tables.size());
        tables.add(table);
        
        return tables;
    }

    private ParsedTable createParsedTable(List<List<TableCell>> tableCells, int pageNo, int tableIndex) {
        ParsedTable parsedTable = new ParsedTable();
        parsedTable.setIndex(tableIndex);
        parsedTable.setPageNo(pageNo);
        parsedTable.setRowCount(tableCells.size());
        
        if (!tableCells.isEmpty()) {
            parsedTable.setColCount(tableCells.get(0).size());
        }
        
        List<List<String>> cellTexts = new ArrayList<>();
        String fontFamily = null;
        Double fontSize = null;
        Boolean bold = false;
        Boolean italic = false;
        String color = null;
        Boolean underline = false;
        Boolean strikethrough = false;
        
        for (List<TableCell> row : tableCells) {
            List<String> rowTexts = new ArrayList<>();
            for (TableCell cell : row) {
                String cellText = cell.getText();
                rowTexts.add(cellText);
                
                if (fontFamily == null && !cellText.isEmpty()) {
                    fontFamily = cell.getFontFamily();
                    fontSize = cell.getFontSize();
                    bold = cell.isBold();
                    italic = cell.isItalic();
                    color = cell.getColor();
                    underline = cell.isUnderline();
                    strikethrough = cell.isStrikethrough();
                }
            }
            cellTexts.add(rowTexts);
        }
        
        parsedTable.setCells(cellTexts);
        parsedTable.setFontFamily(fontFamily);
        parsedTable.setFontSize(fontSize);
        parsedTable.setBold(bold);
        parsedTable.setItalic(italic);
        parsedTable.setColor(color != null ? color : "black");
        parsedTable.setUnderline(underline);
        parsedTable.setStrikethrough(strikethrough);
        
        parsedTable.setVerticalAlign("center");
        parsedTable.setHorizontalAlign("left");
        parsedTable.setAlignment("center");
        
        return parsedTable;
    }

    private static class TableCell {
        float left, bottom, right, top;
        List<TextPosition> textPositions = new ArrayList<>();
        
        TableCell(float left, float bottom, float right, float top) {
            this.left = left;
            this.bottom = bottom;
            this.right = right;
            this.top = top;
        }
        
        void addText(TextPosition tp) {
            textPositions.add(tp);
        }
        
        String getText() {
            Collections.sort(textPositions, Comparator.comparingDouble(tp -> tp.getXDirAdj()));
            StringBuilder sb = new StringBuilder();
            for (TextPosition tp : textPositions) {
                sb.append(tp.getUnicode());
            }
            return sb.toString().trim();
        }
        
        String getFontFamily() {
            if (!textPositions.isEmpty()) {
                return textPositions.get(0).getFont().getName();
            }
            return null;
        }
        
        Double getFontSize() {
            if (!textPositions.isEmpty()) {
                return (double) textPositions.get(0).getFontSizeInPt();
            }
            return null;
        }
        
        boolean isBold() {
            for (TextPosition tp : textPositions) {
                String fontName = tp.getFont().getName().toLowerCase();
                if (fontName.contains("bold") || fontName.contains("black") || fontName.contains("heavy")) {
                    return true;
                }
            }
            return false;
        }
        
        boolean isItalic() {
            for (TextPosition tp : textPositions) {
                String fontName = tp.getFont().getName().toLowerCase();
                if (fontName.contains("italic") || fontName.contains("oblique")) {
                    return true;
                }
            }
            return false;
        }
        
        String getColor() {
            return "black";
        }
        
        boolean isUnderline() {
            return false;
        }
        
        boolean isStrikethrough() {
            return false;
        }
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
    }
    
    private void createParsedImage(PDImageXObject image, int pageNo, float pageWidth, 
                                   int[] imageIndexRef, ParsedDocument parsedDoc) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (width < 50 || height < 50) {
            logger.debug("[PDF解析] 跳过小图片: {}x{}px", width, height);
            return;
        }
        
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
        logger.debug("[PDF解析] 提取图片: {}, 尺寸: {}x{}px", parsedImage.getFileName(), width, height);
        imageIndexRef[0]++;
    }
    
    private void extractImagesFromResources(PDResources resources, int pageNo, float pageWidth, 
                                            int[] imageIndexRef, ParsedDocument parsedDoc) {
        extractImagesFromResources(resources, pageNo, pageWidth, imageIndexRef, parsedDoc, 0);
    }
    
    private void extractImagesFromResources(PDResources resources, int pageNo, float pageWidth, 
                                            int[] imageIndexRef, ParsedDocument parsedDoc, int depth) {
        if (resources == null) return;
        if (depth > 1) return;
        
        Iterable<COSName> xobjectNames = resources.getXObjectNames();
        if (xobjectNames != null) {
            for (COSName name : xobjectNames) {
                try {
                    PDXObject xobject = resources.getXObject(name);
                    if (xobject instanceof PDImageXObject) {
                        PDImageXObject image = (PDImageXObject) xobject;
                        
                        int width = image.getWidth();
                        int height = image.getHeight();
                        
                        if (width < 50 || height < 50) {
                            logger.debug("[PDF解析] 跳过小图片: {}x{}px", width, height);
                            continue;
                        }
                        
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
                        logger.debug("[PDF解析] 提取图片: {}, 尺寸: {}x{}px, 对齐方式: {}", 
                            parsedImage.getFileName(), width, height, alignment);
                        imageIndexRef[0]++;
                    } else if (xobject instanceof PDFormXObject) {
                        PDFormXObject form = (PDFormXObject) xobject;
                        extractImagesFromResources(form.getResources(), pageNo, pageWidth, imageIndexRef, parsedDoc, depth + 1);
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
