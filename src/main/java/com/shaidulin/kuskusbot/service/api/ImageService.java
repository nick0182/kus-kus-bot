package com.shaidulin.kuskusbot.service.api;

import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.dto.receipt.Step;
import com.shaidulin.kuskusbot.util.ImageType;
import org.springframework.lang.NonNull;
import org.threeten.extra.AmountFormats;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Locale;

public interface ImageService {

    Mono<byte[]> fetchImage(@NonNull ImageType type, @NonNull String id);

    default String createPhotoCaption(ReceiptPresentationValue receiptPresentation) {
        Locale russian = new Locale.Builder().setLanguage("ru").setRegion("RU").build();
        String not_defined = "Не указано";
        Duration cookTime = receiptPresentation.cookTime();
        String cookTimeString = cookTime != null ? AmountFormats.wordBased(cookTime, russian) : not_defined;

        int portions = receiptPresentation.portions();
        String portionsString = portions != 0 ? String.valueOf(portions) : not_defined;

        return "\uD83C\uDF72 " + receiptPresentation.name() + "\n" +
                "⏱ " + cookTimeString + "\n" +
                "Количество порций " + portionsString;
    }

    default String createPhotoCaption(Step step) {
        return step.number() + ".\n" + step.text();
    }
}
