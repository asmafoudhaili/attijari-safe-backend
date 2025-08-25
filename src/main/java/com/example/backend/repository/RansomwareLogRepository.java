package com.example.backend.repository;

import com.example.backend.entity.RansomwareLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RansomwareLogRepository extends JpaRepository<RansomwareLog, Long> {
}
