package com.shaidulin.kuskusbot.processor.image.callback;

import com.shaidulin.kuskusbot.processor.image.ImageBotProcessor;
import com.shaidulin.kuskusbot.update.Router;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public class ReceiptPresentationPaginatedBotProcessor implements ImageBotProcessor {

    @Override
    public Mono<? extends SendPhoto> process(Update update) {
        return null;
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_PRESENTATION_PAGINATED;
    }
}
