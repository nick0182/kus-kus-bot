package com.shaidulin.kuskusbot.service.cache;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.dto.receipt.Meta;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptValue;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Permission;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface StringCacheService {

    Mono<Boolean> checkPermission(long userId, Permission permission);

    Mono<String> prepareUserCache(long userId);

    Mono<String> startSearch(long userId);

    Mono<Boolean> storeIngredientSuggestions(long userId, TreeSet<IngredientValue> ingredients);

    Mono<String> storeIngredient(long userId, String ingredient);

    Mono<TreeSet<IngredientValue>> getIngredientSuggestions(long userId);

    Mono<List<String>> getIngredients(long userId);

    Mono<String> getImage(String id);

    Mono<String> storeImage(String id, String telegramFileId);

    Mono<Boolean> storeReceiptPresentations(long userId, ReceiptPresentationMatch match);

    Mono<ReceiptPresentationValue> getReceiptPresentation(long userId, int index);

    Mono<Integer> getReceiptPresentationsSize(long userId);

    Mono<Meta> getReceiptPresentationsMeta(long userId);

    Mono<Boolean> storeReceipt(long userId, ReceiptValue receipt);

    Mono<ReceiptValue> getReceipt(long userId);

    Mono<Long> storeSession(long userId, Map<Integer, Data.Session> sessionHash);

    Mono<Data.Session> getSession(long userId, String sessionId);
}
