package com.example.userservice.models;

import lombok.Data;


public class Session extends BaseModel{

    private String token;
    private User user;
    private Data expiringAt;
    private SessionStatus status;




}
