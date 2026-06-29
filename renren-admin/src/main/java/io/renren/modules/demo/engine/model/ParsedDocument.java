package io.renren.modules.demo.engine.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
}
