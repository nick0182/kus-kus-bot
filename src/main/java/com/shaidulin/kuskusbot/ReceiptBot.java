package com.shaidulin.kuskusbot;

import com.shaidulin.kuskusbot.cache.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.toggle.BareboneToggle;

import static org.telegram.abilitybots.api.objects.Flag.TEXT;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Slf4j
public class ReceiptBot extends AbilityBot {

    private final int creatorId;

    private final ListOperations<Long, Step> listOperations;

    protected ReceiptBot(String botToken, String botUsername, int creatorId, ListOperations<Long, Step> listOperations) {
        super(botToken, botUsername, MapDBContext.offlineInstance(botUsername), new BareboneToggle());
        this.creatorId = creatorId;
        this.listOperations = listOperations;
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
                .flag(upd -> Boolean.FALSE.equals(listOperations.getOperations().hasKey(upd.getMessage().getFrom().getId())))
                .privacy(PUBLIC)
                .locality(USER)
                .input(0)
                .action(this::createNewUser)
                .build();
    }

    private void createNewUser(MessageContext context) {
        long userId = context.user().getId();
        log.debug("Adding new user {} to cache", userId);
        listOperations.rightPushAll(userId, Step.values());
        silent.send("Приветствую тебя " + context.user().getFirstName() + " "
                + context.user().getLastName() + "! Пожалуйста нажми /search чтобы искать рецепт", userId);
    }

    public Ability ingredientSearch() {
        //noinspection unchecked
        return Ability.builder()
                .name("search")
                .info("Ищи рецепты по ингредиентам легко и просто!")
                .flag(upd -> {
                    Step currentStep = listOperations.index(upd.getMessage().getFrom().getId(), 0);
                    return currentStep != null && currentStep.equals(Step.START);
                })
                .privacy(PUBLIC)
                .locality(USER)
                .input(0)
                .action(this::startIngredientSearch)
                .build();
    }

    private void startIngredientSearch(MessageContext context) {
        long userId = context.user().getId();
        log.debug("User {} started ingredient search", userId);
        listOperations.leftPop(userId);
        silent.send("Пожалуйста напиши первый ингредиент", userId);
    }

    public Ability userSentTextMessage() {
        return Ability.builder()
                .name(DEFAULT)
                .flag(TEXT)
                .privacy(PUBLIC)
                .locality(USER)
                .input(0)
                .action(this::reactOnMessage)
                .build();
    }

    private void reactOnMessage(MessageContext context) {
        long userId = context.user().getId();
        Step currentStep = listOperations.leftPop(userId);
        if (currentStep != null) {
            log.debug("New message text: {} from user: {} on step: {}",
                    context.update().getMessage().getText(), userId, currentStep);
            switch (currentStep) {
                case FIRST -> silent.send("Пожалуйста напиши второй ингредиент", userId);
                case SECOND -> silent.send("Пожалуйста напиши третий ингредиент", userId);
                case THIRD -> silent.send("Получены все ингредиенты", userId);
                default -> {
                    log.warn("User {} is not in the context of ingredient search, ignoring message...", userId);
                    listOperations.leftPush(userId, currentStep); // rollback
                }
            }
        } else {
            log.error("User {} should have been populated to cache but hadn't been", userId);
        }
    }
}
