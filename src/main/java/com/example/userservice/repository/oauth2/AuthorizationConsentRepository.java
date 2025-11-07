package com.example.userservice.repository.oauth2;

import com.example.userservice.models.oauth2.AuthorizationConsent;
import com.example.userservice.models.oauth2.AuthorizationConsentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorizationConsentRepository extends JpaRepository<AuthorizationConsent, AuthorizationConsentId> {
    Optional<AuthorizationConsent> findByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
    
    void deleteByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
}

