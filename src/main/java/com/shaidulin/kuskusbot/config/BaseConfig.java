package com.shaidulin.kuskusbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaidulin.kuskusbot.ReceiptBot;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.processor.base.callback.IngredientSearchPageBotProcessor;
import com.shaidulin.kuskusbot.processor.base.callback.IngredientSelectionBotProcessor;
import com.shaidulin.kuskusbot.processor.base.callback.IngredientsPageBotProcessor;
import com.shaidulin.kuskusbot.processor.image.ImageBotProcessor;
import com.shaidulin.kuskusbot.processor.image.callback.ReceiptPresentationBotProcessor;
import com.shaidulin.kuskusbot.processor.base.command.HomePageBotProcessor;
import com.shaidulin.kuskusbot.processor.base.text.IngredientSearchBotProcessor;
import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.api.impl.ImageServiceImpl;
import com.shaidulin.kuskusbot.service.api.impl.ReceiptServiceImpl;
import com.shaidulin.kuskusbot.service.cache.ReceiptPresentationCacheService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.cache.codec.ReceiptPresentationCodec;
import com.shaidulin.kuskusbot.service.cache.impl.ReceiptPresentationCacheServiceImpl;
import com.shaidulin.kuskusbot.service.cache.impl.StringCacheServiceImpl;
import com.shaidulin.kuskusbot.update.RouterMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.RedisCodec;
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

    @Value("${api.receipt.url}")
    private String apiReceiptURL;

    @Value("${api.image.url}")
    private String apiImageURL;

    @Bean
    StringCacheService stringCacheService(RedisClient redisClient) {
        return new StringCacheServiceImpl(redisClient.connect().reactive());
    }

    @Bean
    RedisCodec<String, ReceiptPresentationValue> receiptPresentationCodec(ObjectMapper objectMapper) {
        return new ReceiptPresentationCodec(objectMapper);
    }

    @Bean
    ReceiptPresentationCacheService receiptPresentationCacheService(RedisClient redisClient,
                                                                    RedisCodec<String, ReceiptPresentationValue> codec) {
        return new ReceiptPresentationCacheServiceImpl(redisClient.connect(codec).reactive());
    }

    @Bean
    ReceiptService receiptService(WebClient webClient) {
        return new ReceiptServiceImpl(webClient, apiReceiptURL);
    }

    @Bean
    ImageService imageService(WebClient webClient) {
        return new ImageServiceImpl(webClient, apiImageURL);
    }

    // ----------------------- base processors ---------------------------------------

    @Bean
    BaseBotProcessor homePageBotProcessor(StringCacheService stringCacheService) {
        return new HomePageBotProcessor(stringCacheService);
    }

    @Bean
    BaseBotProcessor ingredientSearchPageBotProcessor(StringCacheService stringCacheService) {
        return new IngredientSearchPageBotProcessor(stringCacheService);
    }

    @Bean
    BaseBotProcessor ingredientSearchBotProcessor(StringCacheService stringCacheService, ReceiptService receiptService) {
        return new IngredientSearchBotProcessor(stringCacheService, receiptService);
    }

    @Bean
    BaseBotProcessor ingredientSelectionBotProcessor(StringCacheService stringCacheService) {
        return new IngredientSelectionBotProcessor(stringCacheService);
    }

    @Bean
    BaseBotProcessor ingredientsPageBotProcessor(StringCacheService stringCacheService) {
        return new IngredientsPageBotProcessor(stringCacheService);
    }

    // ----------------------- image processors ---------------------------------------

    @Bean
    ImageBotProcessor receiptPresentationBotProcessor(StringCacheService stringCacheService,
                                                      ReceiptPresentationCacheService receiptPresentationCacheService,
                                                      ReceiptService receiptService,
                                                      ImageService imageService) {
        return new ReceiptPresentationBotProcessor(stringCacheService,
                receiptPresentationCacheService, receiptService, imageService);
    }

    @Bean
    RouterMapper authorizer(StringCacheService stringCacheService) {
        return new RouterMapper(stringCacheService);
    }

    @Bean
    LongPollingBot receiptBot(RouterMapper routerMapper,
                              StringCacheService stringCacheService,
                              List<BaseBotProcessor> baseBotProcessorList,
                              List<ImageBotProcessor> imageBotProcessorList) {
        return new ReceiptBot(routerMapper, stringCacheService, baseBotProcessorList, imageBotProcessorList) {
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
