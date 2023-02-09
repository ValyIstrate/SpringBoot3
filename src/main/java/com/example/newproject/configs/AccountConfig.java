package com.example.newproject.configs;

import com.example.newproject.entities.Account;
import com.example.newproject.entities.Role;
import com.example.newproject.repositories.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AccountConfig {

    @Bean
    CommandLineRunner commandLineRunner(AccountRepository accountRepository) {
        return args -> {
            Account admin = new Account(
                    "istrate327@gmail.com",
                    "passwd",
                    "Valentin",
                    "Istrate",
                    Role.ADMIN
            );
            accountRepository.saveAll(List.of(admin));
        };
    }
}
