# Cấu trúc dự án Bank Account Management System

## 1) Tổng quan cấu trúc toàn dự án
- account-service/: Service quản lý tài khoản ngân hàng (Spring Boot, Maven).
- api-gateway/: Cổng vào cho các service (Spring Cloud Gateway).
- notification-service/: Service thông báo.
- transaction-service/: Service giao dịch, quản lý thẻ (Spring Boot, CQRS + Mediator tự triển khai).
- docs/: Tài liệu hệ thống.

## 2) Cấu trúc chi tiết transaction-service (dạng cây thư mục)

```
com/klb/transactionService/
├── TransactionServiceApplication.java # Entry point Spring Boot
├── application/               # Application Layer - CQRS Commands & Queries
│   ├── commands/              # Commands (thay đổi dữ liệu)
│   │   └── card/              # Card domain commands
│   │       ├── CreateCardCommand.java
│   │       └── CreateCardResponse.java
│   ├── handlers/              # Command & Query Handlers
│   │   └── CreateCardCommandHandler.java
│   ├── mappers/               # Mapping DTOS và Commands/Queries
│   └── queries/               # Queries (truy vấn dữ liệu)
│       └── card/              # Card domain queries
│           ├── GetCardByCardNumberQuery.java
│           └── GetCardByCardNumberResponse.java
├── configuration/             # Spring Configuration & Security
│   ├── ApplicationInitConfig.java
│   ├── CorsConfiguration.java
│   ├── CustomAuthoritiesConverter.java
│   ├── CustomJwtDecoder.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── SecurityConfig.java
├── core/                      # Core Infrastructure - CQRS + Mediator
│   ├── abstractions/          # Core Interfaces
│   │   ├── Handler.java       # Interface Handler<TRequest, TResponse>
│   │   ├── RequestData.java   # Marker cho Request (Command/Query)
│   │   └── ResponseData.java  # Marker cho Response
│   ├── exceptions/            # Core Exceptions
│   │   ├── HandlerExecutionException.java
│   │   └── HandlerNotFoundException.java
│   └── mediator/              # Mediator Pattern Implementation
│       ├── CqrsBus.java       # Abstraction cho Bus
│       ├── Registry.java      # Đăng ký/tra cứu Handler bằng generic
│       └── SpringBus.java     # Mediator: điều phối request -> handler
├── domain/                    # Domain Layer - Business Logic
│   ├── entities/              # Domain Entities
│   │   ├── Card.java
│   │   ├── InvalidatedToken.java
│   │   ├── Permission.java
│   │   ├── Role.java
│   │   └── User.java
│   ├── enums/                 # Domain Enums
│   │   ├── CardStatus.java
│   │   └── CardType.java
│   ├── event/                 # Domain Events
│   │   ├── card/              # Card domain events
│   │   │   └── CardCreatedEvent.java
│   │   └── transaction/       # Transaction events (trống)
│   ├── repositories/          # Repository Interfaces (Domain Contracts)
│   │   ├── CardRepository.java
│   │   ├── RoleRepository.java
│   │   └── UserRepository.java
│   └── services/              # Domain Services
│       └── CardService.java
├── infrastructure/            # Infrastructure Layer
│   ├── persistence/           # Database Implementation
│   │   ├── mappers/           # Entity Mappers và JPA Entities và DTOs với Entity
│   │   │   ├── CardEntityMapper.java
│   │   └── repositories/      # Repository Implementations
│   │       ├── CardRepositoryImpl.java
│   │       ├── RoleRepositoryImpl.java
│   │       └── UserRepositoryImpl.java
│   └── service/               # Infrastructure Services
│       └── CardServiceImpl.java
├── presentation/              # Presentation Layer - Web API
│   ├── controllers/           # REST Controllers
│   │   └── CardController.java
│   └── dto/                   # Web API DTOs
│       ├── ApiResponse.java
│       └── CardRequest/       # Card request DTOs
│           └── CreateCardRequest.java
└── shared/                    # Shared Components
    ├── constant/              # System Constants
    │   └── PredefinedRole.java
    ├── exception/             # Application Exceptions
    │   ├── AppException.java
    │   ├── ErrorCode.java
    │   └── GlobalExceptionHandler.java
    └── utils/                 # Shared Utilities
        ├── CardNumberGenerator.java
        └── CVVGenerator.java
```

```
HTTP Request → CardController → Command/Query → Handler → Domain Service → Repository → Database
```
