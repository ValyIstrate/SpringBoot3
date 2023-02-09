package com.example.newproject.services;

import com.example.newproject.entities.Account;
import com.example.newproject.entities.PasswordResetToken;
import com.example.newproject.entities.VerificationToken;
import com.example.newproject.models.AccountModel;
import com.example.newproject.repositories.AccountRepository;
import com.example.newproject.repositories.PasswordResetTokenRepository;
import com.example.newproject.repositories.VerificationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AccountService(AccountRepository accountRepository,
                          PasswordEncoder passwordEncoder,
                          VerificationTokenRepository verificationTokenRepository,
                          PasswordResetTokenRepository passwordResetTokenRepository) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public List<Account> getUsers() {
        return accountRepository.findAll();
    }

    public Account registerAccount(AccountModel accountModel) {
        Account account = new Account();
        account.setEmail(accountModel.getEmail());
        account.setFirstName(accountModel.getFirstName());
        account.setLastName(accountModel.getLastName());
        account.setPasswd(passwordEncoder.encode(accountModel.getPasswd()));
        account.setRole("USER");

        accountRepository.save(account);
        return account;
    }

    public void saveVerificationTokenForUser(String token, Account account) {
        VerificationToken verificationToken = new VerificationToken(account, token);
        verificationTokenRepository.save(verificationToken);
    }

    public String validateVerificationToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if(verificationToken == null) {
            return "invalid";
        }

        Account account = verificationToken.getAccount();
        Calendar calendar = Calendar.getInstance();

        if(verificationToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
            verificationTokenRepository.delete(verificationToken);
            return "expired";
        }

        account.setEnabled(true);
        accountRepository.save(account);
        return "valid";
    }

    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    public Account findUserByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public void createPasswordResetTokenForUser(Account account, String token) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(account, token);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if(passwordResetToken == null) {
            return "invalid";
        }

        Account account = passwordResetToken.getAccount();
        Calendar calendar = Calendar.getInstance();

        if(passwordResetToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }

        return "valid";
    }

    public Optional<Account> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getAccount());
    }

    public void changePassword(Account account, String newPassword) {
        account.setPasswd(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public boolean checkIfValidOldPassword(Account account, String oldPassword) {
        return passwordEncoder.matches(oldPassword, account.getPasswd());
    }
}
