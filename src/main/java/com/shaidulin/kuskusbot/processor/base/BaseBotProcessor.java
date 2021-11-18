package com.shaidulin.kuskusbot.processor.base;

import com.shaidulin.kuskusbot.processor.TypeIdentifier;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public interface BaseBotProcessor extends TypeIdentifier {

    Mono<? extends BotApiMethod<?>> process(Update update);
}