package com.ll.demo03.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {
    //dd
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
