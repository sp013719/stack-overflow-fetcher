package com.example.stackoverflowfetcher.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "stackexchange.api")
public class StackExchangeApiConfiguration {
    private String baseUrl;
    private String site;
    private String key = "";  // Default to empty string
    private int pageSize;
}
