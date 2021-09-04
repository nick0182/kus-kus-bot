package com.shaidulin.kuskusbot;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

public class ReceiptBot extends AbilityBot {

    enum Step {
        START, FIRST, SECOND, THIRD
    }

    private final int creatorId;

    private final Map<Long, Step> userSteps;

    protected ReceiptBot(String botToken, String botUsername, int creatorId) {
        super(botToken, botUsername);
        this.creatorId = creatorId;
        this.userSteps = new ConcurrentHashMap<>();
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public Ability userSentTextMessage() {
        //noinspection unchecked
        return Ability.builder()
                .name(DEFAULT)
                .flag(upd -> upd.getMessage().isUserMessage() && !upd.getMessage().isCommand())
                .privacy(PUBLIC)
                .locality(USER)
                .input(0)
                .action(this::reactOnMessage)
                .build();
    }

    private void reactOnMessage(MessageContext context) {
        Long chatId = context.chatId();
        Step step = Optional.ofNullable(userSteps.putIfAbsent(chatId, Step.START)).orElse(Step.START);
        switch (step) {
            case START -> silent.send("Пожалуйста напишите первый ингредиент", chatId);
            case FIRST -> silent.send("Пожалуйста напишите второй ингредиент", chatId);
            case SECOND -> silent.send("Пожалуйста напишите третий ингредиент", chatId);
            case THIRD -> silent.send("Получены все ингредиенты", chatId);
        }
    }


//    public Ability showPossibleIngredients() {
//        return Ability
//                .builder()
//                .name("find")
//                .info("start looking for possible ingredients")
//                .input(0)
//                .locality(USER)
//                .privacy(ADMIN)
//                .action(ctx -> {
//                    Long chatId = ctx.chatId();
//                    cache.put(chatId, Step.START);
//                    silent.send("Пожалуйста напишите первый ингредиент", chatId);
//                })
//                .build();
//    }
//
//    public Reply gotFirstIngredient() {
//        return Reply.of((bot, upd) -> {
//            Long chatId = upd.getMessage().getChatId();
//            cache.put(chatId, Step.FIRST);
//            silent.send("Пожалуйста напишите второй ингредиент", chatId);
//        }, upd -> {
//            Long chatId = upd.getMessage().getChatId();
//            return upd.getMessage().isUserMessage() && cache.containsKey(chatId) && cache.get(chatId).equals(Step.START);
//        });
//    }
//
//    public Reply gotSecondIngredient() {
//        return Reply.of((bot, upd) -> {
//            Long chatId = upd.getMessage().getChatId();
//            cache.put(chatId, Step.SECOND);
//            silent.send("Пожалуйста напишите третий ингредиент", chatId);
//        }, upd -> {
//            Long chatId = upd.getMessage().getChatId();
//            return upd.getMessage().isUserMessage() && cache.containsKey(chatId) && cache.get(chatId).equals(Step.FIRST);
//        });
//    }
//
//    public Reply gotThirdIngredient() {
//        return Reply.of((bot, upd) -> {
//            Long chatId = upd.getMessage().getChatId();
//            cache.put(chatId, Step.THIRD);
//            silent.send("Получены все ингредиенты", chatId);
//        }, upd -> {
//            Long chatId = upd.getMessage().getChatId();
//            return upd.getMessage().isUserMessage() && cache.containsKey(chatId) && cache.get(chatId).equals(Step.SECOND);
//        });
//    }

//    public ReplyFlow ingredientFlow() {
//        return ReplyFlow.builder(db, 23432132)
//                .action((bot, upd) -> silent.send("Пожалуйста напишите первый ингредиент", upd.getMessage().getChatId()))
//                .onlyIf(update -> update.getMessage().getText().equals("старт!"))
//                .next(gotFirstIngredient())
//                .build();
//    }
//
//    private ReplyFlow gotFirstIngredient() {
//        return ReplyFlow.builder(db, 23432132)
//                .action((bot, upd) -> silent.send("Пожалуйста напишите второй ингредиент", upd.getMessage().getChatId()))
//                .onlyIf(update -> update.getMessage().isUserMessage())
//                .next(gotSecondIngredient())
//                .build();
//    }
//
//    private ReplyFlow gotSecondIngredient() {
//        return ReplyFlow.builder(db, 23432132)
//                .action((bot, upd) -> silent.send("Пожалуйста напишите третий ингредиент", upd.getMessage().getChatId()))
//                .onlyIf(update -> update.getMessage().isUserMessage())
//                .next(gotThirdIngredient())
//                .build();
//    }
//
//    private ReplyFlow gotThirdIngredient() {
//        return ReplyFlow.builder(db, 23432132)
//                .action((bot, upd) -> silent.send("Получены все ингредиенты", upd.getMessage().getChatId()))
//                .onlyIf(update -> update.getMessage().isUserMessage())
//                .build();
//    }

//    private ReplyFlow test() {
//        return ReplyFlow.builder(db)
//                .action(upd -> silent.send("Command me to go left or right!", upd.getMessage().getChatId()))
//                .onlyIf(hasMessageWith("/find"))
//                .next(Reply.of(upd ->
//                                silent.send("Sir, I have gone left.", upd.getMessage().getChatId()),
//                        hasMessageWith("left")))
//                .next(Reply.of(upd ->
//                                silent.send("Sir, I have gone right.", upd.getMessage().getChatId()),
//                        hasMessageWith("right")))
//                .build();
//    }
//
//    private Predicate<Update> hasMessageWith(String msg) {
//        return upd -> upd.getMessage().getText().equalsIgnoreCase(msg);
//    }
}
