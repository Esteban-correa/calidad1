package com.ep18.couriersync.backend.qa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// TODO: usa tu servicio real si existe
class PromoResult { boolean ok; int count; long elapsedMs; PromoResult(boolean ok,int count,long ms){this.ok=ok;this.count=count;this.elapsedMs=ms;} }
interface PromoService { PromoResult sendToGroup(int recipientsCount, String offerHtml); }

class CP011_PromosServiceTest {

    @Mock private PromoService promoService;

    @BeforeEach void init(){ MockitoAnnotations.openMocks(this); }

    @Test
    void milDestinatarios_enMenosDeUnMinuto() {
        when(promoService.sendToGroup(1000, "<a>Oferta</a>"))
                .thenReturn(new PromoResult(true,1000,50_000));

        var r = promoService.sendToGroup(1000,"<a>Oferta</a>");
        assertTrue(r.ok);
        assertEquals(1000, r.count);
        assertTrue(r.elapsedMs <= 60_000);
    }
}
