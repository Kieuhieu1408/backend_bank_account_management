package com.klb.transactionService.domain.entities;

import com.klb.transactionService.domain.enums.CardStatus;
import com.klb.transactionService.domain.enums.CardType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Card {
    String cardId;

    String accountId;
    String cardNumber;
    String cardHolderName;
    String cvvNumber;

    CardType cardType;
    CardStatus cardStatus;

    Date issuanceAt;          // thời điểm phát hành
    Date expiryDate;        // ngày hết hạn (không cần thời gian)
    BigDecimal availableBalance; // số dư khả dụng
    BigDecimal creditLimit;      // hạn mức

    // ===== Một ít hành vi domain cơ bản (tùy chọn) =====
    public void activate() { this.cardStatus = CardStatus.ACTIVE; }

    public void block() { this.cardStatus = CardStatus.BLOCKED; }

    public void debit(BigDecimal amount) {
        if (amount.signum() <= 0) return;
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (amount.signum() <= 0) return;
        this.availableBalance = this.availableBalance.add(amount);
    }
}
