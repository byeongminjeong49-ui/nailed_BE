package com.nailed.web.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KakaoPayConfig {

    @Bean("kakaoPayRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}