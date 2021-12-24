package com.shaidulin.kuskusbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaidulin.kuskusbot.ReceiptBot;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.processor.base.callback.*;
import com.shaidulin.kuskusbot.processor.base.command.HomePageBotProcessor;
import com.shaidulin.kuskusbot.processor.base.text.IngredientsPageBotProcessor;
import com.shaidulin.kuskusbot.processor.image.edit.ImageEditBotProcessor;
import com.shaidulin.kuskusbot.processor.image.edit.callback.ReceiptPresentationPaginatedBotProcessor;
import com.shaidulin.kuskusbot.processor.image.send.ImageSendBotProcessor;
import com.shaidulin.kuskusbot.processor.image.send.callback.ReceiptPresentationPageBotProcessor;
import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.api.impl.ImageServiceImpl;
import com.shaidulin.kuskusbot.service.api.impl.ReceiptServiceImpl;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.cache.impl.StringCacheServiceImpl;
import com.shaidulin.kuskusbot.update.RouterMapper;
import io.lettuce.core.RedisClient;
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

    @Value("${bot.receipt.page.size}")
    private int receiptPageSize;

    @Bean
    StringCacheService stringCacheService(RedisClient redisClient, ObjectMapper objectMapper) {
        return new StringCacheServiceImpl(redisClient.connect().reactive(), objectMapper);
    }

    @Bean
    ReceiptService receiptService(WebClient.Builder webClient) {
        return new ReceiptServiceImpl(webClient.build(), apiReceiptURL);
    }

    @Bean
    ImageService imageService(WebClient.Builder webClient) {
        return new ImageServiceImpl(webClient.build(), apiImageURL);
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
    BaseBotProcessor ingredientsPageBotProcessor(StringCacheService stringCacheService, ReceiptService receiptService) {
        return new IngredientsPageBotProcessor(stringCacheService, receiptService);
    }

    @Bean
    BaseBotProcessor ingredientSelectionBotProcessor(StringCacheService stringCacheService) {
        return new IngredientSelectionBotProcessor(stringCacheService);
    }

    @Bean
    BaseBotProcessor ingredientsPaginatedBotProcessor(StringCacheService stringCacheService) {
        return new IngredientsPaginatedBotProcessor(stringCacheService);
    }

    @Bean
    BaseBotProcessor sortPageBotProcessor() {
        return new SortPageBotProcessor();
    }

    @Bean
    BaseBotProcessor receiptIngredientsPageBotProcessor(StringCacheService stringCacheService,
                                                        ReceiptService receiptService) {
        return new ReceiptIngredientsPageBotProcessor(stringCacheService, receiptService);
    }

    @Bean
    BaseBotProcessor receiptNutritionOverviewPageBotProcessor(StringCacheService stringCacheService) {
        return new ReceiptNutritionOverviewPageBotProcessor(stringCacheService);
    }

    // ----------------------- image processors ---------------------------------------

    @Bean
    ImageSendBotProcessor receiptPresentationPageBotProcessor(StringCacheService stringCacheService,
                                                              ReceiptService receiptService,
                                                              ImageService imageService) {
        return new ReceiptPresentationPageBotProcessor(stringCacheService, receiptService, imageService, receiptPageSize);
    }

    @Bean
    ImageEditBotProcessor receiptPresentationPaginatedBotProcessor(StringCacheService stringCacheService,
                                                                   ReceiptService receiptService,
                                                                   ImageService imageService) {
        return new ReceiptPresentationPaginatedBotProcessor(stringCacheService, receiptService, imageService, receiptPageSize);
    }

    @Bean
    RouterMapper routerMapper(StringCacheService stringCacheService) {
        return new RouterMapper(stringCacheService);
    }

    @Bean
    LongPollingBot receiptBot(RouterMapper routerMapper,
                              List<BaseBotProcessor> baseBotProcessorList,
                              List<ImageSendBotProcessor> imageSendBotProcessorList,
                              List<ImageEditBotProcessor> imageEditBotProcessorList) {
        return new ReceiptBot(routerMapper, baseBotProcessorList, imageSendBotProcessorList, imageEditBotProcessorList) {
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
