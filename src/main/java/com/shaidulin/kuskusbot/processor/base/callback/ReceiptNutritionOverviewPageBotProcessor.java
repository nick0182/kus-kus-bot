package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.receipt.Nutrition;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptValue;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.ReceiptKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.marker.Markers.append;

/**
 * Shows receipt nutrition overview
 */
@Slf4j
public record ReceiptNutritionOverviewPageBotProcessor(StringCacheService cacheService,
                                                       ReceiptKeyboardProvider keyboardProvider) implements BaseBotProcessor {

    private static final String PROTEIN_EMOJI = "\uD83E\uDD69";

    private static final String FAT_EMOJI = "\uD83E\uDDC8";

    private static final String CARBOHYDRATE_EMOJI = "\uD83C\uDF5E";

    private static final String CALORIES_EMOJI = "\uD83D\uDCAA";

    @Override
    public Mono<EditMessageCaption> process(Data data) {
        return getReceiptFromCache(data.getUserId())
                .zipWith(keyboardProvider.compileKeyboard(data, Data.Action.SHOW_RECEIPT_INGREDIENTS_PAGE, "Ингредиенты"))
                .map(tuple2 -> EditMessageCaption.builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .caption(createNutritionOverviewCaption(tuple2.getT1().nutritions()))
                        .replyMarkup(tuple2.getT2())
                        .parseMode("HTML")
                        .build());
    }

    private Mono<ReceiptValue> getReceiptFromCache(String userId) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Getting receipt from cache");
        }
        return cacheService.getReceipt(userId);
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
