package com.shaidulin.kuskusbot.cache.impl;

import com.shaidulin.kuskusbot.cache.CacheService;
import com.shaidulin.kuskusbot.cache.Step;
import com.shaidulin.kuskusbot.dto.IngredientValue;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;
import java.util.Set;

@SuppressWarnings("ClassCanBeRecord")
@AllArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final ListOperations<Long, Step> stepsRedisTemplate;

    private final ZSetOperations<String, String> ingredientsRedisTemplate;

    private final ValueOperations<String, Integer> ingredientsIndexRedisTemplate;

    @Override
    public boolean isNewUser(Update update) {
        return Boolean.FALSE.equals(stepsRedisTemplate.getOperations().hasKey(update.getMessage().getFrom().getId()));
    }

    @Override
    public void registerNewUser(long userId) {
        stepsRedisTemplate.rightPushAll(userId, Step.values());
    }

    @Override
    public boolean isIngredientSearch(Update update) {
        Step currentStep = stepsRedisTemplate.index(update.getMessage().getFrom().getId(), 0);
        return currentStep != null && currentStep.equals(Step.START);
    }

    @Override
    public Step toNextStep(long userId) {
        return stepsRedisTemplate.move(ListOperations.MoveFrom.fromHead(userId), ListOperations.MoveTo.toTail(userId));
    }

    @Override
    public Step toPreviousStep(long userId) {
        return stepsRedisTemplate.move(ListOperations.MoveFrom.fromTail(userId), ListOperations.MoveTo.toHead(userId));
    }

    @Override
    public void persistIngredientSuggestions(long userId, Step currentStep, Set<IngredientValue> ingredients) {
        stepsRedisTemplate.getOperations().executePipelined(new SessionCallback<>() {
            @SuppressWarnings("unchecked")
            @Override
            public Object execute(@NotNull RedisOperations operations) throws DataAccessException {
                operations.multi(); // start transaction
                String[] ingredientKeys = composeIngredientKeys(userId, currentStep);
                operations.opsForZSet().removeRange(ingredientKeys[0], 0, -1);
                ingredients.forEach(ingredient ->
                        operations.opsForZSet().add(ingredientKeys[0], ingredient.getName(), ingredient.getCount()));

                operations.opsForValue().set(ingredientKeys[1], 0); // point to first ingredient

                operations.exec(); // commit transaction
                return null;
            }
        });
    }

    @Override
    public Set<ZSetOperations.TypedTuple<String>> getNextIngredientSuggestions(long userId, Step currentStep) {
        String[] ingredientKeys = composeIngredientKeys(userId, currentStep);
        long currentIndex = Objects.requireNonNull(ingredientsIndexRedisTemplate.increment(ingredientKeys[1], 3)) - 3;
        return ingredientsRedisTemplate.reverseRangeWithScores(ingredientKeys[0], currentIndex, -1);
    }

    private String[] composeIngredientKeys(long userId, Step currentStep) {
        String ingredientsKey = String.join(":", String.valueOf(userId), currentStep.toString(), "ingredients");
        String ingredientsIndexKey = String.join(":", ingredientsKey, "index");
        return new String[]{ingredientsKey, ingredientsIndexKey};
    }
}
