package com.shaidulin.kuskusbot.processor.callback;

import com.shaidulin.kuskusbot.processor.BotProcessor;
import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.update.UpdateKey;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public class IngredientSelectionBotProcessor extends BotProcessor {

    public IngredientSelectionBotProcessor(LettuceCacheService lettuceCacheService) {
        super(lettuceCacheService);
    }

    @Override
    public Mono<EditMessageReplyMarkup> process(Update update) {

        return null;
    }

    @Override
    public UpdateKey getKey() {
        return UpdateKey.USER_INGREDIENT_SELECTION;
    }
}
