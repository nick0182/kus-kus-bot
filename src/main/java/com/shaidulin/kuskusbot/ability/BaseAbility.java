package com.shaidulin.kuskusbot.ability;

import com.shaidulin.kuskusbot.service.cache.CacheService;
import lombok.AllArgsConstructor;
import org.telegram.abilitybots.api.util.AbilityExtension;

@AllArgsConstructor
public abstract class BaseAbility implements AbilityExtension {

    protected final CacheService cacheService;
}
