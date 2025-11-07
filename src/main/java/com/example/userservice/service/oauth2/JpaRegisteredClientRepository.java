package com.example.userservice.service.oauth2;

import com.example.userservice.models.oauth2.Client;
import com.example.userservice.repository.oauth2.ClientRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JpaRegisteredClientRepository implements RegisteredClientRepository {
    private final ClientRepository clientRepository;
    private final ObjectMapper objectMapper;
    private static final ObjectMapper FALLBACK_OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    public JpaRegisteredClientRepository(ClientRepository clientRepository, ObjectMapper objectMapper) {
        Assert.notNull(clientRepository, "clientRepository cannot be null");
        Assert.notNull(objectMapper, "objectMapper cannot be null");
        this.clientRepository = clientRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Assert.notNull(registeredClient, "registeredClient cannot be null");
        this.clientRepository.save(toEntity(registeredClient));
    }

    @Override
    public RegisteredClient findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        return this.clientRepository.findById(id)
                .map(this::toObject)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Assert.hasText(clientId, "clientId cannot be empty");
        return this.clientRepository.findByClientId(clientId)
                .map(this::toObject)
                .orElse(null);
    }

    private RegisteredClient toObject(Client client) {
        Set<String> clientAuthenticationMethods = toTrimmedSet(client.getClientAuthenticationMethods());
        Set<String> authorizationGrantTypes = toTrimmedSet(client.getAuthorizationGrantTypes());
        Set<String> redirectUris = toTrimmedSet(client.getRedirectUris());
        Set<String> postLogoutRedirectUris = toTrimmedSet(client.getPostLogoutRedirectUris());
        Set<String> clientScopes = toTrimmedSet(client.getScopes());

        RegisteredClient.Builder builder = RegisteredClient.withId(client.getId())
                .clientId(client.getClientId())
                .clientIdIssuedAt(client.getClientIdIssuedAt())
                .clientSecret(client.getClientSecret())
                .clientSecretExpiresAt(client.getClientSecretExpiresAt())
                .clientName(client.getClientName())
                .clientAuthenticationMethods(authenticationMethods ->
                        clientAuthenticationMethods.forEach(authenticationMethod ->
                                authenticationMethods.add(resolveClientAuthenticationMethod(authenticationMethod))))
                .authorizationGrantTypes((grantTypes) ->
                        authorizationGrantTypes.forEach(grantType ->
                                grantTypes.add(resolveAuthorizationGrantType(grantType))))
                .redirectUris((uris) -> uris.addAll(redirectUris))
                .postLogoutRedirectUris((uris) -> uris.addAll(postLogoutRedirectUris))
                .scopes((scopes) -> scopes.addAll(clientScopes));

        Map<String, Object> clientSettingsMap = parseMap(client.getClientSettings());
        ClientSettings clientSettings = clientSettingsMap.isEmpty()
                ? ClientSettings.builder().build()
                : ClientSettings.withSettings(normalizeSettings(clientSettingsMap)).build();
        builder.clientSettings(clientSettings);

        Map<String, Object> tokenSettingsMap = parseMap(client.getTokenSettings());
        TokenSettings tokenSettings = tokenSettingsMap.isEmpty()
                ? TokenSettings.builder().build()
                : TokenSettings.withSettings(normalizeSettings(tokenSettingsMap)).build();
        builder.tokenSettings(tokenSettings);

        return builder.build();
    }

    private Client toEntity(RegisteredClient registeredClient) {
        Client entity = new Client();
        entity.setId(registeredClient.getId());
        entity.setClientId(registeredClient.getClientId());
        entity.setClientIdIssuedAt(registeredClient.getClientIdIssuedAt());
        entity.setClientSecret(registeredClient.getClientSecret());
        entity.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        entity.setClientName(registeredClient.getClientName());

        Set<String> clientAuthenticationMethods = new HashSet<>();
        registeredClient.getClientAuthenticationMethods().forEach(clientAuthenticationMethod ->
                clientAuthenticationMethods.add(clientAuthenticationMethod.getValue()));
        entity.setClientAuthenticationMethods(joinCommaDelimited(clientAuthenticationMethods));

        Set<String> authorizationGrantTypes = new HashSet<>();
        registeredClient.getAuthorizationGrantTypes().forEach(authorizationGrantType ->
                authorizationGrantTypes.add(authorizationGrantType.getValue()));
        entity.setAuthorizationGrantTypes(joinCommaDelimited(authorizationGrantTypes));

        entity.setRedirectUris(joinCommaDelimited(registeredClient.getRedirectUris()));
        entity.setPostLogoutRedirectUris(joinCommaDelimited(registeredClient.getPostLogoutRedirectUris()));
        entity.setScopes(joinCommaDelimited(registeredClient.getScopes()));
        entity.setClientSettings(writeMap(registeredClient.getClientSettings().getSettings()));
        entity.setTokenSettings(writeMap(registeredClient.getTokenSettings().getSettings()));

        return entity;
    }

    private Map<String, Object> parseMap(String data) {
        if (!StringUtils.hasText(data)) {
            return new LinkedHashMap<>();
        }
        try {
            return this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
        } catch (com.fasterxml.jackson.databind.exc.InvalidTypeIdException ex) {
            try {
                return FALLBACK_OBJECT_MAPPER.readValue(data, new TypeReference<Map<String, Object>>() {});
            } catch (Exception nested) {
                throw new IllegalArgumentException(nested.getMessage(), nested);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private String writeMap(Map<String, Object> data) {
        try {
            return this.objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private static Set<String> toTrimmedSet(String value) {
        if (!StringUtils.hasText(value)) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String joinCommaDelimited(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));
    }

    private static final Set<String> DURATION_SETTING_KEYS = new HashSet<>(Arrays.asList(
            "access_token.time-to-live",
            "refresh_token.time-to-live",
            "authorization_code.time-to-live",
            "device_code.time-to-live"
    ));

    private static final Set<String> BOOLEAN_SETTING_KEYS = new HashSet<>(Arrays.asList(
            "reuse_refresh_tokens",
            "require_proof_key",
            "require_authorization_consent"
    ));

    private static final Set<String> TOKEN_FORMAT_KEYS = new HashSet<>(Collections.singletonList("access_token.format"));

    private static Map<String, Object> normalizeSettings(Map<String, Object> source) {
        Map<String, Object> target = new LinkedHashMap<>(source.size());
        source.forEach((key, value) -> target.put(key, normalizeValue(key, value)));
        return target;
    }

    private static Object normalizeValue(String key, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> nestedMap) {
            Map<String, Object> nested = new LinkedHashMap<>();
            nestedMap.forEach((nestedKey, nestedValue) ->
                    nested.put(String.valueOf(nestedKey), normalizeValue(String.valueOf(nestedKey), nestedValue)));
            return nested;
        }
        if (DURATION_SETTING_KEYS.contains(key)) {
            Duration duration = toDuration(value);
            return duration != null ? duration : value;
        }
        if (BOOLEAN_SETTING_KEYS.contains(key)) {
            Boolean bool = toBoolean(value);
            return bool != null ? bool : value;
        }
        if (TOKEN_FORMAT_KEYS.contains(key)) {
            OAuth2TokenFormat format = toTokenFormat(value);
            return format != null ? format : value;
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            Duration duration = toDuration(str);
            if (duration != null) {
                return duration;
            }
            Boolean bool = toBoolean(str);
            if (bool != null) {
                return bool;
            }
            if (TOKEN_FORMAT_KEYS.contains(key)) {
                OAuth2TokenFormat format = toTokenFormat(str);
                if (format != null) {
                    return format;
                }
            }
        }
        return value;
    }

    private static Duration toDuration(Object value) {
        if (value instanceof Duration duration) {
            return duration;
        }
        if (value instanceof Number number) {
            return Duration.ofSeconds(number.longValue());
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            try {
                return Duration.parse(str);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static Boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
                return Boolean.parseBoolean(str);
            }
        }
        return null;
    }

    private static OAuth2TokenFormat toTokenFormat(Object value) {
        if (value instanceof OAuth2TokenFormat format) {
            return format;
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            return new OAuth2TokenFormat(str);
        }
        return null;
    }

    private static ClientAuthenticationMethod resolveClientAuthenticationMethod(String clientAuthenticationMethod) {
        if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
        } else if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_POST;
        } else if (ClientAuthenticationMethod.PRIVATE_KEY_JWT.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.PRIVATE_KEY_JWT;
        } else if (ClientAuthenticationMethod.CLIENT_SECRET_JWT.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_JWT;
        }
        return new ClientAuthenticationMethod(clientAuthenticationMethod);
    }

    private static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.CLIENT_CREDENTIALS;
        } else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.REFRESH_TOKEN;
        } else if (AuthorizationGrantType.DEVICE_CODE.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.DEVICE_CODE;
        }
        return new AuthorizationGrantType(authorizationGrantType);
    }
}

