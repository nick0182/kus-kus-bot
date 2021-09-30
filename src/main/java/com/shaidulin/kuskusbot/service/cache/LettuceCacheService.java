package com.shaidulin.kuskusbot.service.cache;

import com.shaidulin.kuskusbot.dto.IngredientValue;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.TreeSet;

public interface LettuceCacheService {

    Mono<Boolean> flushUserCache(String userId);

    Mono<String> startSearch(String userId);

    Mono<Step> getIngredientSearchStep(String userId);

    Mono<Boolean> storeIngredients(String userId, Step searchStep, Set<IngredientValue> ingredients);

    Mono<TreeSet<IngredientValue>> getNextIngredients(String userId, Step searchStep, long page);
}
