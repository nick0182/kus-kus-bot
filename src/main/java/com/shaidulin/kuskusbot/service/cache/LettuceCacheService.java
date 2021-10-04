package com.shaidulin.kuskusbot.service.cache;

import com.shaidulin.kuskusbot.dto.IngredientValue;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public interface LettuceCacheService {

    Mono<Boolean> flushUserCache(String userId);

    Mono<String> startSearch(String userId);

    Mono<Step> getIngredientSearchStep(String userId);

    Mono<Boolean> storeIngredientSuggestions(String userId, Step searchStep, Set<IngredientValue> ingredients);

    Mono<Boolean> storeIngredient(String userId, String ingredient);

    Mono<TreeSet<IngredientValue>> getIngredientSuggestions(String userId, Step searchStep);

    Mono<List<String>> getIngredients(String userId);
}
