package com.klb.transactionService.infrastructure.persistence.repositories;

import com.klb.transactionService.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepositoryImpl extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
