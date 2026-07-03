package io.renren.modules.demo.engine.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ParsedDocument {
    private String filePath;
    private String fileName;
    private int pageCount;
    private ParsedPageSetup pageSetup;
    private List<ParsedParagraph> paragraphs = new ArrayList<>();
    private List<ParsedTable> tables = new ArrayList<>();
    private List<ParsedImage> images = new ArrayList<>();
    private String fullText;
    
    private int startBodyPage = 1;
    private Map<Integer, Integer> pageNumberMap = new HashMap<>();
    
    public Integer getFooterPageNo(int pdfPageNo) {
        return pageNumberMap.get(pdfPageNo);
    }
    
    public boolean isBodyPage(int pdfPageNo) {
        return pdfPageNo >= startBodyPage;
    }
    
    public int getLogicalPageNo(int pdfPageNo) {
        return pdfPageNo;
    }
}
