package com.example.backend.repository;

import com.example.backend.entity.SafeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SafeItemRepository extends JpaRepository<SafeItem, Long> {
    Optional<SafeItem> findByItemHashAndThreatType(String itemHash, String threatType);
}
