package com.shaidulin.kuskusbot.processor.base.command;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.ButtonConstants;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import reactor.core.publisher.Mono;

public record HomePageBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<SendMessage> process(Update update) {
        User user = update.getMessage().getFrom();
        String userId = user.getId().toString();
        return cacheService
                .prepareUserCache(userId)
                .map(ignored -> SendMessage
                        .builder()
                        .text("Приветствую тебя " + user.getFirstName() + " " + user.getLastName() +
                                "! Пожалуйста нажми кнопку \"Начать поиск\" чтобы искать рецепты")
                        .chatId(userId)
                        .replyMarkup(ButtonConstants.startSearchKeyboard)
                        .build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.HOME_PAGE;
    }
}
