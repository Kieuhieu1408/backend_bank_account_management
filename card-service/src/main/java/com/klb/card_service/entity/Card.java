package com.klb.card_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    String cardNumber;
    String cardName;
    String cvvNumber;
}
