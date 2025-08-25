package com.example.backend.repository;

import com.example.backend.entity.PhishingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhishingLogRepository extends JpaRepository<PhishingLog, Long> {
}
