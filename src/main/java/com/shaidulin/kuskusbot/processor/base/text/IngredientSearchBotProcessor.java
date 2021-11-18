package com.shaidulin.kuskusbot.processor.base.text;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.KeyboardCreator;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public record IngredientSearchBotProcessor(StringCacheService cacheService, ReceiptService receiptService)
        implements BaseBotProcessor {

    @Override
    public Mono<SendMessage> process(Update update) {
        String userId = update.getMessage().getFrom().getId().toString();
        String toSearch = update.getMessage().getText();
        return cacheService
                .getIngredients(userId)
                .flatMap(known -> receiptService.suggestIngredients(toSearch, known))
                .map(ingredientMatch -> {
                    if (ingredientMatch.ingredients().isEmpty()) {
                        throw new IllegalArgumentException(); // FIXME create custom exception
                    } else {
                        return ingredientMatch.ingredients();
                    }
                })
                .filterWhen(ingredientMatch -> cacheService.storeIngredientSuggestions(userId, ingredientMatch))
                .map(ingredientMatch -> KeyboardCreator.createSuggestionsKeyboard(ingredientMatch, 0))
                .map(ingredientsKeyboard -> SendMessage
                        .builder()
                        .text("Вот что смог найти")
                        .chatId(userId)
                        .replyMarkup(ingredientsKeyboard)
                        .build()
                )
                .onErrorReturn(IllegalArgumentException.class,
                        SendMessage
                                .builder()
                                .text("Ничего не нашел \uD83E\uDD14 Попробуй еще раз")
                                .chatId(userId)
                                .build()
                );
    }

    @Override
    public Router.Type getType() {
        return Router.Type.USER_TEXT;
    }
}
