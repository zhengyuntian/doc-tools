package io.renren.modules.demo.engine.model;

import lombok.Data;

@Data
public class ParsedImage {
    private int index;
    private int pageNo;
    private String fileName;
    private byte[] content;
    private String contentType;
    private int width;
    private int height;
}
