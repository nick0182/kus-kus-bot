package com.shaidulin.kuskusbot.processor.image.send;

import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.processor.TypeIdentifier;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import reactor.core.publisher.Mono;

public interface ImageSendBotProcessor extends TypeIdentifier {

    Mono<? extends SendPhoto> process(Data data);
}
