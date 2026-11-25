package cl.duoc.junta.finanzas.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.junta.finanzas.dto.Dtos.*;
import cl.duoc.junta.finanzas.error.ApiException;
import cl.duoc.junta.finanzas.model.*;
import cl.duoc.junta.finanzas.repository.MovimientoRepository;
import cl.duoc.junta.finanzas.security.Actor;

@Service
public class MovimientoService {

    public static final int MONTO_MAXIMO = 10_000_000;
    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final MovimientoRepository repo;
    private final ReservasClient reservas;
    private final NotificacionesClient avisos;

    public MovimientoService(MovimientoRepository repo, ReservasClient reservas, NotificacionesClient avisos) {
        this.repo = repo;
        this.reservas = reservas;
        this.avisos = avisos;
    }

    public List<MovimientoDto> listar(Actor actor) {
        return repo.findByEstadoOrderByFechaDescIdDesc(EstadoMovimiento.VIGENTE).stream()
            .map(m -> aDto(m, actor)).toList();
    }

    public MovimientoDto obtener(long id, Actor actor) {
        return aDto(buscarVigente(id), actor);
    }

    @Transactional
    public MovimientoDto crear(MovimientoRequest req, Actor actor, String authorization) {
        TipoMovimiento tipo = tipo(req);
        Datos d = validar(req, LocalDate.now(ZONA));

        if (req.reservaId() != null) {
            if (!reservas.existe(req.reservaId(), authorization)) {
                throw ApiException.notFound("La reserva indicada no existe");
            }
            if (tipo == TipoMovimiento.INGRESO && repo.existsByReservaIdAndTipoAndEstado(
                req.reservaId(), TipoMovimiento.INGRESO, EstadoMovimiento.VIGENTE)) {
                throw ApiException.conflict("La reserva ya tiene un pago registrado");
            }
        }
        Movimiento m = new Movimiento();
        m.setTipo(tipo);
        m.setReservaId(tipo == TipoMovimiento.INGRESO ? req.reservaId() : null);
        m.setRegistradoPor(actor.id());
        m.setEstado(EstadoMovimiento.VIGENTE);
        m.setFechaCreacion(System.currentTimeMillis());
        aplicar(m, d, req.comprobanteRef());
        Movimiento guardado = repo.save(m);
        avisarPago(guardado, authorization);
        return aDto(guardado, actor);
    }

    // Avisa al vecino que solicitó la reserva que su pago quedó registrado (mejor esfuerzo).
    private void avisarPago(Movimiento m, String authorization) {
        if (m.getTipo() != TipoMovimiento.INGRESO || m.getReservaId() == null) return;
        Long dueno = reservas.propietario(m.getReservaId(), authorization);
        if (dueno == null) return;
        avisos.avisarAUsuario(dueno, "Pago registrado",
            "Tesorería registró el pago de tu arriendo (" + m.getConcepto() + ") por $" + m.getMonto() + ".",
            "PAGO", m.getReservaId());
    }

    /** Corrige concepto, monto, categoría, fecha o comprobante (el tipo y la reserva no cambian). */
    @Transactional
    public MovimientoDto actualizar(long id, MovimientoRequest req, Actor actor) {
        Movimiento m = buscarVigente(id);
        Datos d = validar(req, LocalDate.now(ZONA));
        aplicar(m, d, req.comprobanteRef() != null ? req.comprobanteRef() : m.getComprobanteRef());
        return aDto(repo.save(m), actor);
    }

    /** "Eliminar" = anular: se conserva el registro, pero deja de contarse en los informes. */
    @Transactional
    public void anular(long id) {
        Movimiento m = buscarVigente(id);
        m.setEstado(EstadoMovimiento.ANULADO);
        repo.save(m);
    }

    private void aplicar(Movimiento m, Datos d, String comprobanteRef) {
        m.setMonto(d.monto());
        m.setConcepto(d.concepto());
        m.setCategoria(d.categoria());
        m.setFecha(d.fecha());
        m.setComprobanteRef(comprobanteRef == null || comprobanteRef.isBlank() ? null
            : comprobanteRef.trim().substring(0, Math.min(comprobanteRef.trim().length(), 120)));
    }

    /** Un vecino ve el movimiento, pero no los respaldos (comprobantes). */
    static MovimientoDto aDto(Movimiento m, Actor actor) {
        boolean ocultar = actor.esRol("VECINO");
        return new MovimientoDto(m.getId(), m.getTipo().name(), m.getMonto(), m.getConcepto(), m.getCategoria(),
            m.getFecha().toString(), m.getReservaId(), ocultar ? null : m.getComprobanteRef(), m.getRegistradoPor());
    }

    private Movimiento buscarVigente(long id) {
        Movimiento m = repo.findById(id).orElseThrow(() -> ApiException.notFound("El movimiento no existe"));
        if (m.getEstado() != EstadoMovimiento.VIGENTE) throw ApiException.notFound("El movimiento fue anulado");
        return m;
    }

    // ---------- Validación ----------
    record Datos(int monto, String concepto, String categoria, LocalDate fecha) {}

    static TipoMovimiento tipo(MovimientoRequest req) {
        try {
            return TipoMovimiento.valueOf(req.tipo());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw ApiException.badRequest("El tipo debe ser INGRESO o GASTO");
        }
    }

    static Datos validar(MovimientoRequest req, LocalDate hoy) {
        if (req == null) throw ApiException.badRequest("Faltan datos del movimiento");
        int monto = req.monto() == null ? 0 : req.monto();
        if (monto <= 0) throw ApiException.badRequest("El monto debe ser mayor a 0");
        if (monto > MONTO_MAXIMO) throw ApiException.badRequest("El monto máximo es " + MONTO_MAXIMO);

        String concepto = req.concepto() == null ? "" : req.concepto().trim();
        if (concepto.length() < 3 || concepto.length() > 60) {
            throw ApiException.badRequest("El concepto debe tener entre 3 y 60 caracteres");
        }
        String categoria = req.categoria() == null ? "" : req.categoria().trim();
        if (categoria.isEmpty() || categoria.length() > 30) {
            throw ApiException.badRequest("Indica una categoría (máximo 30 caracteres)");
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(req.fecha() == null ? "" : req.fecha().trim());
        } catch (DateTimeException e) {
            throw ApiException.badRequest("La fecha debe tener formato aaaa-MM-dd");
        }
        if (fecha.isAfter(hoy)) throw ApiException.badRequest("La fecha no puede ser posterior a hoy");
        return new Datos(monto, concepto, categoria, fecha);
    }
}
