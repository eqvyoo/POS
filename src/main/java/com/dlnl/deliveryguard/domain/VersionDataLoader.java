package com.dlnl.deliveryguard.domain;

import com.dlnl.deliveryguard.repository.ProgramVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VersionDataLoader implements CommandLineRunner {

    private final ProgramVersionRepository programVersionRepository;
    @Override
    public void run(String... args) throws Exception {
        if (programVersionRepository.count() == 0) {
            ProgramVersion programVersion = ProgramVersion.builder()
                    .version("1.0.0")
                    .description("First Release")
                    .releaseDate(LocalDateTime.of(2024,7,24,0,0))
                    .build();
            programVersionRepository.save(programVersion);
        }
    }
}
