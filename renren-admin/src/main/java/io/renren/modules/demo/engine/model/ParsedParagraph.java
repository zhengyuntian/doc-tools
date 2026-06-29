package io.renren.modules.demo.engine.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedParagraph {
    private int index;
    private String text;
    private String alignment;
    private String fontFamily;
    private Double fontSize;
    private Boolean bold;
    private Boolean italic;
    private Boolean underline;
    private Boolean strikethrough;
    private Boolean shading;
    private String color;
    private String lineSpacing;
    private Double lineSpacingValue;
    private Double firstLineIndent;
    private Double leftIndent;
    private Double rightIndent;
    private Double spaceBefore;
    private Double spaceAfter;
    private Double fontScale;
    private Double characterSpacing;
    private String direction;
    private Boolean isTitle;
    private String titleStyle;
    private int pageNo;
    private List<ParsedRun> runs = new ArrayList<>();
}
