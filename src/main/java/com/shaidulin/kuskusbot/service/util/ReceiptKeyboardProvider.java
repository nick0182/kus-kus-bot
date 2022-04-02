package com.shaidulin.kuskusbot.service.util;

import com.shaidulin.kuskusbot.update.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

public interface ReceiptKeyboardProvider {
    Mono<InlineKeyboardMarkup> compileKeyboard(Data data, Data.Action firstButtonAction, String firstButtonText);
}
