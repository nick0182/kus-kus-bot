package com.shaidulin.kuskusbot.processor.command;

import com.shaidulin.kuskusbot.processor.BotProcessor;
import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.update.UpdateKey;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public class SearchPageBotProcessor extends BotProcessor {

    public SearchPageBotProcessor(LettuceCacheService lettuceCacheService) {
        super(lettuceCacheService);
    }

    @Override
    public Mono<SendMessage> process(Update update) {
        String userId = update.getMessage().getFrom().getId().toString();
        return lettuceCacheService
                .startSearch(userId)
                .map(ignored -> new SendMessage(userId, "Пожалуйста напиши первый ингредиент"));
    }

    @Override
    public UpdateKey getKey() {
        return UpdateKey.SEARCH_PAGE;
    }
}
