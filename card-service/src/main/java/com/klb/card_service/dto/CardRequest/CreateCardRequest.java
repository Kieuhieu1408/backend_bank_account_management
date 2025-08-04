package com.klb.card_service.dto.CardRequest;

import com.klb.card_service.entity.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCardRequest {
    private String accountId;
    private String cardHolderName;
    private CardType cardType;
    private BigDecimal creditLimit;
}
