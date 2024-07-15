package com.dlnl.deliveryguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DlnlApplication {

    public static void main(String[] args) {
        SpringApplication.run(DlnlApplication.class, args);
    }

}
