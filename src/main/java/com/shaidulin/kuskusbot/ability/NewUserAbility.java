package com.shaidulin.kuskusbot.ability;

import com.shaidulin.kuskusbot.service.cache.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Slf4j
public class NewUserAbility extends BaseAbility {

    public NewUserAbility(CacheService cacheService) {
        super(cacheService);
    }

    @SuppressWarnings({"unused", "unchecked"})
    public Ability newUser() {
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
        context.bot().silent().send("Приветствую тебя " + context.user().getFirstName() + " "
                + context.user().getLastName() + "! Пожалуйста нажми /search чтобы искать рецепт", userId);
    }
}
