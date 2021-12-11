package com.shaidulin.kuskusbot.service.api;

import com.shaidulin.kuskusbot.dto.receipt.Page;
import com.shaidulin.kuskusbot.dto.ingredient.IngredientMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptValue;
import com.shaidulin.kuskusbot.util.SortType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReceiptService {

    Mono<IngredientMatch> suggestIngredients(@NonNull String toSearch, @Nullable List<String> known);

    Mono<ReceiptPresentationMatch> getReceiptPresentations(@NonNull List<String> ingredients,
                                                           Page page,
                                                           @NonNull SortType sortType);

    Mono<ReceiptValue> getReceipt(int id);
}
