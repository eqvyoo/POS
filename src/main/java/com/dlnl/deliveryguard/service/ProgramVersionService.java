package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.ProgramVersion;
import com.dlnl.deliveryguard.repository.ProgramVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgramVersionService {

    private final ProgramVersionRepository programVersionRepository;

    public ProgramVersion getLatestVersion() {
        Optional<ProgramVersion> latestVersion = programVersionRepository.findTopByOrderByReleaseDateDesc();
        return latestVersion.orElseThrow(() -> new RuntimeException("최신 버전 정보를 찾을 수 없습니다."));
    }
}
