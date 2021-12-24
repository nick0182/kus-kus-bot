package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.receipt.Nutrition;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import com.shaidulin.kuskusbot.util.KeyboardCreator;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shaidulin.kuskusbot.util.ButtonConstants.RECEIPT_WITH_INGREDIENTS_PAGE_PAYLOAD_PREFIX;
import static com.shaidulin.kuskusbot.util.ButtonConstants.SHOW_INGREDIENTS;

public record ReceiptNutritionOverviewPageBotProcessor(StringCacheService stringCacheService)
        implements BaseBotProcessor {

    private static final String PROTEIN_EMOJI = "\uD83E\uDD69";

    private static final String FAT_EMOJI = "\uD83E\uDDC8";

    private static final String CARBOHYDRATE_EMOJI = "\uD83C\uDF5E";

    private static final String CALORIES_EMOJI = "\uD83D\uDCAA";

    @Override
    public Mono<? extends BotApiMethod<?>> process(Update update) {
        CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
        String[] dataArray = callbackWrapper.data().split("_");
        int receiptId = Integer.parseInt(dataArray[0]);
        int page = Integer.parseInt(dataArray[1]);
        return stringCacheService
                .getReceipt(callbackWrapper.userId())
                .map(receipt -> EditMessageCaption.builder()
                        .chatId(callbackWrapper.chatId())
                        .messageId(callbackWrapper.messageId())
                        .caption(createNutritionOverviewCaption(receipt.nutritions()))
                        .replyMarkup(KeyboardCreator.createReceiptKeyboard(receiptId, page,
                                Map.of(SHOW_INGREDIENTS, RECEIPT_WITH_INGREDIENTS_PAGE_PAYLOAD_PREFIX)))
                        .parseMode("HTML")
                        .build());
    }

    private String createNutritionOverviewCaption(List<Nutrition> nutritionOverview) {
        return nutritionOverview.stream().map(nutrition -> {
            String portionHeader = "<u><b>" + nutrition.name().getEmoji() + " " + nutrition.name().getName() + ":</b></u>";
            String proteinLine = "<i>" + PROTEIN_EMOJI + " Белки " + nutrition.protein() + " г</i>";
            String fatLine = "<i>" + FAT_EMOJI + " Жиры " + nutrition.fat() + " г</i>";
            String carbohydrateLine = "<i>" + CARBOHYDRATE_EMOJI + " Углеводы " + nutrition.carbohydrate() + " г</i>";
            String caloriesLine = "<i>" + CALORIES_EMOJI + " Калорийность " + nutrition.calories() + " ккал</i>";
            return String.join("\n", portionHeader, proteinLine, fatLine, carbohydrateLine, caloriesLine) + "\n\n";
        }).collect(Collectors.joining());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_WITH_NUTRITION_OVERVIEW_PAGE;
    }
}
