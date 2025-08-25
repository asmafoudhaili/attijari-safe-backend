package com.example.backend.repository;

import com.example.backend.entity.CodeSafetyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeSafetyLogRepository extends JpaRepository<CodeSafetyLog, Long> {
}
