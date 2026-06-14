package com.clinicasaas.infrastructure.auth;

import com.clinicasaas.domain.auth.User;
import com.clinicasaas.domain.auth.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Adaptador JPA para UserRepository. */
@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {
  Optional<User> findByEmail(String email);
}
