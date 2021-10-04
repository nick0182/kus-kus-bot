package com.shaidulin.kuskusbot;

import com.shaidulin.kuskusbot.processor.BotProcessor;
import com.shaidulin.kuskusbot.update.UpdateKey;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.shaidulin.kuskusbot.update.UpdateKeyMapper.mapKey;

@Slf4j
public abstract class ReceiptBot extends TelegramLongPollingBot {

    private final Map<UpdateKey, BotProcessor> botProcessorMap;

    public ReceiptBot(List<BotProcessor> botProcessorList) {
        botProcessorMap = botProcessorList
                .stream()
                .collect(Collectors.toMap(BotProcessor::getKey, Function.identity()));
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Got update: {}", update);
        UpdateKey updateKey = mapKey(update);
        if (Objects.nonNull(updateKey)) {
            botProcessorMap
                    .get(updateKey)
                    .process(update)
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
}