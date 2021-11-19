package com.shaidulin.kuskusbot.service.cache.impl;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Permission;
import com.shaidulin.kuskusbot.util.ImageType;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public record StringCacheServiceImpl(RedisReactiveCommands<String, String> redisReactiveCommands)
        implements StringCacheService {

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
        return redisReactiveCommands
                .multi()
                .doOnSuccess(ignored -> redisReactiveCommands.del(ingredientSuggestionsKey).subscribe())
                .doOnSuccess(ignored ->
                        redisReactiveCommands
                                .zadd(ingredientSuggestionsKey,
                                        ingredients
                                                .stream()
                                                .map(ingredient -> ScoredValue.just(ingredient.count(), ingredient.name()))
                                                .<ScoredValue<String>>toArray(ScoredValue[]::new))
                                .subscribe())
                .doOnSuccess(ignored -> modifyPermission(userId, "01").subscribe())
                .then(redisReactiveCommands.exec())
                .map(transactionResult -> !transactionResult.wasDiscarded());
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

    @Override
    public Mono<String> getImage(String id, ImageType type) {
        String key = type.equals(ImageType.MAIN) ? composeMainImageKey(id) : null;
        return redisReactiveCommands.get(key);
    }

    @Override
    public Mono<String> storeImage(String id, ImageType type, String telegramFileId) {
        String key = type.equals(ImageType.MAIN) ? composeMainImageKey(id) : null;
        return redisReactiveCommands.set(key, telegramFileId);
    }

    private Mono<String> modifyPermission(String userId, String permissionString) {
        return redisReactiveCommands.set(composeKey(userId, "permissions"), permissionString);
    }

    private String composeKey(String userId, String suffix) {
        return String.join(":", userId, suffix);
    }

    private String composeMainImageKey(String id) {
        return String.join(":", "image", id);
    }
}
