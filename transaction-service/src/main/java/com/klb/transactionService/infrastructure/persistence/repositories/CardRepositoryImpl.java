package com.klb.transactionService.infrastructure.persistence.repositories;

import com.klb.transactionService.infrastructure.persistence.entities.Card;
import com.klb.transactionService.domain.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepositoryImpl extends JpaRepository<Card, String> {

    List<Card> findByAccountId(String accountId);

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByCardStatus(CardStatus status);

    @Query("SELECT c FROM Card c WHERE c.accountId = :accountId AND c.cardStatus = :status")
    List<Card> findByAccountIdAndStatus(@Param("accountId") String accountId,
                                        @Param("status") CardStatus status);

    boolean existsByCardNumber(String cardNumber);
}
