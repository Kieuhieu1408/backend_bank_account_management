package com.klb.transactionService.domain.repositories;

import com.klb.transactionService.domain.entities.Card;
import com.klb.transactionService.domain.enums.CardStatus;

import java.util.List;
import java.util.Optional;

public interface CardRepository {

    Card save(Card card);

    List<Card> findByAccountId(String accountId);

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByCardStatus(CardStatus status);

    List<Card> findByAccountIdAndStatus(String accountId, CardStatus status);

    boolean existsByCardNumber(String cardNumber);
}
