package io.renren.modules.demo.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public class DJLEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println("[DJL配置] EnvironmentPostProcessor - 正在设置DJL系统属性...");
        System.setProperty("DJL_DEFAULT_ENGINE", "OnnxRuntime");
        System.setProperty("DJL_CACHE_DIR", "/tmp/djl_cache");
        System.setProperty("ai.djl.engine.default", "OnnxRuntime");
        System.out.println("[DJL配置] DJL系统属性设置完成");
    }
}
