package com.shaidulin.kuskusbot.processor.text;

import com.shaidulin.kuskusbot.processor.BotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.update.UpdateKey;
import com.shaidulin.kuskusbot.util.KeyboardCreator;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public class IngredientSearchBotProcessor extends BotProcessor {

    private final ReceiptService receiptService;

    public IngredientSearchBotProcessor(LettuceCacheService lettuceCacheService, ReceiptService receiptService) {
        super(lettuceCacheService);
        this.receiptService = receiptService;
    }

    @Override
    public Mono<SendMessage> process(Update update) {
        String userId = update.getMessage().getFrom().getId().toString();
        String toSearch = update.getMessage().getText();
        return lettuceCacheService
                .getIngredients(userId)
                .flatMap(known -> receiptService.suggestIngredients(toSearch, known))
                .map(ingredientMatch -> {
                    if (ingredientMatch.getIngredients().isEmpty()) {
                        throw new IllegalArgumentException(); // FIXME create custom exception
                    } else {
                        return ingredientMatch.getIngredients();
                    }
                })
                .filterWhen(ingredientMatch -> lettuceCacheService.storeIngredientSuggestions(userId, ingredientMatch))
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
    public UpdateKey getKey() {
        return UpdateKey.USER_TEXT;
    }
}
