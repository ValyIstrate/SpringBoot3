package com.example.newproject.controllers;

import com.example.newproject.entities.Account;
import com.example.newproject.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/demo")
public class AccountController {
    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/getUsers")
    public List<Account> getUsers() {
        return accountService.getUsers();
    }
}
