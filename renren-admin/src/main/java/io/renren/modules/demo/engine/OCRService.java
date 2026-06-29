package io.renren.modules.demo.engine;

import io.renren.modules.demo.engine.model.ParsedImage;

import java.util.Map;

public interface OCRService {
    String recognizeText(ParsedImage image) throws Exception;
    Map<String, Object> recognizeTable(ParsedImage image) throws Exception;
}
