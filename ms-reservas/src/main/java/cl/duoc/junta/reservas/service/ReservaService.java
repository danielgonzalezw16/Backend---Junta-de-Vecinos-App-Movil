package cl.duoc.junta.reservas.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.junta.reservas.dto.Dtos.*;
import cl.duoc.junta.reservas.error.ApiException;
import cl.duoc.junta.reservas.model.*;
import cl.duoc.junta.reservas.repository.*;
import cl.duoc.junta.reservas.security.Actor;

@Service
public class ReservaService {

    public static final String HORA_APERTURA = "08:00";
    public static final String HORA_CIERRE = "23:00";
    private static final Pattern HORA = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final ZoneId ZONA = ZoneId.of("America/Santiago");
    private static final List<EstadoReserva> ACTIVAS = List.of(EstadoReserva.PENDIENTE, EstadoReserva.APROBADA);
    private static final long DEPENDENCIA_POR_DEFECTO = 1L;

    private final ReservaRepository reservas;
    private final DependenciaRepository dependencias;
    private final HistorialReservaRepository historial;
    private final NotificacionesClient avisos;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ReservaService(ReservaRepository reservas, DependenciaRepository dependencias,
                          HistorialReservaRepository historial, NotificacionesClient avisos) {
        this.reservas = reservas;
        this.dependencias = dependencias;
        this.historial = historial;
        this.avisos = avisos;
    }

    // ---------- Lectura ----------
    public List<ReservaDto> listar(Actor actor) {
        return reservas.findAllByOrderByFechaDescHoraInicioAsc().stream().map(r -> aDto(r, actor)).toList();
    }

    public ReservaDto obtener(long id, Actor actor) {
        Reserva r = buscar(id);
        if (actor.esRol("VECINO") && !r.getUsuarioId().equals(actor.id())) {
            throw ApiException.forbidden("No puedes ver el detalle de la reserva de otro vecino");
        }
        return aDto(r, actor);
    }

    public boolean existe(long id) {
        return reservas.existsById(id);
    }

    /** Id del usuario dueño de la reserva (lo usa ms-finanzas para avisarle del pago). */
    public long propietario(long id) {
        return buscar(id).getUsuarioId();
    }

    public List<DependenciaDto> listarDependencias() {
        return dependencias.findByActivaTrue().stream()
            .map(d -> new DependenciaDto(d.getId(), d.getNombre(), d.getDescripcion(), d.getUbicacion(),
                d.getCapacidadReferencial()))
            .toList();
    }

    public List<HistorialDto> historial(long id) {
        buscar(id);
        return historial.findByReservaIdOrderByFechaCambioAsc(id).stream()
            .map(h -> new HistorialDto(h.getId(), h.getEstadoAnterior(), h.getEstadoNuevo(),
                h.getUsuarioResponsable(), h.getObservacion(), h.getFechaCambio()))
            .toList();
    }

    // ---------- Escritura ----------
    @Transactional
    public ReservaDto crear(ReservaRequest req, Actor actor) {
        Datos d = validar(req);
        long dependenciaId = dependenciaValida(req.dependenciaId());
        verificarDisponibilidad(dependenciaId, d, -1L);

        long ahora = System.currentTimeMillis();
        Reserva r = new Reserva();
        r.setUsuarioId(actor.id());
        r.setSolicitante(actor.nombre());
        r.setDependenciaId(dependenciaId);
        aplicar(r, d);
        r.setEstado(EstadoReserva.PENDIENTE);
        r.setFechaCreacion(ahora);
        r.setFechaActualizacion(ahora);
        reservas.save(r);
        registrarHistorial(r.getId(), null, EstadoReserva.PENDIENTE, actor, "Solicitud creada");
        avisos.avisarARol("SECRETARIA", "Nueva solicitud de arriendo",
            actor.nombre() + " solicitó la sede para el " + r.getFecha().format(FECHA) + " de " + r.getHoraInicio()
                + " a " + r.getHoraFin() + ".", "RESERVA", r.getId());
        return aDto(r, actor);
    }

    /** El solicitante puede editar mientras esté PENDIENTE; la directiva puede editar cualquiera. */
    @Transactional
    public ReservaDto actualizar(long id, ReservaRequest req, Actor actor) {
        Reserva r = buscar(id);
        verificarPermisoDeEdicion(r, actor);
        Datos d = validar(req);
        verificarDisponibilidad(r.getDependenciaId(), d, r.getId());
        aplicar(r, d);
        r.setFechaActualizacion(System.currentTimeMillis());
        reservas.save(r);
        return aDto(r, actor);
    }

    /** Aprobar o rechazar (solo directiva, ver controlador). Solo se resuelve una solicitud PENDIENTE. */
    @Transactional
    public ReservaDto cambiarEstado(long id, EstadoRequest req, Actor actor) {
        EstadoReserva nuevo;
        try {
            nuevo = EstadoReserva.valueOf(req.estado());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw ApiException.badRequest("Estado no válido");
        }
        if (nuevo == EstadoReserva.PENDIENTE) {
            throw ApiException.badRequest("Solo se puede aprobar o rechazar una solicitud");
        }
        Reserva r = buscar(id);
        if (r.getEstado() != EstadoReserva.PENDIENTE) {
            throw ApiException.conflict("La solicitud ya fue resuelta");
        }
        EstadoReserva anterior = r.getEstado();
        r.setEstado(nuevo);
        r.setFechaActualizacion(System.currentTimeMillis());
        reservas.save(r);
        registrarHistorial(r.getId(), anterior, nuevo, actor, req.observacion());
        String resultado = nuevo == EstadoReserva.APROBADA ? "aprobada" : "rechazada";
        avisos.avisarAUsuario(r.getUsuarioId(), "Reserva " + resultado,
            "Tu solicitud para el " + r.getFecha().format(FECHA) + " fue " + resultado + " por la directiva.",
            "RESERVA", r.getId());
        return aDto(r, actor);
    }

