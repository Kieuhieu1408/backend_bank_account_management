# Hướng dẫn triển khai Card Service với CQRS Pattern

## Triển khai Clean SpringBus Pattern

đây là ví dụ triển khai mediator pattern để tìm đến đúng handler
![alt text](image.png)

### 1.1 Clean CQRS Infrastructure cho Card Service
```java
// card-service/src/main/java/com/klb/card_service/core/RequestData.java
public interface RequestData {
}

// card-service/src/main/java/com/klb/card_service/core/ResponseData.java
public interface ResponseData {
}

// card-service/src/main/java/com/klb/card_service/core/Handler.java
public interface Handler<T extends RequestData, R extends ResponseData> {
    R handle(T requestData);
}

// card-service/src/main/java/com/klb/card_service/core/CqrsBus.java
public interface CqrsBus {
    <T extends RequestData, R extends ResponseData> R execute(T requestData);
}
```

### 1.2 Registry Implementation (Auto-registration) (sổ đăng ký handler)
```java
// card-service/src/main/java/com/klb/card_service/core/Registry.java
@Component
@Slf4j
public class Registry {
    
    private final Map<Class<? extends RequestData>, Handler<?, ?>> handlers = new ConcurrentHashMap<>();
    
    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Map<String, Handler> handlerBeans = context.getBeansOfType(Handler.class);
        
        log.info("Registering {} handlers", handlerBeans.size());
        
        handlerBeans.values().forEach(this::registerHandler);
        
        log.info("Registration completed. Total handlers: {}", handlers.size());
        handlers.forEach((key, value) -> 
            log.info("  {} -> {}", key.getSimpleName(), value.getClass().getSimpleName())
        );
    }
    
    @SuppressWarnings("unchecked")
    private void registerHandler(Handler<?, ?> handler) {
        Class<?> handlerClass = handler.getClass();
        
        // Find Handler interface with generic types
        Arrays.stream(handlerClass.getGenericInterfaces())
            .filter(type -> type instanceof ParameterizedType)
            .map(type -> (ParameterizedType) type)
            .filter(paramType -> Handler.class.equals(paramType.getRawType()))
            .findFirst()
            .ifPresent(paramType -> {
                Type requestType = paramType.getActualTypeArguments()[0];
                if (requestType instanceof Class) {
                    Class<? extends RequestData> requestClass = (Class<? extends RequestData>) requestType;
                    handlers.put(requestClass, handler);
                    log.debug("Registered: {} -> {}", requestClass.getSimpleName(), handlerClass.getSimpleName());
                }
            });
    }
    
    @SuppressWarnings("unchecked")
    public <T extends RequestData, R extends ResponseData> Handler<T, R> getHandler(Class<T> requestClass) {
        return (Handler<T, R>) handlers.get(requestClass);
    }
    
    public boolean hasHandler(Class<? extends RequestData> requestClass) {
        return handlers.containsKey(requestClass);
    }
}
```

