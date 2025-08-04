package com.klb.card_service.commands;

import com.klb.card_service.core.RequestData;
import com.klb.card_service.entity.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCardCommand implements RequestData {
    private String cardId;
    private String accountId;
    private String cardHolderName;
    private CardType cardType;
    private BigDecimal creditLimit;
}
