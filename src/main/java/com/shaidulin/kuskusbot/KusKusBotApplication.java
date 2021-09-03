package com.shaidulin.kuskusbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class KusKusBotApplication {

    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.creatorId}")
    private int creatorId;

    public static void main(String[] args) {
        SpringApplication.run(KusKusBotApplication.class, args);
    }

    @Bean
    void launchBot() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new ReceiptBot(token, username, creatorId));
    }

}
