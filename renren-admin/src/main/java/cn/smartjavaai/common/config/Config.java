package cn.smartjavaai.common.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

public class Config {

    private static final String CACHE_DIR = "smartjavaai_cache";

    private static String cachePath;

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    static{
        createCachePath();
        if(StringUtils.isNotBlank(cachePath)){
            System.setProperty("DJL_CACHE_DIR", cachePath);
        }
        String defaultEngine = System.getProperty("ai.djl.default_engine");
        if(StringUtils.isBlank(defaultEngine)){
            defaultEngine = System.getProperty("DJL_DEFAULT_ENGINE");
        }
        if(StringUtils.isBlank(defaultEngine)){
            defaultEngine = "PyTorch";
        }
        System.setProperty("ai.djl.default_engine", defaultEngine);
        log.info("设置默认引擎：{}", defaultEngine);
    }

    public static void setCachePath(String customeCachePath) {
        if (StringUtils.isNotBlank(customeCachePath)) {
            cachePath = customeCachePath;
            new File(cachePath).mkdirs();
            System.setProperty("DJL_CACHE_DIR", cachePath);
        } else {
            throw new IllegalArgumentException("缓存路径不允许为空");
        }
    }

    public static String getCachePath() {
        if(StringUtils.isBlank(cachePath)){
            createCachePath();
        }
        if(StringUtils.isNotBlank(cachePath)){
            System.setProperty("DJL_CACHE_DIR", cachePath);
        }
        return cachePath;
    }

    public static String getCachePathFromSystem() {
        return System.getProperty("DJL_CACHE_DIR");
    }

    private static void createCachePath(){
        String osName = System.getProperty("os.name");
        log.info("当前操作系统：{}", osName);
        if(osName.toLowerCase().contains("windows")){
            cachePath = Paths.get(
                    System.getProperty("user.home"),
                    "smartjavaai_cache"
            ).toString();
            new File(cachePath).mkdirs();
        }else if(osName.toLowerCase().contains("linux")){
            cachePath = "/root/" + CACHE_DIR;
            new File(cachePath).mkdirs();
        }else if(osName.toLowerCase().contains("mac")){
            cachePath = Paths.get(
                    System.getProperty("user.home"),
                    "smartjavaai_cache"
            ).toString();
            new File(cachePath).mkdirs();
        }else{
            cachePath = Paths.get(
                    System.getProperty("user.home"),
                    "smartjavaai_cache"
            ).toString();
            new File(cachePath).mkdirs();
        }
    }
}
