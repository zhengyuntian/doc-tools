package io.renren.modules.demo.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class DJLConfigListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        System.out.println("[DJL配置] 正在设置DJL系统属性...");
        System.setProperty("DJL_DEFAULT_ENGINE", "OnnxRuntime");
        System.setProperty("DJL_CACHE_DIR", "/tmp/djl_cache");
        System.setProperty("ai.djl.engine.default", "OnnxRuntime");
        System.out.println("[DJL配置] DJL系统属性设置完成:");
        System.out.println("[DJL配置]   DJL_DEFAULT_ENGINE = " + System.getProperty("DJL_DEFAULT_ENGINE"));
        System.out.println("[DJL配置]   DJL_CACHE_DIR = " + System.getProperty("DJL_CACHE_DIR"));
        System.out.println("[DJL配置]   ai.djl.engine.default = " + System.getProperty("ai.djl.engine.default"));
    }
}
