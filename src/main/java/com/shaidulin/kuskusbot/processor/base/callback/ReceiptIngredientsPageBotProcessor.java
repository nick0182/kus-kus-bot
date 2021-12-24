package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.receipt.Ingredient;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import com.shaidulin.kuskusbot.util.KeyboardCreator;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shaidulin.kuskusbot.util.ButtonConstants.RECEIPT_WITH_NUTRITION_OVERVIEW_PAYLOAD_PREFIX;
import static com.shaidulin.kuskusbot.util.ButtonConstants.SHOW_NUTRITION_OVERVIEW;

public record ReceiptIngredientsPageBotProcessor(StringCacheService stringCacheService,
                                                 ReceiptService receiptService) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageCaption> process(Update update) {
        CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
        String[] dataArray = callbackWrapper.data().split("_");
        int receiptId = Integer.parseInt(dataArray[0]);
        int page = Integer.parseInt(dataArray[1]);
        return receiptService
                .getReceipt(receiptId)
                .filterWhen(receipt -> stringCacheService.storeReceipt(callbackWrapper.userId(), receipt))
                .map(receipt -> EditMessageCaption.builder()
                        .chatId(callbackWrapper.chatId())
                        .messageId(callbackWrapper.messageId())
                        .caption(createIngredientsCaption(receipt.ingredients()))
                        .replyMarkup(KeyboardCreator.createReceiptKeyboard(receiptId, page,
                                Map.of(SHOW_NUTRITION_OVERVIEW, RECEIPT_WITH_NUTRITION_OVERVIEW_PAYLOAD_PREFIX)))
                        .build());

    }

    private String createIngredientsCaption(List<Ingredient> ingredients) {
        return ingredients.stream().map(ingredient -> {
            String quantity = ingredient.quantity() != null ? String.valueOf(ingredient.quantity()) : "";
            String measurement = ingredient.measurement() != null ? ingredient.measurement() : "";
            return ingredient.name() + " - " + quantity + " " + measurement + "\n";
        }).collect(Collectors.joining());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_WITH_INGREDIENTS_PAGE;
    }
}
