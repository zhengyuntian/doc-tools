package io.renren.modules.demo.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ConfigurationProperties(prefix = "upload")
public class UploadConfig {

    private String path = "/tmp/dark_upload";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @PostConstruct
    public void init() {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}