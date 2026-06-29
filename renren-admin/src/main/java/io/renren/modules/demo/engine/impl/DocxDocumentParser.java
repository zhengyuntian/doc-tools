package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.DocumentParser;
import io.renren.modules.demo.engine.model.*;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.List;

@Component
public class DocxDocumentParser implements DocumentParser {

    @Override
    public ParsedDocument parse(String filePath) throws Exception {
        ParsedDocument parsedDoc = new ParsedDocument();
        parsedDoc.setFilePath(filePath);
        parsedDoc.setFileName(filePath.substring(filePath.lastIndexOf("/") + 1));

        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            parsePageSetup(document, parsedDoc);
            parseParagraphs(document, parsedDoc);
            parseTables(document, parsedDoc);
            parseImages(document, parsedDoc);
            buildFullText(parsedDoc);

            parsedDoc.setPageCount(document.getProperties().getExtendedProperties().getUnderlyingProperties().getPages());
        }

        return parsedDoc;
    }

    private void parsePageSetup(XWPFDocument document, ParsedDocument parsedDoc) {
        ParsedPageSetup pageSetup = new ParsedPageSetup();

        CTSectPr sectPr = document.getDocument().getBody().getSectPr();
        if (sectPr != null) {
            if (sectPr.isSetPgSz()) {
                String paperSize = "A4";
                Object widthObj = sectPr.getPgSz().getW();
                Object heightObj = sectPr.getPgSz().getH();
                BigInteger width = widthObj instanceof BigInteger ? (BigInteger) widthObj : 
                    (widthObj != null ? new BigInteger(widthObj.toString()) : null);
                BigInteger height = heightObj instanceof BigInteger ? (BigInteger) heightObj : 
                    (heightObj != null ? new BigInteger(heightObj.toString()) : null);
                if (width != null && height != null) {
                    int wMm = (int) (width.doubleValue() / 1440 * 25.4);
                    int hMm = (int) (height.doubleValue() / 1440 * 25.4);
                    if (wMm == 210 && hMm == 297) paperSize = "A4";
                    else if (wMm == 216 && hMm == 279) paperSize = "Letter";
                    else if (wMm == 297 && hMm == 420) paperSize = "A3";
                    else paperSize = wMm + "x" + hMm + "mm";
                }
                pageSetup.setPaperSize(paperSize);
            }

            if (sectPr.isSetPgMar()) {
                pageSetup.setMarginTop(convertTwipToCm(sectPr.getPgMar().getTop()));
                pageSetup.setMarginBottom(convertTwipToCm(sectPr.getPgMar().getBottom()));
                pageSetup.setMarginLeft(convertTwipToCm(sectPr.getPgMar().getLeft()));
                pageSetup.setMarginRight(convertTwipToCm(sectPr.getPgMar().getRight()));
            }
        }

        pageSetup.setHasHeader(!document.getHeaderList().isEmpty());
        pageSetup.setHasFooter(!document.getFooterList().isEmpty());

        parsedDoc.setPageSetup(pageSetup);
    }

    private void parseParagraphs(XWPFDocument document, ParsedDocument parsedDoc) {
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        int pageNo = 1;

        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFParagraph paragraph = paragraphs.get(i);
            ParsedParagraph parsedPara = new ParsedParagraph();
            parsedPara.setIndex(i);
            parsedPara.setPageNo(pageNo);
            parsedPara.setText(paragraph.getText());

            CTPPr ctPr = paragraph.getCTPPr();
            if (ctPr != null) {
                if (ctPr.isSetJc()) {
                    parsedPara.setAlignment(ctPr.getJc().getVal().toString());
                }
                if (ctPr.isSetSpacing()) {
                    parsedPara.setLineSpacing(ctPr.getSpacing().toString());
                }
                if (ctPr.isSetInd()) {
                    if (ctPr.getInd().isSetFirstLine()) {
                        parsedPara.setFirstLineIndent(convertTwipToCm(ctPr.getInd().getFirstLine()));
                    }
                    if (ctPr.getInd().isSetLeft()) {
                        parsedPara.setLeftIndent(convertTwipToCm(ctPr.getInd().getLeft()));
                    }
                    if (ctPr.getInd().isSetRight()) {
                        parsedPara.setRightIndent(convertTwipToCm(ctPr.getInd().getRight()));
                    }
                }
            }

            int offset = 0;
            for (XWPFRun run : paragraph.getRuns()) {
                ParsedRun parsedRun = new ParsedRun();
                parsedRun.setText(run.getText(0));
                parsedRun.setStartOffset(offset);
                parsedRun.setEndOffset(offset + (run.getText(0) != null ? run.getText(0).length() : 0));
                offset = parsedRun.getEndOffset();

                parsedRun.setFontFamily(run.getFontFamily());
                if (run.getFontSizeAsDouble() != null) {
                    parsedRun.setFontSize(run.getFontSizeAsDouble() / 2);
                }
                parsedRun.setBold(Boolean.TRUE.equals(run.isBold()));
                parsedRun.setItalic(Boolean.TRUE.equals(run.isItalic()));
                parsedRun.setColor(run.getColor());

                parsedPara.getRuns().add(parsedRun);
            }

            if (!parsedPara.getRuns().isEmpty()) {
                ParsedRun firstRun = parsedPara.getRuns().get(0);
                parsedPara.setFontFamily(firstRun.getFontFamily());
                parsedPara.setFontSize(firstRun.getFontSize());
                parsedPara.setBold(firstRun.getBold());
                parsedPara.setItalic(firstRun.getItalic());
                parsedPara.setColor(firstRun.getColor());
            }

            if (!paragraph.getText().isEmpty()) {
                parsedDoc.getParagraphs().add(parsedPara);
            }

            if (paragraph.getText().contains("\f")) {
                pageNo++;
            }
        }
    }

    private void parseTables(XWPFDocument document, ParsedDocument parsedDoc) {
        List<XWPFTable> tables = document.getTables();

        for (int i = 0; i < tables.size(); i++) {
            XWPFTable table = tables.get(i);
            ParsedTable parsedTable = new ParsedTable();
            parsedTable.setIndex(i);
            parsedTable.setRowCount(table.getNumberOfRows());

            boolean hasBorder = false;

            for (XWPFTableRow row : table.getRows()) {
                if (parsedTable.getColCount() == 0) {
                    parsedTable.setColCount(row.getTableCells().size());
                }

                List<String> cellTexts = new java.util.ArrayList<>();
                for (XWPFTableCell cell : row.getTableCells()) {
                    cellTexts.add(cell.getText());
                    if (!hasBorder) {
                        hasBorder = cell.getCTTc().getTcPr() != null &&
                                cell.getCTTc().getTcPr().isSetTcBorders();
                    }
                }
                parsedTable.getCells().add(cellTexts);
            }

            parsedTable.setHasBorder(hasBorder);
            parsedDoc.getTables().add(parsedTable);
        }
    }

    private void parseImages(XWPFDocument document, ParsedDocument parsedDoc) {
        List<XWPFPictureData> pictures = document.getAllPictures();

        for (int i = 0; i < pictures.size(); i++) {
            XWPFPictureData picture = pictures.get(i);
            ParsedImage parsedImage = new ParsedImage();
            parsedImage.setIndex(i);
            parsedImage.setFileName(picture.getFileName());
            parsedImage.setContent(picture.getData());
            parsedImage.setContentType(picture.getPackagePart().getContentType());

            parsedDoc.getImages().add(parsedImage);
        }
    }

    private void buildFullText(ParsedDocument parsedDoc) {
        StringBuilder sb = new StringBuilder();
        for (ParsedParagraph para : parsedDoc.getParagraphs()) {
            sb.append(para.getText()).append("\n");
        }
        parsedDoc.setFullText(sb.toString());
    }

    private Double convertTwipToCm(Object twips) {
        if (twips == null) return null;
        BigInteger bigInt;
        if (twips instanceof BigInteger) {
            bigInt = (BigInteger) twips;
        } else {
            bigInt = new BigInteger(twips.toString());
        }
        return Math.round(bigInt.doubleValue() / 1440.0 * 2.54 * 100.0) / 100.0;
    }

    @Override
    public boolean supports(String fileType) {
        return "docx".equalsIgnoreCase(fileType) || "1".equals(fileType);
    }
}
