package com.bankingSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = "com.bankingSystem")
public class BankingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankingSystemApplication.class, args);
    }
}