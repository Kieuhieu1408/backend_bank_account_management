package com.klb.card_service.commands;

import com.klb.card_service.core.ResponseData;
import com.klb.card_service.entity.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCardResponse implements ResponseData {
    private String cardId;
    private String status;
    private String message;
    private String cardNumber;
    private Date expiryDate;
    private CardStatus cardStatus;

    public static CreateCardResponse success(String cardId, String cardNumber, Date expiryDate) {
        return CreateCardResponse.builder()
                .cardId(cardId)
                .status("SUCCESS")
                .message("Card created successfully")
                .cardNumber(cardNumber)
                .expiryDate(expiryDate)
                .cardStatus(CardStatus.PENDING)
                .build();
    }

    public static CreateCardResponse failed(String cardId, String error) {
        return CreateCardResponse.builder()
                .cardId(cardId)
                .status("FAILED")
                .message(error)
                .build();
    }
}
