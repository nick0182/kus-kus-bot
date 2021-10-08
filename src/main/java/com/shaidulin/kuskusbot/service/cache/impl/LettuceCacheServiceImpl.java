package com.shaidulin.kuskusbot.service.cache.impl;

import com.shaidulin.kuskusbot.dto.IngredientValue;
import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.update.Permission;
import io.lettuce.core.*;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
@AllArgsConstructor
public class LettuceCacheServiceImpl implements LettuceCacheService {

    private final RedisReactiveCommands<String, String> redisReactiveCommands;

    @Override
    public Mono<Boolean> checkPermission(String userId, Permission permission) {
        return redisReactiveCommands
                .get(composeKey(userId, "permissions"))
                .map(String::toCharArray)
                .map(permissionsArray -> permissionsArray[permission.getIndex()] == '1')
                .filter(Boolean.TRUE::equals);
    }

    @Override
    public Mono<Boolean> prepareUserCache(String userId) {
        return redisReactiveCommands
                .scan(ScanCursor.INITIAL, ScanArgs.Builder.matches(userId + "*").limit(10))
                .doOnSuccess(ignored -> redisReactiveCommands
                        .multi()
                        .subscribe())
                .filter(keyScanCursor -> !CollectionUtils.isEmpty(keyScanCursor.getKeys()))
                .map(KeyScanCursor::getKeys)
                .doOnNext(keys -> redisReactiveCommands
                        .del(keys.toArray(String[]::new))
                        .subscribe())
                .doOnSuccess(ignored -> modifyPermission(userId, "01").subscribe())
                .then(redisReactiveCommands.exec())
                .map(objects -> !objects.wasDiscarded())
                .filter(Boolean.TRUE::equals);
    }

    @Override
    public Mono<String> startSearch(String userId) {
        return modifyPermission(userId, "10");
    }

    @Override
    public Mono<Boolean> storeIngredientSuggestions(String userId, Set<IngredientValue> ingredients) {
        String ingredientSuggestionsKey = composeKey(userId, "suggestions");
        //noinspection RedundantCast
        return redisReactiveCommands
                .multi()
                .doOnSuccess(ignored -> redisReactiveCommands.del(ingredientSuggestionsKey).subscribe())
                .doOnSuccess(ignored ->
                        redisReactiveCommands
                                .zadd(ingredientSuggestionsKey,
                                        ingredients
                                                .stream()
                                                .map(ingredient -> ScoredValue.just(ingredient.getCount(), ingredient.getName()))
                                                .<ScoredValue<String>>toArray(ScoredValue[]::new))
                                .subscribe())
                .doOnSuccess(ignored -> modifyPermission(userId, "01").subscribe())
                .then(redisReactiveCommands.exec())
                .map(transactionResult -> !((TransactionResult) transactionResult).wasDiscarded());
    }

    @Override
    public Mono<Boolean> storeIngredient(String userId, String ingredient) {
        return redisReactiveCommands
                .multi()
                .doOnSuccess(ignored ->
                        redisReactiveCommands
                                .rpush(composeKey(userId, "ingredients"), ingredient)
                                .subscribe())
                .doOnSuccess(ignored -> modifyPermission(userId, "01").subscribe())
                .then(redisReactiveCommands.exec())
                .map(objects -> !objects.wasDiscarded())
                .filter(Boolean.TRUE::equals);
    }

    @Override
    public Mono<TreeSet<IngredientValue>> getIngredientSuggestions(String userId) {
        return redisReactiveCommands
                .zrevrangeWithScores(composeKey(userId, "suggestions"), 0, -1)
                .collect(
                        Collectors.mapping(
                                scoredValue -> new IngredientValue(scoredValue.getValue(), (int) scoredValue.getScore()),
                                Collectors.toCollection(TreeSet::new)));
    }

    @Override
    public Mono<List<String>> getIngredients(String userId) {
        return redisReactiveCommands
                .lrange(composeKey(userId, "ingredients"), 0, -1)
                .collectList()
                .defaultIfEmpty(Collections.emptyList());
    }

    private Mono<String> modifyPermission(String userId, String permissionString) {
        return redisReactiveCommands.set(composeKey(userId, "permissions"), permissionString);
    }

    private String composeKey(String userId, String suffix) {
        return String.join(":", userId, suffix);
    }
}
