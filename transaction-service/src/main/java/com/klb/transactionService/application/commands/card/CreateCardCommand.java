package com.klb.transactionService.application.card.commands.card;

import com.klb.transactionService.core.abstractions.RequestData;
import com.klb.transactionService.domain.enums.CardType;
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
