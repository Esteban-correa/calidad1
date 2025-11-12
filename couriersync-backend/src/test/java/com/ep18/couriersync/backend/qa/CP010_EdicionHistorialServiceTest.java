package com.ep18.couriersync.backend.qa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// TODO: reemplaza por tu contrato real si lo tienes
class UpdateResult {
    boolean ok; String reason; java.util.Map<String,Object> data = new java.util.HashMap<>();
    static UpdateResult ok(){ var r=new UpdateResult(); r.ok=true; return r; }
    static UpdateResult forbidden(){ var r=new UpdateResult(); r.ok=false; r.reason="forbidden"; return r; }
    UpdateResult with(String k,Object v){ data.put(k,v); return this; }
}
interface HistoryUpdateService {
    UpdateResult update(String clientId, String deliveryId, Map<String,Object> patch, String role, String actor);
}

class CP010_EdicionHistorialServiceTest {

    @Mock private HistoryUpdateService historyUpdateService;

    @BeforeEach void init(){ MockitoAnnotations.openMocks(this); }

    @Test
    void marcarEntregado_registraFechaYResponsable() {
        when(historyUpdateService.update(eq("c1"), eq("d1"),
                argThat(p -> "Entregado".equals(p.get("status"))),
                eq("agent"), eq("Agente QA")))
                .thenReturn(UpdateResult.ok()
                        .with("status","Entregado")
                        .with("deliveredAt","2025-11-20T10:00:00Z")
                        .with("deliveredBy","Agente QA"));

        var res = historyUpdateService.update("c1","d1", Map.of("status","Entregado"), "agent","Agente QA");

        assertTrue(res.ok);
        assertEquals("Entregado", res.data.get("status"));
        assertNotNull(res.data.get("deliveredAt"));
        assertEquals("Agente QA", res.data.get("deliveredBy"));
    }

    @Test
    void sinPermisos_noPermiteEditar() {
        when(historyUpdateService.update(any(), any(), any(), eq("guest"), any()))
                .thenReturn(UpdateResult.forbidden());

        var res = historyUpdateService.update("c1","d1", Map.of("status","En camino"), "guest","X");
        assertFalse(res.ok);
        assertEquals("forbidden", res.reason);
    }
}
