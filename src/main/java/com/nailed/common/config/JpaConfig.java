package com.nailed.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정
 * @EnableJpaAuditing 을 메인 클래스 대신 여기서 관리
 * → 슬라이스 테스트(@WebMvcTest 등) 시 메인 클래스 로딩 없이 정상 동작
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
