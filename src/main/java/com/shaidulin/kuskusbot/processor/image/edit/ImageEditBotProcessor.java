package com.shaidulin.kuskusbot.processor.image.edit;

import com.shaidulin.kuskusbot.processor.TypeIdentifier;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public interface ImageEditBotProcessor extends TypeIdentifier {

    Mono<? extends EditMessageMedia> process(Update update);
}
