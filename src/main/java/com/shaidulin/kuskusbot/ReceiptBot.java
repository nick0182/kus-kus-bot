package com.shaidulin.kuskusbot;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.processor.image.edit.ImageEditBotProcessor;
import com.shaidulin.kuskusbot.processor.image.send.ImageSendBotProcessor;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.update.RouterMapper;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.logstash.logback.marker.Markers.append;

@Slf4j
public abstract class ReceiptBot extends TelegramLongPollingBot {

    private final RouterMapper routerMapper;

    private final Map<Router.Type, BaseBotProcessor> baseBotProcessorMap;

    private final Map<Router.Type, ImageSendBotProcessor> imageSendBotProcessorMap;

    private final Map<Router.Type, ImageEditBotProcessor> imageEditBotProcessorMap;

    public ReceiptBot(RouterMapper routerMapper,
                      List<BaseBotProcessor> baseBotProcessorList,
                      List<ImageSendBotProcessor> imageSendBotProcessorList,
                      List<ImageEditBotProcessor> imageEditBotProcessorList) {
        this.routerMapper = routerMapper;
        baseBotProcessorMap = baseBotProcessorList
                .stream()
                .collect(Collectors.toMap(BaseBotProcessor::getType, Function.identity()));
        imageSendBotProcessorMap = imageSendBotProcessorList
                .stream()
                .collect(Collectors.toMap(ImageSendBotProcessor::getType, Function.identity()));
        imageEditBotProcessorMap = imageEditBotProcessorList
                .stream()
                .collect(Collectors.toMap(ImageEditBotProcessor::getType, Function.identity()));
    }

    @Override
    public void onUpdateReceived(Update update) {
        logUpdate(update);
        routerMapper
                .routeIncomingUpdate(update)
                .flatMap(router -> switch (router.method()) {
                    case BASE -> executeBaseMethod(router);
                    case IMAGE_SEND -> executeImageSendMethod(router, update.getCallbackQuery().getMessage());
                    case IMAGE_EDIT -> executeImageEditMethod(router);
                })
                .doOnError(error -> log.error("Error occurred during process of update: " + update, error))
                .subscribe();
    }

    private void logUpdate(Update update) {
        if (log.isTraceEnabled()) {
            long userId;
            if (update.hasMessage()) {
                userId = update.getMessage().getFrom().getId();
            } else {
                userId = update.getCallbackQuery().getFrom().getId();
            }
            log.trace(append("user_id", userId), "Got update: {}", update);
        }
    }

    private Mono<? extends BotApiMethod<?>> executeBaseMethod(Router router) {
        return baseBotProcessorMap.get(router.type())
                .process(router.data())
                .doOnSuccess(this::logIfNullMethod)
                .doOnNext(botApiMethod -> {
                    try {
                        if (log.isTraceEnabled()) {
                            log.trace(append("user_id", router.data().getUserId()), "Executing method: {}", botApiMethod);
                        }
                        execute(botApiMethod);
                    } catch (TelegramApiException e) {
                        log.error("Failed to execute Telegram API method", e);
                    }
                });
    }

    private Mono<String> executeImageSendMethod(Router router, Message message) {
        return imageSendBotProcessorMap.get(router.type())
                .process(router.data())
                .doOnSuccess(this::logIfNullMethod)
                .flatMap(sendPhoto -> {
                    try {
                        execute(deletePreviousMessage(message));

                        InputFile imageToSend = sendPhoto.getPhoto();
                        String imageName = imageToSend.getMediaName();
                        boolean isNewImage = imageToSend.isNew();

                        if (log.isTraceEnabled()) {
                            log.trace(append("user_id", router.data().getUserId()), "Executing method: {}", sendPhoto);
                        }
                        String telegramFileId = execute(sendPhoto).getPhoto().get(0).getFileId();

                        if (isNewImage) {
                            log.info("Storing new image in cache. file_id: {}; name: {}", telegramFileId, imageName);
                            return routerMapper.stringCacheService().storeImage(imageName, telegramFileId);
                        } else {
                            return Mono.empty();
                        }
                    } catch (TelegramApiException e) {
                        return Mono.error(new RuntimeException("Failed to execute Telegram API method", e));
                    }
                });
    }

    private Mono<String> executeImageEditMethod(Router router) {
        return imageEditBotProcessorMap.get(router.type())
                .process(router.data())
                .doOnSuccess(this::logIfNullMethod)
                .flatMap(editMessageMedia -> {
                    try {
                        InputMedia imageToSend = editMessageMedia.getMedia();
                        String imageName = imageToSend.getMediaName();
                        boolean isNewImage = imageToSend.isNewMedia();

                        if (log.isTraceEnabled()) {
                            log.trace(append("user_id", router.data().getUserId()), "Executing method: {}", editMessageMedia);
                        }
                        String telegramFileId = ((Message) execute(editMessageMedia)).getPhoto().get(0).getFileId();

                        if (isNewImage) {
                            log.info("Storing new image in cache. file_id: {}; name: {}", telegramFileId, imageName);
                            return routerMapper.stringCacheService().storeImage(imageName, telegramFileId);
                        } else {
                            return Mono.empty();
                        }
                    } catch (TelegramApiException e) {
                        return Mono.error(new RuntimeException("Failed to execute Telegram API method", e));
                    }
                });
    }

    private void logIfNullMethod(Object method) {
        if (method == null) {
            log.warn("No response was constructed and sent because BotApiMethod is null");
        }
    }

    private DeleteMessage deletePreviousMessage(Message message) {
        return DeleteMessage.builder()
                .messageId(message.getMessageId())
                .chatId(message.getChatId().toString())
                .build();
    }
}
