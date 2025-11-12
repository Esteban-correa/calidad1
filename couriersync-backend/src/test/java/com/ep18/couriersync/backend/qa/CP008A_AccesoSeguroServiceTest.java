package com.ep18.couriersync.backend.qa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// TODO: si tienes un AuthService real, impórtalo y elimina esta interfaz
interface AuthService {
    record LoginResult(boolean ok, String role, String panel, long elapsedMs) {}
    LoginResult login(String name);
}

class CP008A_AccesoSeguroServiceTest {

    @Mock private AuthService authService;

    @BeforeEach void init(){ MockitoAnnotations.openMocks(this); }

    @Test
    void accesoPorRol_y_tiempoMenorIgual2s() {
        when(authService.login("Admin"))
                .thenReturn(new AuthService.LoginResult(true,"admin","admin",1800));
        when(authService.login("Agente"))
                .thenReturn(new AuthService.LoginResult(true,"agent","agent",900));

        var admin = authService.login("Admin");
        assertEquals("admin", admin.panel());
        assertTrue(admin.elapsedMs() <= 2000);

        var agente = authService.login("Agente");
        assertEquals("agent", agente.panel());
    }
}
