package com.ep18.couriersync.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// --- Imports que ya tenías ---
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

// --- !! AÑADE ESTOS DOS IMPORTS NUEVOS !! ---
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;


// --- Anotaciones que ya tenías ---
@Import(TestcontainersConfiguration.class) 
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
@SpringBootTest
class CouriersyncBackendApplicationTests {

    // --- !! AÑADE ESTA LÍNEA !! ---
    // Esto crea un JwtDecoder "falso" que no hace nada,
    // pero satisface la dependencia que pide SecurityConfig.
    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
    }
}