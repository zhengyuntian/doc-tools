package io.renren.modules.demo.engine.model;

import lombok.Data;

@Data
public class ParsedPageSetup {
    private String paperSize;
    private Double marginTop;
    private Double marginBottom;
    private Double marginLeft;
    private Double marginRight;
    private Boolean hasHeader;
    private Boolean hasFooter;
}
