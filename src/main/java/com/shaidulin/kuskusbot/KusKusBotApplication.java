package com.shaidulin.kuskusbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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
    LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(REPLICA_PREFERRED)
                .build();
        RedisStaticMasterReplicaConfiguration serverConfig = new RedisStaticMasterReplicaConfiguration(redisHost);
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    @Bean
    void launchBot() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new ReceiptBot(token, username, creatorId));
    }

}
