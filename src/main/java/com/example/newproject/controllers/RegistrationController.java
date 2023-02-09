package com.example.newproject.controllers;

import com.example.newproject.entities.Account;
import com.example.newproject.entities.VerificationToken;
import com.example.newproject.events.RegistrationCompleteEvent;
import com.example.newproject.models.AccountModel;
import com.example.newproject.models.PasswordModel;
import com.example.newproject.services.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/registration")
@Slf4j
public class RegistrationController {

    private final AccountService accountService;
    private final ApplicationEventPublisher publisher;

    public RegistrationController(AccountService accountService, ApplicationEventPublisher publisher) {
        this.accountService = accountService;
        this.publisher = publisher;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody AccountModel accountModel, final HttpServletRequest request) {
        Account account = accountService.registerAccount(accountModel);
        publisher.publishEvent(new RegistrationCompleteEvent(account, applicationUrl(request)));
        return "Account has been successfully created!";
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = accountService.validateVerificationToken(token);
        if(result.equalsIgnoreCase("valid")) {
            return "User verified successfully!";
        } else {
            return "Failed to verify user!";
        }
    }

    @GetMapping("/resendVerificationToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,
                                          HttpServletRequest request) {
        VerificationToken verificationToken = accountService.generateNewVerificationToken(oldToken);
        Account account = verificationToken.getAccount();
        resendVerificationTokenMail(account, applicationUrl(request), verificationToken);
        return "Verification link sent!";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {
        Account account = accountService.findUserByEmail(passwordModel.getEmail());
        String url = "";

        if(account != null) {
            String token = UUID.randomUUID().toString();
            accountService.createPasswordResetTokenForUser(account, token);
            url = passwordResetTokenMail(account, applicationUrl(request), token);
        }

        return url;
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody PasswordModel passwordModel) {
        String result = accountService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")) {
            return "Failed to reset password!";
        }

        Optional<Account> account = accountService.getUserByPasswordResetToken(token);
        if(account.isPresent()) {
            accountService.changePassword(account.get(), passwordModel.getNewPassword());
            return "Password reset successfully";
        } else {
            return "User was not found!";
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel) {
        Account account = accountService.findUserByEmail(passwordModel.getEmail());
        if(!accountService.checkIfValidOldPassword(account, passwordModel.getOldPassword())) {
            return "Old password is incorrect!";
        }

        accountService.changePassword(account, passwordModel.getNewPassword());
        return "Password changed successfully!";
    }

    private String passwordResetTokenMail(Account account, String applicationUrl, String token) {
        String url = applicationUrl
                + "/registration/savePassword?token="
                + token;

        // sendVerificationEmail()
        log.info("Click this link to reset your password: {}", url);
        return url;
    }

    private void resendVerificationTokenMail(Account account,
                                             String applicationUrl,
                                             VerificationToken verificationToken) {
        String url = applicationUrl
                + "/registration/verifyRegistration?token="
                + verificationToken.getToken();

        // sendVerificationEmail()
        log.info("Click this link to verify your account: {}", url);
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://"
                + request.getServerName()
                + ":" + request.getServerPort()
                + request.getContextPath();
    }
}