package cl.duoc.junta.reservas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.duoc.junta.reservas.dto.Dtos.ReservaRequest;
import cl.duoc.junta.reservas.error.ApiException;
import cl.duoc.junta.reservas.model.Dependencia;
import cl.duoc.junta.reservas.model.EstadoReserva;
import cl.duoc.junta.reservas.model.Reserva;
import cl.duoc.junta.reservas.repository.*;
import cl.duoc.junta.reservas.security.Actor;
import cl.duoc.junta.reservas.service.NotificacionesClient;
import cl.duoc.junta.reservas.service.ReservaService;

class ReservaServiceTest {

    private ReservaRepository reservas;
    private DependenciaRepository dependencias;
    private HistorialReservaRepository historial;
    private ReservaService service;
    private final Actor vecino = new Actor(1, "Camila Rojas", "VECINO");
    private final Actor otro = new Actor(9, "Otro Vecino", "VECINO");

    @BeforeEach
    void preparar() {
        reservas = mock(ReservaRepository.class);
        dependencias = mock(DependenciaRepository.class);
        historial = mock(HistorialReservaRepository.class);
        service = new ReservaService(reservas, dependencias, historial, mock(NotificacionesClient.class));
        Dependencia sede = new Dependencia(1L, "Sede", "", "", 80, true);
        when(dependencias.findById(1L)).thenReturn(Optional.of(sede));
        when(reservas.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            if (r.getId() == null) r.setId(10L);
            return r;
        });
    }

    private ReservaRequest peticion(String inicio, String fin) {
        return new ReservaRequest(LocalDate.now().plusDays(2).toString(), inicio, fin, 20, "Cumpleaños", false, null);
    }

    @Test
    void creaReservaPendiente() {
        when(reservas.contarConflictos(anyLong(), any(), any(), any(), any(), anyLong())).thenReturn(0L);
        var dto = service.crear(peticion("18:00", "22:00"), vecino);
        assertEquals("PENDIENTE", dto.estado());
        assertEquals(1L, dto.usuarioId());
    }

    @Test
    void rechazaHorarioSuperpuesto() {
        when(reservas.contarConflictos(anyLong(), any(), any(), any(), any(), anyLong())).thenReturn(1L);
        ApiException e = assertThrows(ApiException.class, () -> service.crear(peticion("18:00", "22:00"), vecino));
        assertEquals(409, e.getEstado().value());
    }

    @Test
    void rechazaHorarioFueraDeApertura() {
        assertThrows(ApiException.class, () -> service.crear(peticion("06:00", "10:00"), vecino));
    }

    @Test
    void rechazaFinAnteriorAlInicio() {
        assertThrows(ApiException.class, () -> service.crear(peticion("20:00", "19:00"), vecino));
    }

    @Test
    void vecinoNoVeDatosPersonalesDeOtraReserva() {
        Reserva r = new Reserva(5L, 1L, "Camila Rojas", 1L, LocalDate.now().plusDays(1), "18:00", "20:00", 30,
            "Motivo privado", true, EstadoReserva.APROBADA, 1L, 1L);
        when(reservas.findAllByOrderByFechaDescHoraInicioAsc()).thenReturn(java.util.List.of(r));
        var visto = service.listar(otro).get(0);
        assertEquals("", visto.motivo());
        assertEquals("Otro vecino", visto.solicitante());
        var propio = service.listar(vecino).get(0);
        assertEquals("Motivo privado", propio.motivo());
    }

    @Test
    void soloSeResuelveUnaSolicitudPendiente() {
        Reserva r = new Reserva(5L, 1L, "Camila Rojas", 1L, LocalDate.now().plusDays(1), "18:00", "20:00", 30,
            "Motivo", false, EstadoReserva.APROBADA, 1L, 1L);
        when(reservas.findById(5L)).thenReturn(Optional.of(r));
        var directiva = new Actor(2, "Pedro Soto", "SECRETARIA");
        assertThrows(ApiException.class, () -> service.cambiarEstado(5L,
            new cl.duoc.junta.reservas.dto.Dtos.EstadoRequest("RECHAZADA", null), directiva));
    }
}
