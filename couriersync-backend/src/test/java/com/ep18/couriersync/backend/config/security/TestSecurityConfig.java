package com.ep18.couriersync.backend.config.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration(proxyBeanMethods = false)
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    // Proveer un JwtDecoder simple para satisfacer autoconfiguración de resource-server
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        // JwtDecoder mínimo que no se conecta a un issuer; suficiente para tests que no validan tokens.
        return NimbusJwtDecoder.withSecretKey(new javax.crypto.spec.SecretKeySpec("testtesttesttest".getBytes(), "HmacSHA256")).build();
    }
}