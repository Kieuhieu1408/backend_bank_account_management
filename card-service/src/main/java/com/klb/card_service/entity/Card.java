package com.klb.card_service.entity;

import com.klb.card_service.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.klb.card_service.entity.enums.CardType;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String cardId;

    String accountId;
    String cardNumber;
    String cardHolderName;
    String cvvNumber;

    @Enumerated(EnumType.STRING)
    CardType cardType;
    @Enumerated(EnumType.STRING)
    CardStatus cardStatus;

    @Temporal(TemporalType.TIMESTAMP)
    Date issuanceDate;
    Date expiryDate;
    BigDecimal availableBalance;
    BigDecimal creditLimit;
}
