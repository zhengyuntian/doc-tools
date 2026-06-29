package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.DocumentParser;
import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.engine.model.ParsedParagraph;
import io.renren.modules.demo.engine.model.ParsedPageSetup;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfDocumentParser implements DocumentParser {

    @Override
    public ParsedDocument parse(String filePath) throws Exception {
        ParsedDocument parsedDoc = new ParsedDocument();
        parsedDoc.setFilePath(filePath);
        parsedDoc.setFileName(filePath.substring(filePath.lastIndexOf("/") + 1));

        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            parsedDoc.setPageCount(document.getNumberOfPages());
            System.out.println("[PDF解析] 开始解析，页数: " + document.getNumberOfPages());
            
            parsePageSetup(document, parsedDoc);
            parseParagraphs(document, parsedDoc);
            buildFullText(parsedDoc);
            
            System.out.println("[PDF解析] 解析完成，段落数: " + parsedDoc.getParagraphs().size() 
                + ", 全文长度: " + (parsedDoc.getFullText() != null ? parsedDoc.getFullText().length() : 0));
            
            if (parsedDoc.getParagraphs().size() > 0) {
                System.out.println("[PDF解析] 第一个段落内容: " + parsedDoc.getParagraphs().get(0).getText());
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
        }

        parsedDoc.setPageSetup(pageSetup);
    }

    private void parseParagraphs(PDDocument document, ParsedDocument parsedDoc) throws Exception {
        List<ParsedParagraph> paragraphs = new ArrayList<>();
        
        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            PDPage page = document.getPages().get(pageIndex);
            
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(pageIndex + 1);
            stripper.setEndPage(pageIndex + 1);
            
            String pageText = stripper.getText(document);
            
            String[] lines = pageText.split("\n");
            for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
                String line = lines[lineIndex].trim();
                if (line.isEmpty()) continue;
                
                ParsedParagraph para = new ParsedParagraph();
                para.setIndex(paragraphs.size());
                para.setPageNo(pageIndex + 1);
                para.setText(line);
                
                paragraphs.add(para);
            }
        }
        
        parsedDoc.getParagraphs().addAll(paragraphs);
    }

    private void buildFullText(ParsedDocument parsedDoc) {
        StringBuilder sb = new StringBuilder();
        for (ParsedParagraph para : parsedDoc.getParagraphs()) {
            sb.append(para.getText()).append("\n");
        }
        parsedDoc.setFullText(sb.toString());
    }

    @Override
    public boolean supports(String fileType) {
        return "pdf".equalsIgnoreCase(fileType) || "2".equals(fileType);
    }
}
