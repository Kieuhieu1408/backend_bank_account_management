package com.klb.card_service.controller;

import com.klb.card_service.commands.CreateCardCommand;
import com.klb.card_service.commands.CreateCardResponse;
import com.klb.card_service.core.CqrsBus;
import com.klb.card_service.dto.ApiResponse;
import com.klb.card_service.dto.CardRequest.CreateCardRequest;
import com.klb.card_service.service.CardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.klb.card_service.dto.*;

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
        // Sinh cardId ở đây (UUID)
        String cardId = java.util.UUID.randomUUID().toString();

        // Mapping từ DTO sang Command (tùy thuộc vào cấu trúc của CreateCardRequest và CreateCardCommand)
        CreateCardCommand command = new CreateCardCommand(
                cardId,
                request.getAccountId(),
                request.getCardHolderName(),
                request.getCardType(),
                request.getCreditLimit()
        );

        // Gửi command qua CQRS Bus để dispatch tới handler
        CreateCardResponse result = cqrsBus.execute(command);

        // Trả về theo response chuẩn
        return ApiResponse.<CreateCardResponse>builder()
                .result(result)
                .build();
    }
}
