package com.shaidulin.kuskusbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaidulin.kuskusbot.ReceiptBot;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.processor.base.callback.*;
import com.shaidulin.kuskusbot.processor.base.command.HomePageBotProcessor;
import com.shaidulin.kuskusbot.processor.base.text.IngredientsPageBotProcessor;
import com.shaidulin.kuskusbot.processor.image.edit.ImageEditBotProcessor;
import com.shaidulin.kuskusbot.processor.image.edit.callback.ReceiptPresentationPaginatedBotProcessor;
import com.shaidulin.kuskusbot.processor.image.edit.callback.ReceiptStepPaginatedBotProcessor;
import com.shaidulin.kuskusbot.processor.image.send.ImageSendBotProcessor;
import com.shaidulin.kuskusbot.processor.image.send.callback.ReceiptPresentationPageBotProcessor;
import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.api.impl.ImageServiceImpl;
import com.shaidulin.kuskusbot.service.api.impl.ReceiptServiceImpl;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.cache.impl.StringCacheServiceImpl;
import com.shaidulin.kuskusbot.service.util.*;
import com.shaidulin.kuskusbot.service.util.impl.*;
import com.shaidulin.kuskusbot.update.RouterMapper;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
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

    @Bean
    MediaMessageProvider<EditMessageMedia> editMessageMediaProvider(StringCacheService cacheService,
                                                                    ImageService imageService) {
        return new EditMessageMediaProviderImpl(cacheService, imageService);
    }

    @Bean
    IngredientPageKeyboardProvider ingredientPageKeyboardProvider(StringCacheService cacheService) {
        return new IngredientPageKeyboardProviderImpl(cacheService);
    }

    @Bean
    ReceiptKeyboardProvider receiptKeyboardProvider(StringCacheService cacheService) {
        return new ReceiptKeyboardProviderImpl(cacheService);
    }

    @Bean
    ReceiptPageKeyboardProvider receiptPageKeyboardProvider(StringCacheService cacheService) {
        return new ReceiptPageKeyboardProviderImpl(cacheService);
    }

    @Bean
    StepPageKeyboardProvider stepPageKeyboardProvider(StringCacheService cacheService) {
        return new StepPageKeyboardProviderImpl(cacheService);
    }

    @Bean
    SimpleKeyboardProvider ingredientSelectionKeyboardProviderImpl(StringCacheService cacheService) {
        return new IngredientSelectionKeyboardProviderImpl(cacheService);
    }

    @Bean
    SimpleKeyboardProvider sortPageKeyboardProviderImpl(StringCacheService cacheService) {
        return new SortPageKeyboardProviderImpl(cacheService);
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
    BaseBotProcessor ingredientsPageBotProcessor(StringCacheService stringCacheService, ReceiptService receiptService,
                                                 IngredientPageKeyboardProvider ingredientPageKeyboardProvider) {
        return new IngredientsPageBotProcessor(stringCacheService, receiptService, ingredientPageKeyboardProvider);
    }

    @Bean
    BaseBotProcessor ingredientSelectionBotProcessor(StringCacheService stringCacheService,
                                                     SimpleKeyboardProvider ingredientSelectionKeyboardProviderImpl) {
        return new IngredientSelectionBotProcessor(stringCacheService, ingredientSelectionKeyboardProviderImpl);
    }

    @Bean
    BaseBotProcessor ingredientsPaginatedBotProcessor(StringCacheService stringCacheService,
                                                      IngredientPageKeyboardProvider ingredientPageKeyboardProvider) {
        return new IngredientsPaginatedBotProcessor(stringCacheService, ingredientPageKeyboardProvider);
    }

    @Bean
    BaseBotProcessor sortPageBotProcessor(SimpleKeyboardProvider sortPageKeyboardProviderImpl) {
        return new SortPageBotProcessor(sortPageKeyboardProviderImpl);
    }

    @Bean
    BaseBotProcessor receiptIngredientsPageBotProcessor(StringCacheService stringCacheService,
                                                        ReceiptService receiptService,
                                                        ReceiptKeyboardProvider receiptKeyboardProvider) {
        return new ReceiptIngredientsPageBotProcessor(stringCacheService, receiptService, receiptKeyboardProvider);
    }

    @Bean
    BaseBotProcessor receiptNutritionOverviewPageBotProcessor(StringCacheService stringCacheService,
                                                              ReceiptKeyboardProvider receiptKeyboardProvider) {
        return new ReceiptNutritionOverviewPageBotProcessor(stringCacheService, receiptKeyboardProvider);
    }

    // ----------------------- image processors ---------------------------------------

    @Bean
    ImageSendBotProcessor receiptPresentationPageBotProcessor(StringCacheService stringCacheService,
                                                              ReceiptService receiptService,
                                                              ReceiptPageKeyboardProvider receiptPageKeyboardProvider,
                                                              ImageService imageService) {
        return new ReceiptPresentationPageBotProcessor(stringCacheService, receiptService, receiptPageKeyboardProvider,
                imageService, receiptPageSize);
    }

    @Bean
    ImageEditBotProcessor receiptPresentationPaginatedBotProcessor(StringCacheService stringCacheService,
                                                                   ReceiptService receiptService,
                                                                   ReceiptPageKeyboardProvider receiptPageKeyboardProvider,
                                                                   MediaMessageProvider<EditMessageMedia> editMessageMediaProvider) {
        return new ReceiptPresentationPaginatedBotProcessor(stringCacheService, receiptService, receiptPageKeyboardProvider,
                editMessageMediaProvider, receiptPageSize);
    }

    @Bean
    ImageEditBotProcessor receiptStepPaginatedBotProcessor(StringCacheService stringCacheService,
                                                           StepPageKeyboardProvider stepPageKeyboardProvider,
                                                           MediaMessageProvider<EditMessageMedia> editMessageMediaProvider) {
        return new ReceiptStepPaginatedBotProcessor(stringCacheService, stepPageKeyboardProvider, editMessageMediaProvider);
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
