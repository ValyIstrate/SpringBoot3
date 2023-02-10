package com.example.newproject.events.listeners;

import com.example.newproject.entities.Account;
import com.example.newproject.events.RegistrationCompleteEvent;
import com.example.newproject.services.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent>{

    private final AccountService accountService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        // Create the Verification Token for the User with Link
        Account account = event.getAccount();
        String token = UUID.randomUUID().toString();
        accountService.saveVerificationTokenForUser(token, account);

        // Send mail to user (just mimicking)
        String url = event.getApplicationUrl()
                + "/registration/verifyRegistration?token="
                + token;

        // sendVerificationEmail()
        log.info("Click this link to verify your account: {}", url);
    }
}
