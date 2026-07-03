/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 * renren-admin
 *
 * @author Mark sunlightcs@gmail.com
 */
@SpringBootApplication
@EnableAsync
public class AdminApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		System.setProperty("ai.djl.default_engine", "OnnxRuntime");
		System.setProperty("DJL_DEFAULT_ENGINE", "OnnxRuntime");
		System.setProperty("DJL_CACHE_DIR", "/tmp/djl_cache");
		System.setProperty("ai.djl.engine.default", "OnnxRuntime");
		System.setProperty("DJL_PYTORCH_HOME", "/tmp/pytorch_cache");
		System.setProperty("PYTORCH_HOME", "/tmp/pytorch_cache");
		System.setProperty("DJL_CACHE_HOME", "/tmp/djl_cache");
		System.setProperty("ai.djl.onnx.disable_alternative", "true");
		SpringApplication.run(AdminApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(AdminApplication.class);
	}
}