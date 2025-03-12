package com.example.stackoverflowfetcher.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CommonConfigurations {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean("customObjectMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
