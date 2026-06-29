package io.renren.modules.demo.engine.model;

import lombok.Data;

@Data
public class ParsedRun {
    private String text;
    private String fontFamily;
    private Double fontSize;
    private Boolean bold;
    private Boolean italic;
    private String color;
    private int startOffset;
    private int endOffset;
}
