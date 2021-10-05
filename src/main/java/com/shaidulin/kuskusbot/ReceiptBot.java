package com.shaidulin.kuskusbot;

import com.shaidulin.kuskusbot.processor.BotProcessor;
import com.shaidulin.kuskusbot.update.Authorizer;
import com.shaidulin.kuskusbot.update.UpdateKey;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class ReceiptBot extends TelegramLongPollingBot {

    private final Authorizer authorizer;

    private final Map<UpdateKey, BotProcessor> botProcessorMap;

    public ReceiptBot(Authorizer authorizer, List<BotProcessor> botProcessorList) {
        this.authorizer = authorizer;
        botProcessorMap = botProcessorList
                .stream()
                .collect(Collectors.toMap(BotProcessor::getKey, Function.identity()));
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Got update: {}", update);
        authorizer
                .authorize(update)
                .flatMap(updateKey -> botProcessorMap.get(updateKey).process(update))
                .doOnSuccess(botApiMethod -> {
                    if (botApiMethod == null) {
                        log.error("No response was constructed and sent for update: " + update);
                    }
                })
                .subscribe(
                        botApiMethod -> {
                            try {
                                execute(botApiMethod);
                            } catch (TelegramApiException e) {
                                log.error("Failed to execute Telegram API method", e);
                            }
                        },
                        error -> log.error("Error occurred during process of update: " + update, error)
                );
    }
}