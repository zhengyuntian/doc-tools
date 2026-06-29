package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.OCRService;
import io.renren.modules.demo.engine.model.ParsedImage;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaddleOCRService implements OCRService {

    @Value("${paddle.ocr.url:http://localhost:9292/ocr}")
    private String ocrUrl;

    @Value("${paddle.table.url:http://localhost:9292/table}")
    private String tableUrl;

    @Value("${paddle.enabled:false}")
    private boolean enabled;

    private OkHttpClient client;

    @PostConstruct
    public void init() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String recognizeText(ParsedImage image) throws Exception {
        if (!enabled) {
            return "";
        }

        if (image == null || image.getContent() == null) {
            return "";
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(image.getContent());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image", base64Image);
            requestBody.put("use_angle_cls", true);
            requestBody.put("use_gpu", false);

            RequestBody body = RequestBody.create(
                    com.alibaba.fastjson2.JSON.toJSONString(requestBody),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(ocrUrl)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("OCR请求失败: " + response);
                }

                String responseBody = response.body().string();
                return parseOCRResult(responseBody);
            }
        } catch (Exception e) {
            System.out.println("[OCR] 图片文字识别失败: " + e.getMessage());
            return "";
        }
    }

    @Override
    public Map<String, Object> recognizeTable(ParsedImage image) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        if (!enabled) {
            result.put("success", false);
            result.put("message", "OCR服务未启用");
            return result;
        }

        if (image == null || image.getContent() == null) {
            result.put("success", false);
            result.put("message", "图片内容为空");
            return result;
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(image.getContent());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image", base64Image);

            RequestBody body = RequestBody.create(
                    com.alibaba.fastjson2.JSON.toJSONString(requestBody),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(tableUrl)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("表格识别请求失败: " + response);
                }

                String responseBody = response.body().string();
                return parseTableResult(responseBody);
            }
        } catch (Exception e) {
            System.out.println("[OCR] 表格识别失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    private String parseOCRResult(String json) {
        try {
            com.alibaba.fastjson2.JSONObject obj = com.alibaba.fastjson2.JSON.parseObject(json);
            com.alibaba.fastjson2.JSONArray result = obj.getJSONArray("result");
            
            StringBuilder text = new StringBuilder();
            if (result != null) {
                for (int i = 0; i < result.size(); i++) {
                    com.alibaba.fastjson2.JSONArray line = result.getJSONArray(i);
                    if (line != null && line.size() > 1) {
                        text.append(line.getString(1)).append("\n");
                    }
                }
            }
            return text.toString().trim();
        } catch (Exception e) {
            System.out.println("[OCR] 解析OCR结果失败: " + e.getMessage());
            return json;
        }
    }

    private Map<String, Object> parseTableResult(String json) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            com.alibaba.fastjson2.JSONObject obj = com.alibaba.fastjson2.JSON.parseObject(json);
            
            if (obj.containsKey("html")) {
                result.put("success", true);
                result.put("html", obj.getString("html"));
                result.put("message", "表格识别成功");
            } else if (obj.containsKey("result")) {
                result.put("success", true);
                result.put("data", obj.get("result"));
                result.put("message", "表格识别成功");
            } else {
                result.put("success", false);
                result.put("message", "无法识别表格内容");
            }
        } catch (Exception e) {
            System.out.println("[OCR] 解析表格结果失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
}
