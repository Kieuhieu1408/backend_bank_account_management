package com.klb.transactionService.infrastructure.persistence.adapters;

import com.klb.transactionService.domain.entities.Card;
import com.klb.transactionService.domain.enums.CardStatus;
import com.klb.transactionService.domain.repositories.CardRepository;
import com.klb.transactionService.infrastructure.persistence.repositories.CardRepositoryImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation for CardRepository that bridges the domain layer with JPA persistence
 */
@Component
public class CardRepositoryAdapter implements CardRepository {

    private final CardRepositoryImpl jpaCardRepository;

    public CardRepositoryAdapter(CardRepositoryImpl jpaCardRepository) {
        this.jpaCardRepository = jpaCardRepository;
    }

    @Override
    public Card save(Card domainCard) {
        com.klb.transactionService.infrastructure.persistence.entities.Card jpaCard = mapToJpaEntity(domainCard);
        com.klb.transactionService.infrastructure.persistence.entities.Card savedCard = jpaCardRepository.save(jpaCard);
        return mapToDomainEntity(savedCard);
    }

    @Override
    public List<Card> findByAccountId(String accountId) {
        return jpaCardRepository.findByAccountId(accountId).stream()
                .map(this::mapToDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Card> findByCardNumber(String cardNumber) {
        return jpaCardRepository.findByCardNumber(cardNumber)
                .map(this::mapToDomainEntity);
    }

    @Override
    public List<Card> findByCardStatus(CardStatus status) {
        return jpaCardRepository.findByCardStatus(status).stream()
                .map(this::mapToDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Card> findByAccountIdAndStatus(String accountId, CardStatus status) {
        return jpaCardRepository.findByAccountIdAndStatus(accountId, status).stream()
                .map(this::mapToDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCardNumber(String cardNumber) {
        return jpaCardRepository.existsByCardNumber(cardNumber);
    }

    /**
     * Maps a JPA entity to a domain entity
     */
    private Card mapToDomainEntity(com.klb.transactionService.infrastructure.persistence.entities.Card jpaCard) {
        return Card.builder()
                .cardId(jpaCard.getCardId())
                .accountId(jpaCard.getAccountId())
                .cardNumber(jpaCard.getCardNumber())
                .cardHolderName(jpaCard.getCardHolderName())
                .cvvNumber(jpaCard.getCvvNumber())
                .cardType(jpaCard.getCardType())
                .cardStatus(jpaCard.getCardStatus())
                .issuanceAt(jpaCard.getIssuanceAt())
                .expiryDate(jpaCard.getExpiryDate())
                .availableBalance(jpaCard.getAvailableBalance())
                .creditLimit(jpaCard.getCreditLimit())
                .build();
    }

    /**
     * Maps a domain entity to a JPA entity
     */
    private com.klb.transactionService.infrastructure.persistence.entities.Card mapToJpaEntity(Card domainCard) {
        return com.klb.transactionService.infrastructure.persistence.entities.Card.builder()
                .cardId(domainCard.getCardId())
                .accountId(domainCard.getAccountId())
                .cardNumber(domainCard.getCardNumber())
                .cardHolderName(domainCard.getCardHolderName())
                .cvvNumber(domainCard.getCvvNumber())
                .cardType(domainCard.getCardType())
                .cardStatus(domainCard.getCardStatus())
                .issuanceAt(domainCard.getIssuanceAt())
                .expiryDate(domainCard.getExpiryDate())
                .availableBalance(domainCard.getAvailableBalance())
                .creditLimit(domainCard.getCreditLimit())
                .build();
    }
}
