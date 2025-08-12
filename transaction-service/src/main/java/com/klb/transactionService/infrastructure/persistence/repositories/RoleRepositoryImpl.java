package com.klb.transactionService.infrastructure.persistence.repositories;

import com.klb.transactionService.domain.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepositoryImpl extends JpaRepository<Role, String> {

}
