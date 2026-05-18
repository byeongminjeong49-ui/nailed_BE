package com.nailed.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 로컬 업로드 이미지 서빙 설정
 * - /uploads/** 요청을 실제 저장 디렉토리와 매핑한다
 * - ProductService.uploadImage()에서 저장한 파일을 프론트엔드가 URL로 접근 가능하게 한다
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
