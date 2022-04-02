package com.shaidulin.kuskusbot.processor.base.command;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.logstash.logback.marker.Markers.append;

/**
 * Home page
 */
@Slf4j
public record HomePageBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<SendMessage> process(Data data) {
        String userId = data.getUserId();
        return prepareCache(userId)
                .flatMap(ignored -> compileKeyboard(userId))
                .map(keyboardMarkup -> SendMessage
                        .builder()
                        .text("Приветствую тебя " + data.getFirstName() + " " + data.getLastName() +
                                "! Пожалуйста нажми кнопку \"Начать поиск\" чтобы искать рецепты")
                        .chatId(data.getChatId())
                        .replyMarkup(keyboardMarkup)
                        .build());
    }

    private Mono<String> prepareCache(String userId) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Initializing cache");
        }
        return cacheService.prepareUserCache(userId);
    }

    private Mono<InlineKeyboardMarkup> compileKeyboard(String userId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow("Начать поиск", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, Data.Session.builder().action(Data.Action.PROMPT_INGREDIENT).build());
        buttons.add(DynamicKeyboard.createDonationButtonRow());

        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Storing session: {}", sessionHash);
        }
        return cacheService
                .storeSession(userId, sessionHash)
                .map(ignored -> InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.HOME_PAGE;
    }
}
