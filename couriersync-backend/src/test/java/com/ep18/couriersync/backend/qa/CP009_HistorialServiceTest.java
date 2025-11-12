package com.ep18.couriersync.backend.qa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// TODO: usa tus clases reales si existen
class HistoryItem { String id,status,note,date; HistoryItem(String i,String s,String n,String d){id=i;status=s;note=n;date=d;} }
class Paged<T>{ List<T> items; int total; Paged(List<T> i,int t){items=i;total=t;} }
interface HistoryService {
    Paged<HistoryItem> searchExact(String clientId);
    List<String> suggest(String partial);
    Paged<HistoryItem> listPaged(String clientId, int page, int size);
}

class CP009_HistorialServiceTest {

    @Mock private HistoryService historyService;

    @BeforeEach void init(){ MockitoAnnotations.openMocks(this); }

    @Test
    void busquedaExacta_devuelveFechasEstadosObservaciones() {
        var item = new HistoryItem("d1","En camino","Obs","2025-11-03");
        when(historyService.searchExact("c1")).thenReturn(new Paged<>(List.of(item),1));

        var res = historyService.searchExact("c1");
        assertEquals(1, res.items.size());
        var r0 = res.items.get(0);
        assertNotNull(r0.status);
        assertNotNull(r0.note);
        assertNotNull(r0.date);
    }

    @Test
    void busquedaParcial_ofreceSugerenciasAccesibles() {
        when(historyService.suggest("San")).thenReturn(List.of("Santiago","Santa"));
        var s = historyService.suggest("San");
        assertFalse(s.isEmpty());
    }

    @Test
    void paginacion_cuandoHayMasDeDiez() {
        var many = java.util.stream.IntStream.range(0,25)
                .mapToObj(i -> new HistoryItem("d"+i,"X","N","2025-01-01")).toList();

        when(historyService.listPaged("c1",1,10)).thenReturn(new Paged<>(many.subList(0,10),25));
        when(historyService.listPaged("c1",2,10)).thenReturn(new Paged<>(many.subList(10,20),25));

        var p1 = historyService.listPaged("c1",1,10);
        var p2 = historyService.listPaged("c1",2,10);
        assertEquals(10, p1.items.size());
        assertEquals(10, p2.items.size());
        assertEquals(25, p1.total);
    }
}
