package com.shaidulin.kuskusbot.service.cache;

import com.shaidulin.kuskusbot.dto.receipt.Meta;
import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptValue;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Permission;
import com.shaidulin.kuskusbot.util.ImageType;
import reactor.core.publisher.Mono;

import java.util.*;

public interface StringCacheService {

    Mono<Boolean> checkPermission(String userId, Permission permission);

    Mono<Boolean> prepareUserCache(String userId);

    Mono<String> startSearch(String userId);

    Mono<Boolean> storeIngredientSuggestions(String userId, TreeSet<IngredientValue> ingredients);

    Mono<Boolean> storeIngredient(String userId, String ingredient);

    Mono<TreeSet<IngredientValue>> getIngredientSuggestions(String userId, int from);

    Mono<List<String>> getIngredients(String userId);

    Mono<String> getImage(String id, ImageType type);

    Mono<String> storeImage(String id, ImageType type, String telegramFileId);

    Mono<Boolean> storeReceiptPresentations(String userId, ReceiptPresentationMatch match);

    Mono<ReceiptPresentationValue> getReceiptPresentation(String userId, int index);

    Mono<Integer> getReceiptPresentationsSize(String userId);

    Mono<Meta> getReceiptPresentationsMeta(String userId);

    Mono<Boolean> storeReceipt(String userId, ReceiptValue receipt);

    Mono<ReceiptValue> getReceipt(String userId);

    Mono<Long> storeSession(String userId, Map<Integer, Data.Session> sessionHash);

    Mono<Data.Session> getSession(String userId, String sessionId);
}
