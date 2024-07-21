package com.dlnl.deliveryguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
// @ComponentScan(basePackages = {"com.dlnl.deliveryguard.jwt", "com.dlnl.deliveryguard.config","com.dlnl.deliveryguard"})
@EnableJpaAuditing
public class DlnlApplication {

    public static void main(String[] args) {
        SpringApplication.run(DlnlApplication.class, args);
    }

}
