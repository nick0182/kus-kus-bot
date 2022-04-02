package com.shaidulin.kuskusbot.service.util.impl;

import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.MediaMessageProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.util.ImageType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static net.logstash.logback.marker.Markers.append;

@Slf4j
public record EditMessageMediaProviderImpl(StringCacheService cacheService, ImageService imageService)
        implements MediaMessageProvider<EditMessageMedia> {

    @Override
    public Mono<EditMessageMedia> provideMessage(String name, String caption, ImageType type, Data data, InlineKeyboardMarkup keyboard) {
        return cacheService
                .getImage(name)
                .map(cachedImage -> compileMessage(cachedImage, null, data, caption, name, keyboard))
                .switchIfEmpty(imageService
                        .fetchImage(type, name)
                        .map(imageBytes -> compileMessage(null, imageBytes, data, caption, name, keyboard)));
    }

    @SneakyThrows
    private EditMessageMedia compileMessage(String cachedImage, byte[] imageBytes, Data data,
                                            String caption, String name, InlineKeyboardMarkup keyboard) {
        boolean isNewMedia = Objects.isNull(cachedImage);
        String userId = data.getUserId();

        InputMedia photo = new InputMediaPhoto();
        photo.setCaption(caption);
        if (isNewMedia) {
            if (log.isTraceEnabled()) {
                log.trace(append("user_id", userId),
                        "Compiling photo from resource with length: {} and name: {}", imageBytes.length, name);
            }
            photo.setMedia(new ByteArrayResource(imageBytes).getInputStream(), name);
        } else {
            if (log.isTraceEnabled()) {
                log.trace(append("user_id", userId), "Compiling photo from cache with name: {}", name);
            }
            photo.setMedia(cachedImage);
        }

        return EditMessageMedia.builder()
                .chatId(data.getChatId())
                .messageId(data.getMessageId())
                .media(photo)
                .replyMarkup(keyboard)
                .build();
    }
}
