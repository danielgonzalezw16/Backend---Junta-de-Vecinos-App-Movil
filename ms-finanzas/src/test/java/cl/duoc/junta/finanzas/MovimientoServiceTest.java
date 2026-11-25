package cl.duoc.junta.finanzas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.duoc.junta.finanzas.dto.Dtos.MovimientoRequest;
import cl.duoc.junta.finanzas.error.ApiException;
import cl.duoc.junta.finanzas.model.Movimiento;
import cl.duoc.junta.finanzas.repository.MovimientoRepository;
import cl.duoc.junta.finanzas.security.Actor;
import cl.duoc.junta.finanzas.service.MovimientoService;
import cl.duoc.junta.finanzas.service.NotificacionesClient;
import cl.duoc.junta.finanzas.service.ReservasClient;

class MovimientoServiceTest {

    private MovimientoRepository repo;
    private ReservasClient reservas;
    private MovimientoService service;
    private final Actor tesoreria = new Actor(3, "Ana Muñoz", "TESORERIA");
    private final Actor vecino = new Actor(1, "Camila Rojas", "VECINO");

    @BeforeEach
    void preparar() {
        repo = mock(MovimientoRepository.class);
        reservas = mock(ReservasClient.class);
        service = new MovimientoService(repo, reservas, mock(NotificacionesClient.class));
        when(repo.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            if (m.getId() == null) m.setId(1L);
            return m;
        });
    }

    private MovimientoRequest gasto(int monto) {
        return new MovimientoRequest("GASTO", monto, "Cuenta de luz", "Luz", LocalDate.now().toString(), null, "boleta.jpg");
    }

    @Test
    void registraGastoValido() {
        var dto = service.crear(gasto(28500), tesoreria, "Bearer x");
        assertEquals("GASTO", dto.tipo());
        assertEquals(28500, dto.monto());
        assertEquals("boleta.jpg", dto.comprobanteRef());
    }

    @Test
    void vecinoNoVeElComprobante() {
        var dto = service.crear(gasto(1000), tesoreria, "Bearer x");
        Movimiento m = new Movimiento(1L, cl.duoc.junta.finanzas.model.TipoMovimiento.GASTO, 1000, "Luz", "Luz",
            LocalDate.now(), null, "boleta.jpg", 3L, cl.duoc.junta.finanzas.model.EstadoMovimiento.VIGENTE, 1L);
        when(repo.findById(1L)).thenReturn(java.util.Optional.of(m));
        assertNull(service.obtener(1L, vecino).comprobanteRef());
        assertNotNull(dto);
    }

    @Test
    void rechazaMontoCeroONegativo() {
        assertThrows(ApiException.class, () -> service.crear(gasto(0), tesoreria, "Bearer x"));
    }

    @Test
    void rechazaFechaFutura() {
        var req = new MovimientoRequest("GASTO", 1000, "Gasto", "Luz", LocalDate.now().plusDays(5).toString(), null, null);
        assertThrows(ApiException.class, () -> service.crear(req, tesoreria, "Bearer x"));
    }

    @Test
    void pagoDeReservaInexistenteSeRechaza() {
        when(reservas.existe(anyLong(), anyString())).thenReturn(false);
        var req = new MovimientoRequest("INGRESO", 35000, "Arriendo sede", "Arriendo", LocalDate.now().toString(), 99L, null);
        ApiException e = assertThrows(ApiException.class, () -> service.crear(req, tesoreria, "Bearer x"));
        assertEquals(404, e.getEstado().value());
    }

    @Test
    void noSePagaDosVecesLaMismaReserva() {
        when(reservas.existe(anyLong(), anyString())).thenReturn(true);
        when(repo.existsByReservaIdAndTipoAndEstado(any(), any(), any())).thenReturn(true);
        var req = new MovimientoRequest("INGRESO", 35000, "Arriendo sede", "Arriendo", LocalDate.now().toString(), 5L, null);
        ApiException e = assertThrows(ApiException.class, () -> service.crear(req, tesoreria, "Bearer x"));
        assertEquals(409, e.getEstado().value());
    }
}
