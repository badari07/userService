package com.example.userservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class ValidateTokenResDTO {
    private RequestStatus status;
    private Boolean isValid;
    private String email;
    private String userId;
    private List<String> roles;
    private Long expirationTime; // milliseconds since epoch
    private String message;
}

