package com.shaidulin.kuskusbot.processor.image.edit.callback;

import com.shaidulin.kuskusbot.dto.receipt.ReceiptValue;
import com.shaidulin.kuskusbot.dto.receipt.Step;
import com.shaidulin.kuskusbot.processor.image.edit.ImageEditBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.MediaMessageProvider;
import com.shaidulin.kuskusbot.service.util.StepPageKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.ImageType;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
public record ReceiptStepPaginatedBotProcessor(StringCacheService cacheService,
                                               StepPageKeyboardProvider keyboardProvider,
                                               MediaMessageProvider<EditMessageMedia> editMessageMediaProvider)
        implements ImageEditBotProcessor {

    @Override
    public Mono<EditMessageMedia> process(Data data) {
        Data.Session currentSession = data.getSession();
        int page = Optional.ofNullable(currentSession.getCurrentStepPage()).orElse(0);
        return cacheService
                .getReceipt(data.getUserId())
                .map(ReceiptValue::steps)
                .zipWhen(steps -> keyboardProvider.compileKeyboard(data, steps.size() > page + 1))
                .flatMap(tuple2 -> provideMessage(tuple2.getT1().get(page), data, tuple2.getT2()));
    }

    private Mono<EditMessageMedia> provideMessage(Step step, Data data, InlineKeyboardMarkup keyboard) {
        String name = data.getSession().getReceiptId() + "." + step.number();
        String caption = MediaMessageProvider.createPhotoCaption(step);
        return editMessageMediaProvider.provideMessage(name, caption, ImageType.STEP, data, keyboard);
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_STEP_PAGINATED;
    }
}
