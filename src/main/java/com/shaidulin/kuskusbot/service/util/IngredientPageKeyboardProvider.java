package com.shaidulin.kuskusbot.service.util;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

import java.util.TreeSet;

public interface IngredientPageKeyboardProvider {
    Mono<InlineKeyboardMarkup> compileKeyboard(long userId, int page, TreeSet<IngredientValue> ingredients);
}
