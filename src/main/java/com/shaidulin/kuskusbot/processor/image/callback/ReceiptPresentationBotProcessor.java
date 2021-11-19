package com.shaidulin.kuskusbot.processor.image.callback;

import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.processor.image.ImageBotProcessor;
import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.ReceiptPresentationCacheService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import com.shaidulin.kuskusbot.util.ImageType;
import com.shaidulin.kuskusbot.util.KeyboardCreator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.threeten.extra.AmountFormats;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
public record ReceiptPresentationBotProcessor(StringCacheService stringCacheService,
                                              ReceiptPresentationCacheService receiptPresentationCacheService,
                                              ReceiptService receiptService,
                                              ImageService imageService) implements ImageBotProcessor {

    private static final Locale RUSSIAN = new Locale.Builder().setLanguage("ru").setRegion("RU").build();

    private static final String NOT_DEFINED = "Не указано";

    @Override
    public Mono<? extends SendPhoto> process(Update update) {
        CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
        return stringCacheService
                .getIngredients(callbackWrapper.userId())
                .flatMap(receiptService::getReceiptPresentations)
                .map(ReceiptPresentationMatch::receipts)
                .filterWhen(receiptPresentations -> receiptPresentationCacheService()
                        .storeReceiptPresentations(callbackWrapper.userId(), receiptPresentations))
                .flatMap(receiptPresentations1 ->
                        provideMessage(receiptPresentations1, callbackWrapper.chatId()));
    }

    private Mono<SendPhoto> provideMessage(List<ReceiptPresentationValue> receiptPresentations, String chatId) {
        InlineKeyboardMarkup keyboard = KeyboardCreator.createReceiptKeyboard(receiptPresentations, 0);

        ReceiptPresentationValue receiptPresentation = receiptPresentations.get(0);

        log.debug("Got receipt presentation to render: {}", receiptPresentation);

        String imageId = String.valueOf(receiptPresentation.queryParam());

        return stringCacheService
                .getImage(imageId, ImageType.MAIN)
                .map(cachedImage -> compileMessage(cachedImage, null, chatId, receiptPresentation, keyboard))
                .switchIfEmpty(imageService
                        .fetchImage(ImageType.MAIN, imageId)
                        .map(imageBytes -> compileMessage(null, imageBytes, chatId, receiptPresentation, keyboard))
                );
    }

    @SneakyThrows
    private SendPhoto compileMessage(String cachedImage, byte[] imageBytes, String chatId,
                                     ReceiptPresentationValue receiptPresentation, InlineKeyboardMarkup keyboard) {
        boolean isNewMedia = Objects.isNull(cachedImage);
        String imageName = String.valueOf(receiptPresentation.queryParam());

        InputFile photo = new InputFile();
        if (isNewMedia) {
            log.debug("Compiling photo from resource with length: {} and name: {}", imageBytes.length, imageName);
            photo.setMedia(new ByteArrayResource(imageBytes).getInputStream(), imageName);
        } else {
            log.debug("Compiling photo from cache with name: {}", imageName);
            photo.setMedia(cachedImage);
        }

        return SendPhoto.builder()
                .chatId(chatId)
                .photo(photo)
                .caption(createPhotoCaption(receiptPresentation))
                .replyMarkup(keyboard)
                .build();
    }

    private String createPhotoCaption(ReceiptPresentationValue receiptPresentation) {
        Duration cookTime = receiptPresentation.cookTime();
        String cookTimeString = cookTime != null ? AmountFormats.wordBased(cookTime, RUSSIAN) : NOT_DEFINED;

        int portions = receiptPresentation.portions();
        String portionsString = portions != 0 ? String.valueOf(portions) : NOT_DEFINED;

        return "\uD83C\uDF72 " + receiptPresentation.name() + "\n" +
                "⏱ " + cookTimeString + "\n" +
                "Количество порций " + portionsString;
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPTS_PAGE;
    }
}
