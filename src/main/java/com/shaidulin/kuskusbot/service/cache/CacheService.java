package com.shaidulin.kuskusbot.service.cache;

import com.shaidulin.kuskusbot.dto.IngredientValue;
import org.springframework.data.redis.core.ZSetOperations;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

public interface CacheService {

    boolean isNewUser(Update update);

    void registerNewUser(long userId);

    boolean isIngredientSearch(Update update);

    Step getCurrentStep(long userId);

    Step toNextStep(long userId);

    Step toPreviousStep(long userId);

    void persistIngredientSuggestions(long userId, Step currentStep, Set<IngredientValue> ingredients);

    Set<ZSetOperations.TypedTuple<String>> getNextIngredientSuggestions(long userId, Step currentStep);

    Set<ZSetOperations.TypedTuple<String>> getPreviousIngredientSuggestions(long userId, Step currentStep);

    long getCurrentIngredientSuggestionPage(long userId, Step currentStep);
}
