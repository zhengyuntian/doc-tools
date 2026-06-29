package io.renren.modules.demo.engine.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedTable {
    private int index;
    private int pageNo;
    private int rowCount;
    private int colCount;
    private List<List<String>> cells = new ArrayList<>();
    private Boolean hasBorder;
    private String fontFamily;
    private Double fontSize;
    private String color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underline;
    private Boolean strikethrough;
}
