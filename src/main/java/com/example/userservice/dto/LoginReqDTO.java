package com.example.userservice.dto;


import lombok.Data;

@Data
public class LoginReqDTO {

    private String email;
    private String password;
}
