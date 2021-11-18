package com.shaidulin.kuskusbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class KusKusBotApplication {

    @Value("${spring.redis.host}")
    protected String redisHost;

    public static void main(String[] args) {
        SpringApplication.run(KusKusBotApplication.class, args);
    }

    @Bean
    WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    RedisClient redisClient() {
        return RedisClient.create(RedisURI.builder().withHost(redisHost).build());
    }

    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}
