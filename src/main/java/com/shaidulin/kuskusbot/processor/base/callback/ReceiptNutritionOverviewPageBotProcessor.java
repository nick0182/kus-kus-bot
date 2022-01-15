package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.receipt.Nutrition;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.ButtonConstants;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Shows receipt nutrition overview
 */
public record ReceiptNutritionOverviewPageBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    private static final String PROTEIN_EMOJI = "\uD83E\uDD69";

    private static final String FAT_EMOJI = "\uD83E\uDDC8";

    private static final String CARBOHYDRATE_EMOJI = "\uD83C\uDF5E";

    private static final String CALORIES_EMOJI = "\uD83D\uDCAA";

    @Override
    public Mono<EditMessageCaption> process(Data data) {
        return cacheService
                .getReceipt(data.getUserId())
                .zipWith(compileButton(data, Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE))
                .zipWith(compileButton(data, Data.Action.SHOW_RECEIPT_INGREDIENTS_PAGE))
                .zipWith(compileButton(data, Data.Action.SHOW_STEP_PAGE),
                        (tuple2OfTuple2, stepButtonKey) -> Tuples.of(
                                tuple2OfTuple2.getT1().getT1().nutritions(),
                                DynamicKeyboard.createReceiptKeyboard(
                                        tuple2OfTuple2.getT1().getT2(),
                                        Map.of(ButtonConstants.SHOW_INGREDIENTS_OVERVIEW, tuple2OfTuple2.getT2(),
                                                ButtonConstants.SHOW_STEPS, stepButtonKey))))
                .map(tuple2 -> EditMessageCaption.builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .caption(createNutritionOverviewCaption(tuple2.getT1()))
                        .replyMarkup(tuple2.getT2())
                        .parseMode("HTML")
                        .build());
    }

    private Mono<UUID> compileButton(Data data, Data.Action action) {
        Data.Session currentSession = data.getSession();
        UUID key = UUID.randomUUID();
        Data.Session session = Data.Session
                .builder()
                .action(action)
                .receiptSortType(currentSession.getReceiptSortType())
                .receiptId(currentSession.getReceiptId())
                .currentReceiptPage(currentSession.getCurrentReceiptPage())
                .build();
        return cacheService
                .storeSession(data.getUserId(), key, session)
                .map(ignored -> key);
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
