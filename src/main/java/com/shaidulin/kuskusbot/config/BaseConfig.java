package com.shaidulin.kuskusbot.config;

import com.shaidulin.kuskusbot.ReceiptBot;
import com.shaidulin.kuskusbot.ability.IngredientSearchAbility;
import com.shaidulin.kuskusbot.ability.NewUserAbility;
import com.shaidulin.kuskusbot.service.cache.CacheService;
import com.shaidulin.kuskusbot.service.cache.Step;
import com.shaidulin.kuskusbot.service.cache.impl.CacheServiceImpl;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.api.impl.ReceiptServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

public class BaseConfig {

    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.creatorId}")
    private int creatorId;

    @Value("${spring.redis.host}")
    protected String redisHost;

    @Value("${api.receipt.url}")
    private String apiURL;

    @Bean
    WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    ReceiptService receiptService(WebClient webClient) {
        return new ReceiptServiceImpl(webClient, apiURL);
    }

    /*
     * a list of steps
     * key pattern {chatId}
     */
    @Bean
    RedisTemplate<Long, Step> stepsRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<Long, Step> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Long.class));
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Step.class));
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    /*
     * a sorted set (by count of receipts DESC) of suggested ingredients for a current step
     * key pattern {chatId}:{stepName}:ingredients
     */
    @Bean
    RedisTemplate<String, String> ingredientsRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    /*
     * a value of suggested ingredient's current page
     * key pattern {chatId}:{stepName}:ingredients:page
     */
    @Bean
    RedisTemplate<String, Integer> ingredientsPageRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Integer.class));
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    CacheService cacheService(RedisTemplate<Long, Step> stepsRedisTemplate,
                              RedisTemplate<String, String> ingredientsRedisTemplate,
                              RedisTemplate<String, Integer> ingredientsPageRedisTemplate) {
        return new CacheServiceImpl(stepsRedisTemplate.opsForList(), ingredientsRedisTemplate.opsForZSet(), ingredientsPageRedisTemplate.opsForValue());
    }

    @Bean
    AbilityExtension newUserAbility(CacheService cacheService) {
        return new NewUserAbility(cacheService);
    }

    @Bean
    AbilityExtension ingredientSearchAbility(CacheService cacheService, ReceiptService receiptService) {
        return new IngredientSearchAbility(cacheService, receiptService);
    }

    @Bean
    LongPollingBot receiptBot(AbilityExtension newUserAbility, AbilityExtension ingredientSearchAbility) {
        return new ReceiptBot(token, username, creatorId, newUserAbility, ingredientSearchAbility);
    }
}
