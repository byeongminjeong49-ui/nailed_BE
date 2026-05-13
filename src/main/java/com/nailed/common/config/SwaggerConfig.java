package com.nailed.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI (Swagger) 설정
 * 접속: http://localhost:8080/swagger-ui/index.html
 *
 * [Swagger에서 Mock 로그인 방법]
 * 1. POST /api/auth/mock-login 실행 → accessToken 복사
 * 2. 우측 상단 Authorize 버튼 클릭
 * 3. Bearer {accessToken} 입력 → 이후 모든 API 자동 인증
 *
 * [build.gradle 의존성]
 * implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Nailed API")
                        .description("""
                                중고 명품·IT기기 거래 플랫폼 **Nailed** REST API 문서
                                
                                **Mock 로그인:** `POST /api/auth/mock-login` → `{ "memberId": 1 }` 입력 시 JWT 발급
                                """)
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                                .name(jwtSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
