package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.receipt.Nutrition;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptValue;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.ReceiptKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.Emoji;
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
            String proteinLine = "<i>" + Emoji.PROTEIN + " Белки " + nutrition.protein() + " г</i>";
            String fatLine = "<i>" + Emoji.FAT + " Жиры " + nutrition.fat() + " г</i>";
            String carbohydrateLine = "<i>" + Emoji.CARBOHYDRATE + " Углеводы " + nutrition.carbohydrate() + " г</i>";
            String caloriesLine = "<i>" + Emoji.CALORIES + " Калорийность " + nutrition.calories() + " ккал</i>";
            return String.join("\n", portionHeader, proteinLine, fatLine, carbohydrateLine, caloriesLine) + "\n\n";
        }).collect(Collectors.joining());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_WITH_NUTRITION_OVERVIEW_PAGE;
    }
}
