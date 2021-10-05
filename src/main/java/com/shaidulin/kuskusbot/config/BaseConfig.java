package com.shaidulin.kuskusbot.config;

import com.shaidulin.kuskusbot.ReceiptBot;
import com.shaidulin.kuskusbot.processor.BotProcessor;
import com.shaidulin.kuskusbot.processor.callback.IngredientSelectionBotProcessor;
import com.shaidulin.kuskusbot.processor.callback.IngredientsPageBotProcessor;
import com.shaidulin.kuskusbot.processor.command.HomePageBotProcessor;
import com.shaidulin.kuskusbot.processor.command.SearchPageBotProcessor;
import com.shaidulin.kuskusbot.processor.text.IngredientSearchBotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.api.impl.ReceiptServiceImpl;
import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.service.cache.impl.LettuceCacheServiceImpl;
import com.shaidulin.kuskusbot.update.Authorizer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.util.List;

@Configuration
public class BaseConfig {

    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

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

    @Bean
    RedisClient redisClient() {
        return RedisClient.create(RedisURI.builder().withHost(redisHost).build());
    }

    @Bean
    LettuceCacheService lettuceCacheService(RedisClient redisClient) {
        return new LettuceCacheServiceImpl(redisClient.connect().reactive());
    }

    @Bean
    BotProcessor homePageBotProcessor(LettuceCacheService lettuceCacheService) {
        return new HomePageBotProcessor(lettuceCacheService);
    }

    @Bean
    BotProcessor searchPageBotProcessor(LettuceCacheService lettuceCacheService) {
        return new SearchPageBotProcessor(lettuceCacheService);
    }

    @Bean
    BotProcessor ingredientSearchBotProcessor(LettuceCacheService lettuceCacheService, ReceiptService receiptService) {
        return new IngredientSearchBotProcessor(lettuceCacheService, receiptService);
    }

    @Bean
    BotProcessor ingredientSelectionBotProcessor(LettuceCacheService lettuceCacheService) {
        return new IngredientSelectionBotProcessor(lettuceCacheService);
    }

    @Bean
    BotProcessor ingredientsPageBotProcessor(LettuceCacheService lettuceCacheService) {
        return new IngredientsPageBotProcessor(lettuceCacheService);
    }

    @Bean
    Authorizer authorizer(LettuceCacheService lettuceCacheService) {
        return new Authorizer(lettuceCacheService);
    }

    @Bean
    LongPollingBot receiptBot(Authorizer authorizer, List<BotProcessor> botProcessorList) {
        return new ReceiptBot(authorizer, botProcessorList) {
            @Override
            public String getBotUsername() {
                return username;
            }

            @Override
            public String getBotToken() {
                return token;
            }
        };
    }
}
