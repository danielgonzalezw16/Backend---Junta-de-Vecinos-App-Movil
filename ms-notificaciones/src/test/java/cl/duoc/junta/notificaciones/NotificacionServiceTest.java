package cl.duoc.junta.notificaciones;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.duoc.junta.notificaciones.dto.Dtos.NuevaNotificacion;
import cl.duoc.junta.notificaciones.error.ApiException;
import cl.duoc.junta.notificaciones.model.Notificacion;
import cl.duoc.junta.notificaciones.repository.NotificacionRepository;
import cl.duoc.junta.notificaciones.security.Actor;
import cl.duoc.junta.notificaciones.service.NotificacionService;

class NotificacionServiceTest {

    private NotificacionRepository repo;
    private NotificacionService service;
    private final Actor vecino = new Actor(1, "Camila Rojas", "VECINO");

    @BeforeEach
    void preparar() {
        repo = mock(NotificacionRepository.class);
        service = new NotificacionService(repo);
        when(repo.save(any(Notificacion.class))).thenAnswer(inv -> {
            Notificacion n = inv.getArgument(0);
            if (n.getId() == null) n.setId(1L);
            return n;
        });
    }

    @Test
    void creaAvisoParaUnUsuario() {
        var dto = service.crear(new NuevaNotificacion(1L, null, "Reserva aprobada", "Tu reserva fue aprobada", "reserva", 10L));
        assertEquals("RESERVA", dto.tipo());
        assertFalse(dto.leida());
        assertEquals(10L, dto.referenciaId());
    }

    @Test
    void exigeUnSoloDestinatario() {
        assertThrows(ApiException.class, () -> service.crear(new NuevaNotificacion(null, null, "t", "m", "OTRO", null)));
        assertThrows(ApiException.class, () -> service.crear(new NuevaNotificacion(1L, "VECINO", "t", "m", "OTRO", null)));
        assertThrows(ApiException.class, () -> service.crear(new NuevaNotificacion(null, "ADMIN", "t", "m", "OTRO", null)));
    }

    @Test
    void marcaComoLeidaSoloLoPropio() {
        Notificacion propia = new Notificacion(5L, 1L, null, "t", "m", "RESERVA", 10L, 1L, false);
        Notificacion ajena = new Notificacion(6L, 9L, null, "t", "m", "RESERVA", 10L, 1L, false);
        when(repo.findById(5L)).thenReturn(Optional.of(propia));
        when(repo.findById(6L)).thenReturn(Optional.of(ajena));

        assertTrue(service.marcarLeida(5L, vecino).leida());
        assertThrows(ApiException.class, () -> service.marcarLeida(6L, vecino));
    }

    @Test
    void avisoPorPerfilLoVenTodosLosDeEsePerfil() {
        Notificacion paraDirectiva = new Notificacion(7L, null, "SECRETARIA", "t", "m", "RESERVA", 10L, 1L, false);
        when(repo.findById(7L)).thenReturn(Optional.of(paraDirectiva));
        assertTrue(service.marcarLeida(7L, new Actor(2, "Pedro Soto", "SECRETARIA")).leida());
        assertThrows(ApiException.class, () -> service.marcarLeida(7L, vecino));
    }

    @Test
    void marcarTodasLasLeidas() {
        Notificacion a = new Notificacion(1L, 1L, null, "t", "m", "OTRO", null, 1L, false);
        Notificacion b = new Notificacion(2L, 1L, null, "t", "m", "OTRO", null, 2L, false);
        when(repo.findTop50ByUsuarioIdOrRolOrderByFechaDescIdDesc(1L, "VECINO")).thenReturn(List.of(a, b));
        service.marcarTodasLeidas(vecino);
        assertTrue(a.isLeida() && b.isLeida());
    }
}
