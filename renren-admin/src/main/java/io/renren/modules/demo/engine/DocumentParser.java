package io.renren.modules.demo.engine;

import io.renren.modules.demo.engine.model.ParsedDocument;

public interface DocumentParser {
    ParsedDocument parse(String filePath) throws Exception;
    boolean supports(String fileType);
}
