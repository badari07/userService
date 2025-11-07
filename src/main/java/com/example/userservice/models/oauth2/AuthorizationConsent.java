package com.example.userservice.models.oauth2;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "authorizationconsent")
@Data
@IdClass(AuthorizationConsentId.class)
public class AuthorizationConsent {
    @Id
    private String registeredClientId;
    
    @Id
    private String principalName;
    
    @Column(length = 1000)
    private String authorities;
}

