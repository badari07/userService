package com.example.userservice.configs;

import com.fasterxml.jackson.databind.Module;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

import java.util.List;

@Configuration
public class OAuth2JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer oauth2ObjectMapperCustomizer() {
        return builder -> {
            ClassLoader classLoader = OAuth2JacksonConfig.class.getClassLoader();
            List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
            List<Module> modules = new java.util.ArrayList<>(securityModules);
            modules.add(new OAuth2AuthorizationServerJackson2Module());
            builder.modulesToInstall(modules.toArray(new Module[0]));
        };
    }
}

