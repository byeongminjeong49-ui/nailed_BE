package com.nailed.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.static.product.path:src/main/resources/static/images/products}")
    private String staticProductPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 임시 업로드 파일 서빙 (상품 등록 전 미리보기용)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");

        // 상품 이미지 서빙 - classpath 캐시 우회를 위해 filesystem에서 직접 읽음
        registry.addResourceHandler("/images/products/**")
                .addResourceLocations("file:" + staticProductPath + "/");
    }
}
