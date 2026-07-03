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
            String tableFontFamily = null;
            Double tableFontSize = null;
            String tableColor = "black";
            Boolean tableBold = false;
            Boolean tableItalic = false;
            Boolean tableUnderline = false;
            Boolean tableStrikethrough = false;
            String tableVerticalAlign = null;
            String tableHorizontalAlign = null;
            String tableAlignment = null;
            boolean hasFontInfo = false;

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
                    
                    // 解析表格字体信息（从第一个非空单元格获取）
                    if (!hasFontInfo && !cell.getText().isEmpty()) {
                        List<XWPFParagraph> cellParagraphs = cell.getParagraphs();
                        if (!cellParagraphs.isEmpty()) {
                            List<XWPFRun> runs = cellParagraphs.get(0).getRuns();
                            if (!runs.isEmpty()) {
                                XWPFRun run = runs.get(0);
                                tableFontFamily = run.getFontFamily();
                                if (run.getFontSizeAsDouble() != null) {
                                    tableFontSize = run.getFontSizeAsDouble() / 2;
                                }
                                tableColor = run.getColor();
                                if (tableColor == null) tableColor = "black";
                                tableBold = Boolean.TRUE.equals(run.isBold());
                                tableItalic = Boolean.TRUE.equals(run.isItalic());
                                tableUnderline = Boolean.TRUE.equals(run.getUnderline() != null);
                                tableStrikethrough = Boolean.TRUE.equals(run.isStrikeThrough());
                                hasFontInfo = true;
                            }
                        }
                    }
                    
                    // 解析表格对齐信息（从第一个非空单元格获取）
                    if (tableVerticalAlign == null) {
                        if (cell.getVerticalAlignment() != null) {
                            tableVerticalAlign = cell.getVerticalAlignment().name().toLowerCase();
                        } else {
                            tableVerticalAlign = "center";
                        }
                    }
                    if (tableHorizontalAlign == null) {
                        List<XWPFParagraph> cellParagraphs = cell.getParagraphs();
                        if (!cellParagraphs.isEmpty()) {
                            XWPFParagraph para = cellParagraphs.get(0);
                            if (para.getAlignment() != null) {
                                tableHorizontalAlign = para.getAlignment().name().toLowerCase();
                            } else {
                                tableHorizontalAlign = "left";
                            }
                        }
                    }
                }
                parsedTable.getCells().add(cellTexts);
            }

            parsedTable.setHasBorder(hasBorder);
            parsedTable.setFontFamily(tableFontFamily);
            parsedTable.setFontSize(tableFontSize);
            parsedTable.setColor(tableColor);
            parsedTable.setBold(tableBold);
            parsedTable.setItalic(tableItalic);
            parsedTable.setUnderline(tableUnderline);
            parsedTable.setStrikethrough(tableStrikethrough);
            if (table.getCTTbl().getTblPr() != null && table.getCTTbl().getTblPr().isSetJc()) {
                tableAlignment = table.getCTTbl().getTblPr().getJc().getVal().toString();
            } else {
                tableAlignment = "center";
            }
            
            parsedTable.setVerticalAlign(tableVerticalAlign);
            parsedTable.setHorizontalAlign(tableHorizontalAlign);
            parsedTable.setAlignment(tableAlignment);
            parsedDoc.getTables().add(parsedTable);
        }
    }

    private void parseImages(XWPFDocument document, ParsedDocument parsedDoc) {
        List<XWPFPictureData> pictures = document.getAllPictures();
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        for (int i = 0; i < pictures.size(); i++) {
            XWPFPictureData picture = pictures.get(i);
            ParsedImage parsedImage = new ParsedImage();
            parsedImage.setIndex(i);
            parsedImage.setFileName(picture.getFileName());
            parsedImage.setContent(picture.getData());
            parsedImage.setContentType(picture.getPackagePart().getContentType());

            String alignment = findImageAlignment(picture, paragraphs);
            parsedImage.setAlignment(alignment);

            parsedDoc.getImages().add(parsedImage);
        }
    }

    private String findImageAlignment(XWPFPictureData picture, List<XWPFParagraph> paragraphs) {
        for (XWPFParagraph paragraph : paragraphs) {
            for (XWPFRun run : paragraph.getRuns()) {
                for (XWPFPicture pic : run.getEmbeddedPictures()) {
                    if (pic.getPictureData().equals(picture)) {
                        if (paragraph.getAlignment() != null) {
                            return paragraph.getAlignment().name().toLowerCase();
                        }
                        return "left";
                    }
                }
            }
        }
        return "left";
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
