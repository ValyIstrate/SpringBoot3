package com.example.newproject.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountModel {
    private String firstName;
    private String lastName;
    private String email;
    private String passwd;
    private String matchingPasswd;
}
