package com.securecoda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecureCodaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureCodaApplication.class, args);
    }
}