### 1.3 SpringBus Implementation (Clean & Simple)
```java
// card-service/src/main/java/com/klb/card_service/core/SpringBus.java
@Component
@Slf4j
public class SpringBus implements CqrsBus {

    private final Registry registry;
    
    public SpringBus(Registry registry) {
        this.registry = registry;
    }

    @Override
    public <T extends RequestData, R extends ResponseData> R execute(T requestData) {
        Class<T> requestClass = (Class<T>) requestData.getClass();
        
        log.debug("Executing: {}", requestClass.getSimpleName());
        
        Handler<T, R> handler = registry.getHandler(requestClass);
        
        if (handler == null) {
            log.error("No handler found for: {}", requestClass.getSimpleName());
            throw new HandlerNotFoundException("No handler registered for: " + requestClass.getSimpleName());
        }
        
        try {
            R response = handler.handle(requestData);
            log.debug("Executed successfully: {}", requestClass.getSimpleName());
            return response;
            
        } catch (Exception e) {
            log.error("Error executing {}: {}", requestClass.getSimpleName(), e.getMessage(), e);
            throw new HandlerExecutionException("Error executing " + requestClass.getSimpleName(), e);
        }
    }
}

// Exception classes
public class HandlerNotFoundException extends RuntimeException {
    public HandlerNotFoundException(String message) {
        super(message);
    }
}

public class HandlerExecutionException extends RuntimeException {
    public HandlerExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## 2. Tạo Domain Models cho Card Service

### 2.1 Card Entity
```java
// card-service/src/main/java/com/klb/card_service/domain/Card.java
@Entity
@Table(name = "cards")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Card {

    @Id
    private String cardId;

    @Column(nullable = false)
    private String accountId;

    @Column(unique = true, nullable = false)
    private String cardNumber;

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private String cvvNumber;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus;

    @Temporal(TemporalType.TIMESTAMP)
    private Date issuanceDate;

    @Temporal(TemporalType.DATE)
    private Date expiryDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(precision = 15, scale = 2)
    private BigDecimal availableBalance;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 2.2 Enums
```java
// card-service/src/main/java/com/klb/card_service/domain/CardType.java
public enum CardType {
    DEBIT,
    CREDIT,
    PREPAID
}

// card-service/src/main/java/com/klb/card_service/domain/CardStatus.java
public enum CardStatus {
    PENDING,
    ACTIVE,
    BLOCKED,
    EXPIRED,
    CANCELLED
}
```

### 2.3 Repository
```java
// card-service/src/main/java/com/klb/card_service/repository/CardRepository.java
@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    
    List<Card> findByAccountId(String accountId);
    
    Optional<Card> findByCardNumber(String cardNumber);
    
    List<Card> findByCardStatus(CardStatus status);
    
    @Query("SELECT c FROM Card c WHERE c.accountId = :accountId AND c.cardStatus = :status")
    List<Card> findByAccountIdAndStatus(@Param("accountId") String accountId, 
                                       @Param("status") CardStatus status);
    
    boolean existsByCardNumber(String cardNumber);
}
```

## 3. Commands và Responses (Clean Implementation)

### 3.1 Create Card Command
```java
// card-service/src/main/java/com/klb/card_service/commands/CreateCardCommand.java
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

// card-service/src/main/java/com/klb/card_service/commands/CreateCardResponse.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCardResponse implements ResponseData {
    private String cardId;
    private String status;
    private String message;
    private String cardNumber;
    private Date expiryDate;
    private CardStatus cardStatus;
    
    public static CreateCardResponse success(String cardId, String cardNumber, Date expiryDate) {
        return CreateCardResponse.builder()
            .cardId(cardId)
            .status("SUCCESS")
            .message("Card created successfully")
            .cardNumber(cardNumber)
            .expiryDate(expiryDate)
            .cardStatus(CardStatus.PENDING)
            .build();
    }
    
    public static CreateCardResponse failed(String cardId, String error) {
        return CreateCardResponse.builder()
            .cardId(cardId)
            .status("FAILED")
            .message(error)
            .build();
    }
}
```

### 3.2 Activate Card Command
```java
// card-service/src/main/java/com/klb/card_service/commands/ActivateCardCommand.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivateCardCommand implements RequestData {
    private String cardId;
}

// card-service/src/main/java/com/klb/card_service/commands/ActivateCardResponse.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivateCardResponse implements ResponseData {
    private String cardId;
    private String status;
    private String message;
    private CardStatus cardStatus;
    
    public static ActivateCardResponse success(String cardId) {
        return ActivateCardResponse.builder()
            .cardId(cardId)
            .status("SUCCESS")
            .message("Card activated successfully")
            .cardStatus(CardStatus.ACTIVE)
            .build();
    }
    
    public static ActivateCardResponse failed(String cardId, String error) {
        return ActivateCardResponse.builder()
            .cardId(cardId)
            .status("FAILED")
            .message(error)
            .build();
    }
}
```

## 4. Command Handlers (Pure Implementation)

### 4.1 Create Card Handler
```java
// card-service/src/main/java/com/klb/card_service/handlers/CreateCardCommandHandler.java
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
```

### 4.2 Activate Card Handler
```java
// card-service/src/main/java/com/klb/card_service/handlers/ActivateCardCommandHandler.java
@Component
@Slf4j
public class ActivateCardCommandHandler implements Handler<ActivateCardCommand, ActivateCardResponse> {
    
    private final CardRepository cardRepository;
    
    public ActivateCardCommandHandler(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }
    
    @Override
    @Transactional
    public ActivateCardResponse handle(ActivateCardCommand request) {
        try {
            log.info("Activating card: {}", request.getCardId());
            
            // 1. Find card
            Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + request.getCardId()));
            
            // 2. Validate card status
            if (card.getCardStatus() == CardStatus.ACTIVE) {
                return ActivateCardResponse.failed(request.getCardId(), "Card is already active");
            }
            
            if (card.getCardStatus() == CardStatus.BLOCKED) {
                return ActivateCardResponse.failed(request.getCardId(), "Cannot activate blocked card");
            }
            
            if (card.getCardStatus() == CardStatus.EXPIRED) {
                return ActivateCardResponse.failed(request.getCardId(), "Cannot activate expired card");
            }
            
            if (card.getCardStatus() == CardStatus.CANCELLED) {
                return ActivateCardResponse.failed(request.getCardId(), "Cannot activate cancelled card");
            }
            
            // 3. Activate card
            card.setCardStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
            
            log.info("Card activated successfully: {}", request.getCardId());
            
            return ActivateCardResponse.success(request.getCardId());
            
        } catch (CardNotFoundException e) {
            log.warn("Card not found: {}", e.getMessage());
            return ActivateCardResponse.failed(request.getCardId(), e.getMessage());
            
        } catch (Exception e) {
            log.error("Error activating card: {}", e.getMessage(), e);
            return ActivateCardResponse.failed(request.getCardId(), "Internal server error");
        }
    }
    
    public static class CardNotFoundException extends RuntimeException {
        public CardNotFoundException(String message) {
            super(message);
        }
    }
}
```
