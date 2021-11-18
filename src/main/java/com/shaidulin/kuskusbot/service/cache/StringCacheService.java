package com.shaidulin.kuskusbot.service.cache;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.update.Permission;
import com.shaidulin.kuskusbot.util.ImageType;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public interface StringCacheService {

    Mono<Boolean> checkPermission(String userId, Permission permission);

    Mono<Boolean> prepareUserCache(String userId);

    Mono<String> startSearch(String userId);

    Mono<Boolean> storeIngredientSuggestions(String userId, Set<IngredientValue> ingredients);

    Mono<Boolean> storeIngredient(String userId, String ingredient);

    Mono<TreeSet<IngredientValue>> getIngredientSuggestions(String userId);

    Mono<List<String>> getIngredients(String userId);

    Mono<String> getImage(String id, ImageType type);
}