    @Transactional
    public void eliminar(long id, Actor actor) {
        Reserva r = buscar(id);
        verificarPermisoDeEdicion(r, actor);
        historial.deleteByReservaId(id);
        reservas.delete(r);
        if (!r.getUsuarioId().equals(actor.id())) {
            avisos.avisarAUsuario(r.getUsuarioId(), "Reserva eliminada",
                "La directiva eliminó tu reserva del " + r.getFecha().format(FECHA) + ".", "RESERVA", null);
        }
    }

    // ---------- Reglas ----------
    private void verificarPermisoDeEdicion(Reserva r, Actor actor) {
        if (actor.esRol("SECRETARIA")) return;
        if (!r.getUsuarioId().equals(actor.id())) {
            throw ApiException.forbidden("Solo el solicitante puede modificar su reserva");
        }
        if (r.getEstado() != EstadoReserva.PENDIENTE) {
            throw ApiException.conflict("Solo se puede modificar una solicitud pendiente");
        }
    }

    private void verificarDisponibilidad(long dependenciaId, Datos d, long excluirId) {
        long conflictos = reservas.contarConflictos(dependenciaId, d.fecha(), d.horaInicio(), d.horaFin(),
            ACTIVAS, excluirId);
        if (conflictos > 0) {
            throw ApiException.conflict("Ese horario ya está reservado o tiene una solicitud pendiente");
        }
    }

    private long dependenciaValida(Long id) {
        long dep = id == null ? DEPENDENCIA_POR_DEFECTO : id;
        Dependencia d = dependencias.findById(dep).orElseThrow(() -> ApiException.badRequest("La dependencia no existe"));
        if (!d.isActiva()) throw ApiException.badRequest("La dependencia no está disponible");
        return dep;
    }

    private void aplicar(Reserva r, Datos d) {
        r.setFecha(d.fecha());
        r.setHoraInicio(d.horaInicio());
        r.setHoraFin(d.horaFin());
        r.setPersonas(d.personas());
        r.setMotivo(d.motivo());
        r.setRequiereSonido(d.requiereSonido());
    }

    private void registrarHistorial(Long reservaId, EstadoReserva anterior, EstadoReserva nuevo, Actor actor, String obs) {
        HistorialReserva h = new HistorialReserva();
        h.setReservaId(reservaId);
        h.setEstadoAnterior(anterior == null ? null : anterior.name());
        h.setEstadoNuevo(nuevo.name());
        h.setUsuarioResponsable(actor.nombre());
        h.setObservacion(obs == null || obs.isBlank() ? null : obs.trim());
        h.setFechaCambio(System.currentTimeMillis());
        historial.save(h);
    }

    /** Un vecino solo ve el detalle de sus reservas; de las ajenas solo el horario ocupado. */
    static ReservaDto aDto(Reserva r, Actor actor) {
        boolean ocultar = actor.esRol("VECINO") && !r.getUsuarioId().equals(actor.id());
        return new ReservaDto(r.getId(), r.getUsuarioId(),
            ocultar ? "Otro vecino" : r.getSolicitante(), r.getDependenciaId(), r.getFecha().toString(),
            r.getHoraInicio(), r.getHoraFin(), ocultar ? 0 : r.getPersonas(), ocultar ? "" : r.getMotivo(),
            !ocultar && r.isRequiereSonido(), r.getEstado().name(), r.getFechaCreacion());
    }

    private Reserva buscar(long id) {
        return reservas.findById(id).orElseThrow(() -> ApiException.notFound("La reserva no existe"));
    }

    // ---------- Validación ----------
    record Datos(LocalDate fecha, String horaInicio, String horaFin, int personas, String motivo, boolean requiereSonido) {}

    static Datos validar(ReservaRequest req) {
        return validar(req, LocalDate.now(ZONA));
    }

    static Datos validar(ReservaRequest req, LocalDate hoy) {
        if (req == null) throw ApiException.badRequest("Faltan datos de la reserva");
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(req.fecha() == null ? "" : req.fecha().trim());
        } catch (DateTimeException e) {
            throw ApiException.badRequest("La fecha debe tener formato aaaa-MM-dd");
        }
        if (fecha.isBefore(hoy)) throw ApiException.badRequest("La fecha no puede ser anterior a hoy");

        String inicio = req.horaInicio() == null ? "" : req.horaInicio().trim();
        String fin = req.horaFin() == null ? "" : req.horaFin().trim();
        if (!HORA.matcher(inicio).matches() || !HORA.matcher(fin).matches()) {
            throw ApiException.badRequest("Las horas deben tener formato HH:mm");
        }
        if (inicio.compareTo(HORA_APERTURA) < 0 || fin.compareTo(HORA_CIERRE) > 0) {
            throw ApiException.badRequest("Horario permitido: " + HORA_APERTURA + " a " + HORA_CIERRE);
        }
        if (fin.compareTo(inicio) <= 0) throw ApiException.badRequest("La hora de término debe ser posterior a la de inicio");

        int personas = req.personas() == null ? 0 : req.personas();
        if (personas < 1 || personas > 80) throw ApiException.badRequest("El N° de personas debe estar entre 1 y 80");

        String motivo = req.motivo() == null ? "" : req.motivo().trim();
        if (motivo.length() < 5 || motivo.length() > 100) {
            throw ApiException.badRequest("El motivo debe tener entre 5 y 100 caracteres");
        }
        return new Datos(fecha, inicio, fin, personas, motivo, Boolean.TRUE.equals(req.requiereSonido()));
    }
}
