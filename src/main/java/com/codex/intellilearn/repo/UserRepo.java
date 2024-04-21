package com.codex.intellilearn.repo;

import com.codex.intellilearn.model.User_;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User_, Integer> {
    Optional<User_> findByEmail(String email);

    Optional<User_> findByUsername(String username);
}
