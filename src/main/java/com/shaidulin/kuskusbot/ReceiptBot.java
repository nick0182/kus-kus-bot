package com.shaidulin.kuskusbot;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.processor.image.ImageBotProcessor;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.update.RouterMapper;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class ReceiptBot extends TelegramLongPollingBot {

    private final RouterMapper routerMapper;

    private final MethodExecutor<BotApiMethod<?>> baseMethodExecutor;

    private final MethodExecutor<SendPhoto> imageMethodExecutor;

    public ReceiptBot(RouterMapper routerMapper, List<BaseBotProcessor> baseBotProcessorList,
                      List<ImageBotProcessor> imageBotProcessorList) {
        this.routerMapper = routerMapper;
        baseMethodExecutor = new BaseMethodExecutor(baseBotProcessorList
                .stream()
                .collect(Collectors.toMap(BaseBotProcessor::getType, Function.identity())));
        imageMethodExecutor = new ImageMethodExecutor(imageBotProcessorList
                .stream()
                .collect(Collectors.toMap(ImageBotProcessor::getType, Function.identity())));
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Got update: {}", update);
        routerMapper
                .routeIncomingUpdate(update)
                .flatMap(router -> switch (router.method()) {
                    case BASE -> baseMethodExecutor.executeMethod(router.type(), update);
                    case IMAGE -> imageMethodExecutor.executeMethod(router.type(), update);
                })
                .doOnError(error -> log.error("Error occurred during process of update: " + update, error))
                .subscribe();
    }

    private interface MethodExecutor<T> {

        @SuppressWarnings("UnusedReturnValue")
        Mono<? extends T> executeMethod(Router.Type type, Update update);
    }

    private class BaseMethodExecutor implements MethodExecutor<BotApiMethod<?>> {

        private final Map<Router.Type, BaseBotProcessor> baseBotProcessorMap;

        private BaseMethodExecutor(Map<Router.Type, BaseBotProcessor> baseBotProcessorMap) {
            this.baseBotProcessorMap = baseBotProcessorMap;
        }

        @Override
        public Mono<? extends BotApiMethod<?>> executeMethod(Router.Type type, Update update) {
            return baseBotProcessorMap.get(type)
                    .process(update)
                    .doOnSuccess(method -> {
                        if (method == null) {
                            log.debug("No response was constructed and sent because BotApiMethod is null");
                        }
                    })
                    .doOnNext(botApiMethod -> {
                        try {
                            execute(botApiMethod);
                        } catch (TelegramApiException e) {
                            log.error("Failed to execute Telegram API method", e);
                        }
                    });
        }
    }

    private class ImageMethodExecutor implements MethodExecutor<SendPhoto> {

        private final Map<Router.Type, ImageBotProcessor> imageBotProcessorMap;

        private ImageMethodExecutor(Map<Router.Type, ImageBotProcessor> imageBotProcessorMap) {
            this.imageBotProcessorMap = imageBotProcessorMap;
        }

        @Override
        public Mono<? extends SendPhoto> executeMethod(Router.Type type, Update update) {
            return imageBotProcessorMap.get(type)
                    .process(update)
                    .doOnSuccess(method -> {
                        if (method == null) {
                            log.debug("No response was constructed and sent because BotApiMethod is null");
                        }
                    })
                    .doOnNext(botApiMethod -> {
                        try {
                            execute(deletePreviousMessage(update.getCallbackQuery().getMessage()));
                            execute(botApiMethod);
                        } catch (TelegramApiException e) {
                            log.error("Failed to execute Telegram API method", e);
                        }
                    });
        }

        private DeleteMessage deletePreviousMessage(Message message) {
            return DeleteMessage.builder()
                    .messageId(message.getMessageId())
                    .chatId(message.getChatId().toString())
                    .build();
        }
    }
}