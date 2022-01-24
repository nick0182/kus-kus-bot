package com.shaidulin.kuskusbot.processor.image.edit.callback;

import com.shaidulin.kuskusbot.dto.receipt.ReceiptValue;
import com.shaidulin.kuskusbot.dto.receipt.Step;
import com.shaidulin.kuskusbot.processor.image.edit.ImageEditBotProcessor;
import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.ImageType;
import com.shaidulin.kuskusbot.util.SortType;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
public record ReceiptStepPaginatedBotProcessor(StringCacheService cacheService,
                                               ImageService imageService) implements ImageEditBotProcessor {

    @Override
    public Mono<? extends EditMessageMedia> process(Data data) {
        Data.Session currentSession = data.getSession();
        int page = Optional.ofNullable(currentSession.getCurrentStepPage()).orElse(0);
        return cacheService
                .getReceipt(data.getUserId())
                .map(ReceiptValue::steps)
                .zipWhen(steps -> compileReceiptStepButtons(data, page, steps.size() > page + 1))
                .flatMap(tuple2 -> provideMessage(tuple2.getT1().get(page), data, tuple2.getT2()));
    }

    private Mono<InlineKeyboardMarkup> compileReceiptStepButtons(Data data, int currentStepPage, boolean hasMoreReceipts) {
        Data.Session currentSession = data.getSession();
        int receiptId = currentSession.getReceiptId();
        int currentReceiptPage = currentSession.getCurrentReceiptPage();
        SortType receiptSortType = currentSession.getReceiptSortType();

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonCurrentIndex = 0;

        Integer previousPageButtonIndex = null;
        Integer nextPageButtonIndex = null;

        if (currentStepPage > 0) {
            previousPageButtonIndex = buttonCurrentIndex++;
            sessionHash.put(previousPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_STEP_PAGE)
                    .receiptId(receiptId)
                    .currentReceiptPage(currentReceiptPage)
                    .receiptSortType(receiptSortType)
                    .currentStepPage(currentStepPage - 1)
                    .build());
        }

        if (hasMoreReceipts) {
            nextPageButtonIndex = buttonCurrentIndex++;
            sessionHash.put(nextPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_STEP_PAGE)
                    .receiptId(receiptId)
                    .currentReceiptPage(currentReceiptPage)
                    .receiptSortType(receiptSortType)
                    .currentStepPage(currentStepPage + 1)
                    .build());
        }

        buttons.add(DynamicKeyboard.createNavigationPanelRow(previousPageButtonIndex, nextPageButtonIndex));

        buttons.add(DynamicKeyboard.createButtonRow("К рецепту", String.valueOf(buttonCurrentIndex)));
        sessionHash.put(buttonCurrentIndex, Data.Session
                .builder()
                .action(Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE)
                .receiptId(receiptId)
                .currentReceiptPage(currentReceiptPage)
                .receiptSortType(receiptSortType)
                .build());

        return cacheService
                .storeSession(data.getUserId(), sessionHash)
                .map(ignored -> InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    private Mono<EditMessageMedia> provideMessage(Step step, Data data, InlineKeyboardMarkup keyboard) {
        log.debug("Got receipt step to render: {}", step);

        String imageId = data.getSession().getReceiptId() + "." + step.number();

        return cacheService
                .getImage(imageId, ImageType.MAIN)
                .map(cachedImage -> compileMessage(cachedImage, null, data, step, imageId, keyboard))
                .switchIfEmpty(imageService
                        .fetchImage(ImageType.STEP, imageId)
                        .map(imageBytes -> compileMessage(null, imageBytes, data, step, imageId, keyboard))
                );
    }

    @SneakyThrows
    private EditMessageMedia compileMessage(String cachedImage, byte[] imageBytes, Data data,
                                            Step step, String imageId, InlineKeyboardMarkup keyboard) {
        boolean isNewMedia = Objects.isNull(cachedImage);

        InputMedia photo = new InputMediaPhoto();
        photo.setCaption(imageService.createPhotoCaption(step));
        if (isNewMedia) {
            log.debug("Compiling photo from resource with length: {} and name: {}", imageBytes.length, imageId);
            photo.setMedia(new ByteArrayResource(imageBytes).getInputStream(), imageId);
        } else {
            log.debug("Compiling photo from cache with name: {}", imageId);
            photo.setMedia(cachedImage);
        }

        return EditMessageMedia.builder()
                .chatId(data.getChatId())
                .messageId(data.getMessageId())
                .media(photo)
                .replyMarkup(keyboard)
                .build();
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_STEP_PAGINATED;
    }
}
