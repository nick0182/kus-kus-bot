package com.shaidulin.kuskusbot;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.processor.image.edit.ImageEditBotProcessor;
import com.shaidulin.kuskusbot.processor.image.send.ImageSendBotProcessor;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.update.RouterMapper;
import com.shaidulin.kuskusbot.util.ImageType;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
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
        log.debug("Got update: {}", update);
        routerMapper
                .routeIncomingUpdate(update)
                .flatMap(router -> switch (router.method()) {
                    case BASE -> executeBaseMethod(router.type(), update);
                    case IMAGE_SEND -> executeImageSendMethod(router.type(), update);
                    case IMAGE_EDIT -> executeImageEditMethod(router.type(), update);
                })
                .doOnError(error -> log.error("Error occurred during process of update: " + update, error))
                .subscribe();
    }

    private Mono<? extends BotApiMethod<?>> executeBaseMethod(Router.Type type, Update update) {
        return baseBotProcessorMap.get(type)
                .process(update)
                .doOnSuccess(this::logIfNullMethod)
                .doOnNext(botApiMethod -> {
                    try {
                        execute(botApiMethod);
                    } catch (TelegramApiException e) {
                        log.error("Failed to execute Telegram API method", e);
                    }
                });
    }

    private Mono<? extends SendPhoto> executeImageSendMethod(Router.Type type, Update update) {
        return imageSendBotProcessorMap.get(type)
                .process(update)
                .doOnSuccess(this::logIfNullMethod)
                .doOnNext(sendPhoto -> {
                    try {
                        execute(deletePreviousMessage(update.getCallbackQuery().getMessage()));

                        InputFile imageToSend = sendPhoto.getPhoto();
                        String imageName = imageToSend.getMediaName();
                        boolean isNewImage = imageToSend.isNew();

                        String telegramFileId = execute(sendPhoto).getPhoto().get(0).getFileId();

                        if (isNewImage) {
                            log.debug("Storing new image in cache with fileId: {} with name: {}", telegramFileId, imageName);
                            routerMapper.stringCacheService().storeImage(imageName, ImageType.MAIN, telegramFileId).subscribe();
                        }
                    } catch (TelegramApiException e) {
                        log.error("Failed to execute Telegram API method", e);
                    }
                });
    }

    private Mono<? extends EditMessageMedia> executeImageEditMethod(Router.Type type, Update update) {
        return imageEditBotProcessorMap.get(type)
                .process(update)
                .doOnSuccess(this::logIfNullMethod)
                .doOnNext(editMessageMedia -> {
                    try {
                        InputMedia imageToSend = editMessageMedia.getMedia();
                        String imageName = imageToSend.getMediaName();
                        boolean isNewImage = imageToSend.isNewMedia();

                        String telegramFileId = ((Message) execute(editMessageMedia)).getPhoto().get(0).getFileId();

                        if (isNewImage) {
                            log.debug("Storing new image in cache with fileId: {} with name: {}", telegramFileId, imageName);
                            routerMapper.stringCacheService().storeImage(imageName, ImageType.MAIN, telegramFileId).subscribe();
                        }
                    } catch (TelegramApiException e) {
                        log.error("Failed to execute Telegram API method", e);
                    }
                });
    }

    private void logIfNullMethod(Object method) {
        if (method == null) {
            log.debug("No response was constructed and sent because BotApiMethod is null");
        }
    }

    private DeleteMessage deletePreviousMessage(Message message) {
        return DeleteMessage.builder()
                .messageId(message.getMessageId())
                .chatId(message.getChatId().toString())
                .build();
    }
}