package com.shaidulin.kuskusbot.processor.base;

import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.processor.TypeIdentifier;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import reactor.core.publisher.Mono;

public interface BaseBotProcessor extends TypeIdentifier {

    Mono<? extends BotApiMethod<?>> process(Data data);
}