package com.shaidulin.kuskusbot.service.cache.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaidulin.kuskusbot.dto.receipt.Meta;
import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptValue;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Permission;
import com.shaidulin.kuskusbot.util.ImageType;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@SuppressWarnings("CallingSubscribeInNonBlockingScope")
public record StringCacheServiceImpl(RedisReactiveCommands<String, String> redisReactiveCommands,
                                     ObjectMapper objectMapper) implements StringCacheService {

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
        String[] keysToDelete = new String[]{
                composeKey(userId, "permissions"),
                composeKey(userId, "suggestions"),
                composeKey(userId, "ingredients"),
                composeReceiptPresentationsKey(userId),
                composeReceiptPresentationsMetaKey(userId)
        };

        return redisReactiveCommands
                .multi()
                .doOnNext(ignored -> redisReactiveCommands.del(keysToDelete).subscribe())
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
                .doOnNext(ignored -> redisReactiveCommands.del(ingredientSuggestionsKey).subscribe())
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
                .doOnNext(ignored ->
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

    @Override
    public Mono<Boolean> storeReceiptPresentations(String userId, ReceiptPresentationMatch match) {
        String receiptPresentationsKey = composeReceiptPresentationsKey(userId);
        return redisReactiveCommands
                .multi()
                .doOnNext(ignored -> redisReactiveCommands.del(receiptPresentationsKey).subscribe())
                .doOnSuccess(ignored ->
                        redisReactiveCommands
                                .rpush(receiptPresentationsKey,
                                        match.receipts().stream().map(this::serializeToCache).toArray(String[]::new))
                                .subscribe())
                .doOnSuccess(ignored ->
                        redisReactiveCommands
                                .set(composeReceiptPresentationsMetaKey(userId), serializeToCache(match.meta()))
                                .subscribe())
                .then(redisReactiveCommands.exec())
                .map(objects -> !objects.wasDiscarded());
    }

    @Override
    public Mono<ReceiptPresentationValue> getReceiptPresentation(String userId, int index) {
        return redisReactiveCommands
                .lindex(composeReceiptPresentationsKey(userId), index)
                .map(cacheString -> deserializeFromCache(cacheString, ReceiptPresentationValue.class));
    }

    @Override
    public Mono<Integer> getReceiptPresentationsSize(String userId) {
        return redisReactiveCommands.llen(composeReceiptPresentationsKey(userId)).map(Long::intValue);
    }

    @Override
    public Mono<Meta> getReceiptPresentationsMeta(String userId) {
        return redisReactiveCommands
                .get(composeReceiptPresentationsMetaKey(userId))
                .map(cacheString -> deserializeFromCache(cacheString, Meta.class));
    }

    @Override
    public Mono<Boolean> storeReceipt(String userId, ReceiptValue receipt) {
        return redisReactiveCommands
                .set(composeKey(userId, "receipt"), serializeToCache(receipt))
                .map(response -> response.equals("OK"));
    }

    @SneakyThrows
    private <T> String serializeToCache(T value) {
        return objectMapper.writeValueAsString(value);
    }

    @SneakyThrows
    private <T> T deserializeFromCache(String cacheString, Class<T> clazz) {
        return objectMapper.readValue(cacheString, clazz);
    }

    private Mono<String> modifyPermission(String userId, String permissionString) {
        return redisReactiveCommands.set(composeKey(userId, "permissions"), permissionString);
    }

    private String composeKey(String userId, String suffix) {
        return String.join(":", userId, suffix);
    }

    private String composeReceiptPresentationsKey(String userId) {
        return String.join(":", userId, "receipts", "presentations");
    }

    private String composeReceiptPresentationsMetaKey(String userId) {
        return String.join(":", userId, "receipts", "presentations", "meta");
    }

    private String composeMainImageKey(String id) {
        return String.join(":", "image", id);
    }
}
