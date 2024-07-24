package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.domain.ProgramVersion;
import com.dlnl.deliveryguard.service.ProgramVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version")
public class ProgramVersionController {

    @Autowired
    private ProgramVersionService programVersionService;

    @GetMapping("/latest")
    public ResponseEntity<ProgramVersion> getLatestVersion() {
        ProgramVersion latestVersion = programVersionService.getLatestVersion();
        return ResponseEntity.ok(latestVersion);
    }
}
