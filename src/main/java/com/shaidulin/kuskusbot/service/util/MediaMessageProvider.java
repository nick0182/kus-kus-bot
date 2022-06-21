package com.shaidulin.kuskusbot.service.util;

import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.dto.receipt.Step;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.util.Emoji;
import com.shaidulin.kuskusbot.util.ImageType;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.threeten.extra.AmountFormats;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MediaMessageProvider<T> {

    Locale RUSSIAN = new Locale.Builder().setLanguage("ru").setRegion("RU").build();

    Mono<T> provideMessage(String name, String caption, ImageType type, Data data, InlineKeyboardMarkup keyboard);

    static String createPhotoCaption(ReceiptPresentationValue receiptPresentation) {
        return Stream.of(
                        createNameString(receiptPresentation.name()),
                        createCookTimeString(receiptPresentation.cookTime()),
                        createPortionsString(receiptPresentation.portions()))
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

    private static String createNameString(String name) {
        return Emoji.NAME + " " + name;
    }

    private static String createCookTimeString(Duration cookTime) {
        if (Objects.nonNull(cookTime)) {
            return Emoji.COOK_TIME + " " + AmountFormats.wordBased(cookTime, RUSSIAN);
        } else {
            return null;
        }
    }

    private static String createPortionsString(int portions) {
        String portionsString = String.valueOf(portions);
        String prefix = Emoji.PORTIONS + " ";
        if (portions == 0) {
            return null;
        } else if (portions == 1) {
            return prefix + portionsString + " порция";
        } else if (portions == 2 || portions == 3 || portions == 4) {
            return prefix + portionsString + " порции";
        } else if (portions >= 20 && portions % 10 == 1) {
            return prefix + portionsString + " порция";
        } else if (portions >= 20 && (portions % 10 == 2 || portions % 10 == 3 || portions % 10 == 4)) {
            return prefix + portionsString + " порции";
        } else {
            return prefix + portionsString + " порций";
        }
    }

    static String createPhotoCaption(Step step) {
        return step.number() + ".\n" + step.text();
    }
}
