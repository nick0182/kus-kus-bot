package com.shaidulin.kuskusbot.processor.base.command;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Home page
 */
public record HomePageBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<SendMessage> process(Data data) {
        String userId = data.getUserId();
        return cacheService
                .prepareUserCache(userId)
                .flatMap(ignored -> compileStartSearchButton(userId))
                .map(keyboardMarkup -> SendMessage
                        .builder()
                        .text("Приветствую тебя " + data.getFirstName() + " " + data.getLastName() +
                                "! Пожалуйста нажми кнопку \"Начать поиск\" чтобы искать рецепты")
                        .chatId(userId)
                        .replyMarkup(keyboardMarkup)
                        .build());
    }

    private Mono<InlineKeyboardMarkup> compileStartSearchButton(String userId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        int buttonIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow("Начать поиск", String.valueOf(buttonIndex)));
        return cacheService
                .storeSession(userId, Map.of(buttonIndex, Data.Session.builder().action(Data.Action.PROMPT_INGREDIENT).build()))
                .map(ignored -> InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.HOME_PAGE;
    }
}
