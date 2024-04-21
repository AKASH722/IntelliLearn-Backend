package com.codex.intellilearn.repo;

import com.codex.intellilearn.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepo extends JpaRepository<VerificationToken, Integer> {

    VerificationToken findByUser_Id(Integer user_id);

    VerificationToken findByToken(String token);
}
