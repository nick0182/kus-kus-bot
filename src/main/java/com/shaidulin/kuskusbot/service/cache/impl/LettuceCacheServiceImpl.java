package com.shaidulin.kuskusbot.service.cache.impl;

import com.shaidulin.kuskusbot.dto.IngredientValue;
import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.service.cache.Step;
import io.lettuce.core.*;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
@AllArgsConstructor
public class LettuceCacheServiceImpl implements LettuceCacheService {

    private final RedisReactiveCommands<String, String> redisReactiveCommands;

    @Override
    public Mono<Boolean> flushUserCache(String userId) {

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
                .doOnSuccess(ignored -> redisReactiveCommands
                        .rpush(userId, Arrays.stream(Step.values()).map(Enum::toString).toArray(String[]::new))
                        .subscribe())
                .then(redisReactiveCommands.exec())
                .map(objects -> !objects.wasDiscarded());
    }

    @Override
    public Mono<String> startSearch(String userId) {
        return redisReactiveCommands
                .lindex(userId, 0)
                .filter(currentStep -> Step.valueOf(currentStep).equals(Step.START))
                .flatMap(ignored -> toNextStep(userId));
    }

    @Override
    public Mono<Step> getIngredientSearchStep(String userId) {
        return redisReactiveCommands
                .lindex(userId, 0)
                .map(Step::valueOf)
                .filter(currentStep ->
                        switch (currentStep) {
                            case FIRST, SECOND, THIRD -> true;
                            default -> false;
                        }
                );
    }

    @Override
    public Mono<Boolean> storeIngredientSuggestions(String userId, Step searchStep, Set<IngredientValue> ingredients) {
        //noinspection RedundantCast
        return redisReactiveCommands
                .multi()
                .doOnSuccess(ignored ->
                        redisReactiveCommands
                                .zadd(composeIngredientSuggestionsKey(userId, searchStep),
                                        ingredients
                                                .stream()
                                                .map(ingredient -> ScoredValue.just(ingredient.getCount(), ingredient.getName()))
                                                .<ScoredValue<String>>toArray(ScoredValue[]::new))
                                .subscribe())
                .then(redisReactiveCommands.exec())
                .map(transactionResult -> !((TransactionResult) transactionResult).wasDiscarded());
    }

    @Override
    public Mono<Boolean> storeIngredient(String userId, String ingredient) {
        return redisReactiveCommands
                .multi()
                .doOnSuccess(ignored ->
                        redisReactiveCommands.lpush(composeIngredientsKey(userId), ingredient).subscribe())
                .doOnSuccess(ignored -> toNextStep(userId).subscribe())
                .then(redisReactiveCommands.exec())
                .map(objects -> !objects.wasDiscarded());
    }

    @Override
    public Mono<TreeSet<IngredientValue>> getIngredientSuggestions(String userId, Step searchStep) {
        return redisReactiveCommands
                .zrevrangeWithScores(composeIngredientSuggestionsKey(userId, searchStep), 0, -1)
                .collect(
                        Collectors.mapping(
                                scoredValue -> new IngredientValue(scoredValue.getValue(), (int) scoredValue.getScore()),
                                Collectors.toCollection(TreeSet::new)));
    }

    @Override
    public Mono<List<String>> getIngredients(String userId) {
        return redisReactiveCommands
                .lrange(composeIngredientsKey(userId), 0, -1)
                .collectList()
                .defaultIfEmpty(Collections.emptyList());
    }

    private String composeIngredientSuggestionsKey(String userId, Step searchStep) {
        return String.join(":", userId, searchStep.toString(), "ingredient", "suggestions");
    }

    private String composeIngredientsKey(String userId) {
        return String.join(":", userId, "ingredients");
    }

    private Mono<String> toNextStep(String userId) {
        return redisReactiveCommands.lmove(userId, userId, LMoveArgs.Builder.leftRight());
    }
}
