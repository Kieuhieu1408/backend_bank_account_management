package com.klb.transactionService.infrastructure.persistence.entities;

import com.klb.transactionService.domain.enums.CardStatus;
import com.klb.transactionService.domain.enums.CardType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "card")
public class Card {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "card_id", nullable = false, updatable = false, columnDefinition = "VARCHAR(36)")
    String cardId;

    @Column(name = "account_id", nullable = false)
    String accountId;

    @Column(name = "card_number", nullable = false, unique = true, length = 32)
    String cardNumber;

    @Column(name = "card_holder_name", nullable = false, length = 128)
    String cardHolderName;

    @Column(name = "cvv_number", nullable = false, length = 8)
    String cvvNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 32)
    CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_status", nullable = false, length = 32)
    CardStatus cardStatus;

    @Column(name = "issuance_at", nullable = false)
    Date issuanceAt;

    @Column(name = "expiry_date", nullable = false)
    Date expiryDate;

    @Column(name = "available_balance", precision = 19, scale = 4, nullable = false)
    BigDecimal availableBalance;

    @Column(name = "credit_limit", precision = 19, scale = 4, nullable = false)
    BigDecimal creditLimit;
}
