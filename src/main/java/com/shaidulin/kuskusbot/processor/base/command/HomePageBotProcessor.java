package com.shaidulin.kuskusbot.processor.base.command;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Mono;

import java.util.UUID;

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
                .map(buttonKey -> SendMessage
                        .builder()
                        .text("Приветствую тебя " + data.getFirstName() + " " + data.getLastName() +
                                "! Пожалуйста нажми кнопку \"Начать поиск\" чтобы искать рецепты")
                        .chatId(userId)
                        .replyMarkup(DynamicKeyboard.createHomePageKeyboard(buttonKey))
                        .build());
    }

    private Mono<UUID> compileStartSearchButton(String userId) {
        UUID key = UUID.randomUUID();
        return cacheService
                .storeSession(userId, key, Data.Session.builder().action(Data.Action.PROMPT_INGREDIENT).build())
                .map(ignored -> key);
    }

    @Override
    public Router.Type getType() {
        return Router.Type.HOME_PAGE;
    }
}
