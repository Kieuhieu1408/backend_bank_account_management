package com.klb.transactionService.presentation.controller;

import com.klb.transactionService.application.card.commands.card.CreateCardCommand;
import com.klb.transactionService.application.card.commands.card.CreateCardResponse;
import com.klb.transactionService.core.mediator.CqrsBus;
import com.klb.transactionService.presentation.dto.ApiResponse;
import com.klb.transactionService.presentation.dto.CardRequest.CreateCardRequest;
import com.klb.transactionService.domain.services.CardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CardController {
    CardService cardService;
    final CqrsBus cqrsBus;

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CreateCardResponse> createCard(@RequestBody CreateCardRequest request) {
        CreateCardCommand command = new CreateCardCommand(
                null,
                request.getAccountId(),
                request.getCardHolderName(),
                request.getCardType(),
                request.getCreditLimit()
        );

        CreateCardResponse result = cqrsBus.execute(command);

        return ApiResponse.<CreateCardResponse>builder()
                .result(result)
                .build();
    }
}
