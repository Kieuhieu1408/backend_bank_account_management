package com.klb.card_service.handlers;

import com.klb.card_service.commands.CreateCardCommand;
import com.klb.card_service.commands.CreateCardResponse;
import com.klb.card_service.core.Handler;
import com.klb.card_service.entity.Card;
import com.klb.card_service.entity.enums.CardStatus;
import com.klb.card_service.repository.CardRepository;
import com.klb.card_service.utils.CVVGenerator;
import com.klb.card_service.utils.CardNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

@Component
@Slf4j
public class CreateCardCommandHandler implements Handler<CreateCardCommand, CreateCardResponse> {

    private final CardRepository cardRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final CVVGenerator cvvGenerator;

    public CreateCardCommandHandler(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
        this.cardNumberGenerator = new CardNumberGenerator();
        this.cvvGenerator = new CVVGenerator();
    }

    @Override
    @Transactional
    public CreateCardResponse handle(CreateCardCommand request) {
        try {
            log.info("Creating card for account: {}", request.getAccountId());

            // 1. Validate request
            validateRequest(request);

            // 2. Generate card details
            String cardNumber = generateUniqueCardNumber();
            String cvv = cvvGenerator.generate();
            Date expiryDate = generateExpiryDate();

            // 3. Create card entity
            Card card = Card.builder()
                    .cardId(request.getCardId())
                    .accountId(request.getAccountId())
                    .cardNumber(cardNumber)
                    .cardHolderName(request.getCardHolderName())
                    .cardType(request.getCardType())
                    .cvvNumber(cvv)
                    .cardStatus(CardStatus.PENDING)
                    .creditLimit(request.getCreditLimit())
                    .availableBalance(request.getCreditLimit())
                    .issuanceDate(new Date())
                    .expiryDate(expiryDate)
                    .build();

            // 4. Save to database
            cardRepository.save(card);

            log.info("Card created successfully: {}", request.getCardId());

            return CreateCardResponse.success(
                    request.getCardId(),
                    maskCardNumber(cardNumber),
                    expiryDate
            );

        } catch (ValidationException e) {
            log.warn("Validation failed for card creation: {}", e.getMessage());
            return CreateCardResponse.failed(request.getCardId(), e.getMessage());

        } catch (Exception e) {
            log.error("Error creating card: {}", e.getMessage(), e);
            return CreateCardResponse.failed(request.getCardId(), "Internal server error");
        }
    }

    private void validateRequest(CreateCardCommand request) {
        if (request.getAccountId() == null || request.getAccountId().trim().isEmpty()) {
            throw new ValidationException("Account ID is required");
        }
        if (request.getCardHolderName() == null || request.getCardHolderName().trim().isEmpty()) {
            throw new ValidationException("Card holder name is required");
        }
        if (request.getCardType() == null) {
            throw new ValidationException("Card type is required");
        }
        if (request.getCreditLimit() == null || request.getCreditLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Credit limit must be greater than zero");
        }
    }

    private String generateUniqueCardNumber() {
        String cardNumber;
        int attempts = 0;
        do {
            cardNumber = cardNumberGenerator.generate();
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Unable to generate unique card number");
            }
        } while (cardRepository.existsByCardNumber(cardNumber));

        return cardNumber;
    }

    private Date generateExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 3);
        return calendar.getTime();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    // Custom exception
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
