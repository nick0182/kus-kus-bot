package com.shaidulin.kuskusbot.processor;

import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.update.UpdateKey;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public abstract class BotProcessor {

    protected final LettuceCacheService lettuceCacheService;

    protected BotProcessor(LettuceCacheService lettuceCacheService) {
        this.lettuceCacheService = lettuceCacheService;
    }

    public abstract Mono<? extends BotApiMethod<?>> process(Update update);

    public abstract UpdateKey getKey();
}