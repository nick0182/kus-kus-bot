package com.shaidulin.kuskusbot.processor.image.send;

import com.shaidulin.kuskusbot.processor.TypeIdentifier;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public interface ImageSendBotProcessor extends TypeIdentifier {

    Mono<? extends SendPhoto> process(Update update);
}