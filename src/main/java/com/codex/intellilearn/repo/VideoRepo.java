package com.codex.intellilearn.repo;

import com.codex.intellilearn.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepo extends JpaRepository<Video, Integer> {
}
