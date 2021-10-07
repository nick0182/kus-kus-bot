package com.shaidulin.kuskusbot.processor.command;

import com.shaidulin.kuskusbot.processor.BotProcessor;
import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.update.UpdateKey;
import com.shaidulin.kuskusbot.util.ButtonConstants;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import reactor.core.publisher.Mono;

public class HomePageBotProcessor extends BotProcessor {

    public HomePageBotProcessor(LettuceCacheService lettuceCacheService) {
        super(lettuceCacheService);
    }

    @Override
    public Mono<SendMessage> process(Update update) {
        User user = update.getMessage().getFrom();
        String userId = user.getId().toString();
        return lettuceCacheService
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
    public UpdateKey getKey() {
        return UpdateKey.HOME_PAGE;
    }
}
