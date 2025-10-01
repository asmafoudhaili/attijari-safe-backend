package com.example.backend.repository;

import com.example.backend.entity.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    List<Reclamation> findByProcessedFalse();

    Optional<Reclamation> findByItemHashAndThreatTypeAndProcessedFalse(String computedHash, String threatType);
}
