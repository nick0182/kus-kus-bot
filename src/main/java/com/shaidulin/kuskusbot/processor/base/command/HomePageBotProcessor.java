package com.shaidulin.kuskusbot.processor.base.command;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.SimpleKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

import static net.logstash.logback.marker.Markers.append;

/**
 * Home page
 */
@Slf4j
public record HomePageBotProcessor(StringCacheService cacheService,
                                   SimpleKeyboardProvider keyboardProvider,
                                   String greetingText) implements BaseBotProcessor {

    @Override
    public Mono<? extends BotApiMethod<?>> process(Data data) {
        String userId = data.getUserId();
        return prepareCache(userId)
                .flatMap(ignored -> keyboardProvider.compileKeyboard(userId))
                .map(keyboardMarkup -> compileMessage(keyboardMarkup, data));
    }

    private Mono<String> prepareCache(String userId) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Initializing cache");
        }
        return cacheService.prepareUserCache(userId);
    }

    private BotApiMethod<?> compileMessage(InlineKeyboardMarkup keyboardMarkup, Data data) {
        String firstName = data.getFirstName();
        String lastName = Optional.ofNullable(data.getLastName()).orElse("");
        String greeting = String.format(greetingText, firstName, lastName);
        String chatId = data.getChatId();
        if (isTriggeredByStartCommand(data.getInput())) {
            return SendMessage
                    .builder()
                    .text(greeting)
                    .chatId(chatId)
                    .replyMarkup(keyboardMarkup)
                    .parseMode("HTML")
                    .build();
        } else {
            return EditMessageText
                    .builder()
                    .text(greeting)
                    .chatId(chatId)
                    .messageId(data.getMessageId())
                    .replyMarkup(keyboardMarkup)
                    .parseMode("HTML")
                    .build();
        }
    }

    private boolean isTriggeredByStartCommand(String input) {
        return Objects.nonNull(input) && Objects.equals(input, "/start");
    }

    @Override
    public Router.Type getType() {
        return Router.Type.HOME_PAGE;
    }
}
