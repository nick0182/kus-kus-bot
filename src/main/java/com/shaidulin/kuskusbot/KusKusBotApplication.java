package com.shaidulin.kuskusbot;

import com.shaidulin.kuskusbot.cache.Step;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static io.lettuce.core.ReadFrom.REPLICA_PREFERRED;

@SpringBootApplication
public class KusKusBotApplication {

    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.creatorId}")
    private int creatorId;

    @Value("${spring.redis.host}")
    private String redisHost;

    public static void main(String[] args) {
        SpringApplication.run(KusKusBotApplication.class, args);
    }

    @Bean
    @Profile("prod")
    LettuceConnectionFactory redisConnectionFactoryProd() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(REPLICA_PREFERRED)
                .build();
        RedisStaticMasterReplicaConfiguration serverConfig = new RedisStaticMasterReplicaConfiguration(redisHost);
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    @Bean
    LettuceConnectionFactory redisConnectionFactoryDev() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost));
    }

    @Bean
    @Profile("prod")
    RedisTemplate<Long, Step> redisTemplateProd(LettuceConnectionFactory redisConnectionFactoryProd) {
        RedisTemplate<Long, Step> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Long.class));
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        redisTemplate.setConnectionFactory(redisConnectionFactoryProd);
        return redisTemplate;
    }

    @Bean
    RedisTemplate<Long, Step> redisTemplateDev(LettuceConnectionFactory redisConnectionFactoryDev) {
        RedisTemplate<Long, Step> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Long.class));
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Step.class));
        redisTemplate.setConnectionFactory(redisConnectionFactoryDev);
        return redisTemplate;
    }

    @Bean
    @Profile("prod")
    BotSession launchBotProd(RedisTemplate<Long, Step> redisTemplateProd) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        return botsApi.registerBot(new ReceiptBot(token, username, creatorId, redisTemplateProd.opsForList()));
    }

    @Bean
    BotSession launchBotDev(RedisTemplate<Long, Step> redisTemplateDev) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        return botsApi.registerBot(new ReceiptBot(token, username, creatorId, redisTemplateDev.opsForList()));
    }
}
