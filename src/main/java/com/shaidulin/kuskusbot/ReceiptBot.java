package com.shaidulin.kuskusbot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.toggle.BareboneToggle;
import org.telegram.abilitybots.api.util.AbilityExtension;

@Slf4j
public class ReceiptBot extends AbilityBot {

    private final int creatorId;

    public ReceiptBot(String botToken, String botUsername, int creatorId,
                      AbilityExtension newUserAbility, AbilityExtension ingredientSearchAbility) {
        super(botToken, botUsername, MapDBContext.offlineInstance(botUsername), new BareboneToggle());
        this.creatorId = creatorId;
        addExtensions(newUserAbility, ingredientSearchAbility);
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

}
