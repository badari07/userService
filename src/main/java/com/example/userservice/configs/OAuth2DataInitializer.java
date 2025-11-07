package com.example.userservice.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class OAuth2DataInitializer implements CommandLineRunner {
    private final RegisteredClientRepository registeredClientRepository;

    public OAuth2DataInitializer(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public void run(String... args) {
        // Check if client already exists
        RegisteredClient existingClient = registeredClientRepository.findByClientId("oidc-client");
        RegisteredClient oidcClient;
        if (existingClient == null) {
            oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("oidc-client")
                    .clientSecret("{noop}secret")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://127.0.0.1:8080/login/oauth2/code/oidc-client")
                    .redirectUri("https://oauth.pstmn.io/v1/callback")
                    .postLogoutRedirectUri("http://127.0.0.1:8080/")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(true)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofHours(1))
                            .refreshTokenTimeToLive(Duration.ofDays(1))
                            .build())
                    .build();

            registeredClientRepository.save(oidcClient);
            System.out.println("OAuth2 client 'oidc-client' initialized in database");
        } else {
            RegisteredClient.Builder builder = RegisteredClient.from(existingClient);
            if (!existingClient.getRedirectUris().contains("https://oauth.pstmn.io/v1/callback")) {
                builder.redirectUri("https://oauth.pstmn.io/v1/callback");
            }
            oidcClient = builder.build();
            registeredClientRepository.save(oidcClient);
            System.out.println("OAuth2 client 'oidc-client' updated in database");
        }
    }
}

