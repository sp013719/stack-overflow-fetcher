package com.example.stackoverflowfetcher.configuration;

import com.example.stackoverflowfetcher.job.DownloadJob;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Setter
@Configuration
@ConfigurationProperties(prefix = "redis")
public class RedisConfiguration {
    private String host;
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(this.host, this.port);
        return new JedisConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, DownloadJob> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, DownloadJob> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
