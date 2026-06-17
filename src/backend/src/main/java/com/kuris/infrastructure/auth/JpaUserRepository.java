package com.kuris.infrastructure.auth;

import com.kuris.domain.auth.User;
import com.kuris.domain.auth.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Adaptador JPA para UserRepository. */
@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {
  Optional<User> findByEmail(String email);
}
