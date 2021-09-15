package com.shaidulin.kuskusbot;

import com.shaidulin.kuskusbot.cache.CacheService;
import com.shaidulin.kuskusbot.cache.Step;
import com.shaidulin.kuskusbot.dto.IngredientMatch;
import com.shaidulin.kuskusbot.service.ReceiptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.toggle.BareboneToggle;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.objects.Flag.TEXT;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Slf4j
public class ReceiptBot extends AbilityBot {

    private final int creatorId;

    private final CacheService cacheService;

    private final ReceiptService receiptService;

    private static final String SHOW_MORE_SUGGESTIONS = "Ещё";

    private static final String SHOW_LESS_SUGGESTIONS = "Предыдущие";

    public ReceiptBot(String botToken, String botUsername, int creatorId,
                      CacheService cacheService, ReceiptService receiptService) {
        super(botToken, botUsername, MapDBContext.offlineInstance(botUsername), new BareboneToggle());
        this.creatorId = creatorId;
        this.cacheService = cacheService;
        this.receiptService = receiptService;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public Ability newUser() {
        //noinspection unchecked
        return Ability.builder()
                .name("start")
                .info("добро пожаловать!")
                .flag(cacheService::isNewUser)
                .privacy(PUBLIC)
                .locality(USER)
                .input(0)
                .action(this::createNewUser)
                .build();
    }

    private void createNewUser(MessageContext context) {
        long userId = context.user().getId();
        log.debug("Adding new user {} to cache", userId);
        cacheService.registerNewUser(userId);
        silent.send("Приветствую тебя " + context.user().getFirstName() + " "
                + context.user().getLastName() + "! Пожалуйста нажми /search чтобы искать рецепт", userId);
    }

    public Ability ingredientSearch() {
        //noinspection unchecked
        return Ability.builder()
                .name("search")
                .info("Ищи рецепты по ингредиентам легко и просто!")
                .flag(cacheService::isIngredientSearch)
                .privacy(PUBLIC)
                .locality(USER)
                .input(0)
                .action(this::startIngredientSearch)
                .reply(this::reactOnPrompt, TEXT, upd -> !upd.getMessage().isCommand())
                .reply(this::reactOnSuggestion, CALLBACK_QUERY)
                .build();
    }

    private void startIngredientSearch(MessageContext context) {
        long userId = context.user().getId();
        log.debug("User {} started ingredient search", userId);
        cacheService.toNextStep(userId);
        silent.send("Пожалуйста напиши первый ингредиент", userId);
    }

    private void reactOnPrompt(BaseAbilityBot bot, Update update) {
        Message userMessage = update.getMessage();
        long userId = userMessage.getFrom().getId();
        Step currentStep = cacheService.getCurrentStep(userId);
        if (currentStep != null) {
            String ingredientSuggest = userMessage.getText();
            log.debug("New message text: {} from user: {} on step: {}", ingredientSuggest, userId, currentStep);
            switch (currentStep) {
                case FIRST -> receiptService.suggestIngredients(ingredientSuggest)
                        .filter(ingredientMatch -> reactOnEmptyIngredients(ingredientMatch, ingredientSuggest, userId))
                        .subscribe(
                                foundMatch -> {
                                    cacheService.persistIngredientSuggestions(userId, currentStep, foundMatch.getIngredients());
                                    sendIngredientButtons(userId, currentStep);
                                },
                                error -> log.error("Failed to suggest ingredients", error)
                        );
                case SECOND -> silent.send("Пожалуйста напиши третий ингредиент", userId);
                case THIRD -> silent.send("Получены все ингредиенты", userId);
                default -> log.warn("User {} is not in the context of ingredient search, ignoring message...", userId);
            }
        } else {
            log.error("User {} should have been populated to cache but hadn't been", userId);
        }
    }

    private void reactOnSuggestion(BaseAbilityBot bot, Update update) {
        CallbackQuery userCallbackQuery = update.getCallbackQuery();
        long userId = userCallbackQuery.getFrom().getId();
        Step currentStep = cacheService.getCurrentStep(userId);
        Long chatId = userCallbackQuery.getMessage().getChatId();
        Integer messageId = userCallbackQuery.getMessage().getMessageId();
        String suggestion = userCallbackQuery.getData();
        if (suggestion.equals(SHOW_MORE_SUGGESTIONS)) {
            sendNextIngredientButtons(userId, currentStep, chatId, messageId);
        } else if (suggestion.equals(SHOW_LESS_SUGGESTIONS)) {
            sendPreviousIngredientButtons(userId, currentStep, chatId, messageId);
        }
    }

    private boolean reactOnEmptyIngredients(IngredientMatch match, String ingredientSuggest, long userId) {
        boolean isEmpty = match.getIngredients().isEmpty();
        if (isEmpty) {
            log.warn("No ingredients were suggested for input: {} sent by user: {}",
                    ingredientSuggest, userId);
            silent.send("Ничего не нашли \uD83E\uDD14 Попробуй еще раз", userId);
        }
        return !isEmpty;
    }

    private void sendIngredientButtons(long userId, Step currentStep) {
        Set<ZSetOperations.TypedTuple<String>> ingredients = cacheService.getNextIngredientSuggestions(userId, currentStep);
        InlineKeyboardMarkup ingredientKeyboard = setupSuggestionsKeyboard(ingredients, true);

        SendMessage ingredientButtonsMessage = SendMessage
                .builder()
                .text("Вот что удалось найти")
                .chatId(String.valueOf(userId))
                .replyMarkup(ingredientKeyboard)
                .build();
        silent.execute(ingredientButtonsMessage);
    }

    private void sendNextIngredientButtons(long userId, Step currentStep, Long chatId, Integer messageId) {
        Set<ZSetOperations.TypedTuple<String>> ingredients = cacheService.getNextIngredientSuggestions(userId, currentStep);
        boolean isFirstPage = cacheService.getCurrentIngredientSuggestionPage(userId, currentStep) == 0;
        InlineKeyboardMarkup ingredientKeyboard = setupSuggestionsKeyboard(ingredients, isFirstPage);

        EditMessageReplyMarkup nextIngredientButtonsMessage = EditMessageReplyMarkup
                .builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .replyMarkup(ingredientKeyboard)
                .build();
        silent.execute(nextIngredientButtonsMessage);
    }

    private void sendPreviousIngredientButtons(long userId, Step currentStep, Long chatId, Integer messageId) {
        Set<ZSetOperations.TypedTuple<String>> ingredients = cacheService.getPreviousIngredientSuggestions(userId, currentStep);
        boolean isFirstPage = cacheService.getCurrentIngredientSuggestionPage(userId, currentStep) == 0;
        InlineKeyboardMarkup ingredientKeyboard = setupSuggestionsKeyboard(ingredients, isFirstPage);

        EditMessageReplyMarkup previousIngredientButtonsMessage = EditMessageReplyMarkup
                .builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .replyMarkup(ingredientKeyboard)
                .build();
        silent.execute(previousIngredientButtonsMessage);
    }

    private InlineKeyboardMarkup setupSuggestionsKeyboard(Set<ZSetOperations.TypedTuple<String>> ingredients, boolean isFirstPage) {
        boolean hasMore = ingredients.size() > 3;

        InlineKeyboardMarkup ingredientKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> ingredientKeyboardButtons = new ArrayList<>();

        ingredients
                .stream()
                .limit(3)
                .forEach(ingredient ->
                        ingredientKeyboardButtons.add(
                                Collections.singletonList(
                                        new InlineKeyboardButton(
                                                String.join(
                                                        " - ",
                                                        ingredient.getValue(),
                                                        String.valueOf(Objects.requireNonNull(ingredient.getScore()).intValue())),
                                                null,
                                                ingredient.getValue(),
                                                null,
                                                null,
                                                null,
                                                null,
                                                null
                                        ))));

        if (!isFirstPage) {
            ingredientKeyboardButtons.add(
                    Collections.singletonList(
                            new InlineKeyboardButton(
                                    SHOW_LESS_SUGGESTIONS,
                                    null,
                                    SHOW_LESS_SUGGESTIONS, null,
                                    null,
                                    null,
                                    null,
                                    null)));
        }

        if (hasMore) {
            ingredientKeyboardButtons.add(
                    Collections.singletonList(
                            new InlineKeyboardButton(
                                    SHOW_MORE_SUGGESTIONS,
                                    null,
                                    SHOW_MORE_SUGGESTIONS, null,
                                    null,
                                    null,
                                    null,
                                    null)));
        }

        ingredientKeyboard.setKeyboard(ingredientKeyboardButtons);
        return ingredientKeyboard;
    }

}
