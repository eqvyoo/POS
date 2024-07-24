package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.ProgramVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramVersionRepository extends JpaRepository<ProgramVersion, Long> {
    Optional<ProgramVersion> findTopByOrderByReleaseDateDesc();
}