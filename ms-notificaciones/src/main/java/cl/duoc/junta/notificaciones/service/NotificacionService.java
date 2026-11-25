package cl.duoc.junta.notificaciones.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.junta.notificaciones.dto.Dtos.*;
import cl.duoc.junta.notificaciones.error.ApiException;
import cl.duoc.junta.notificaciones.model.Notificacion;
import cl.duoc.junta.notificaciones.repository.NotificacionRepository;
import cl.duoc.junta.notificaciones.security.Actor;

@Service
public class NotificacionService {

    private static final List<String> ROLES = List.of("VECINO", "SECRETARIA", "TESORERIA");

    private final NotificacionRepository repo;

    public NotificacionService(NotificacionRepository repo) {
        this.repo = repo;
    }

    public List<NotificacionDto> listar(Actor actor) {
        return visibles(actor).stream().map(NotificacionService::aDto).toList();
    }

    @Transactional
    public NotificacionDto marcarLeida(long id, Actor actor) {
        Notificacion n = propia(id, actor);
        n.setLeida(true);
        return aDto(repo.save(n));
    }

    @Transactional
    public void marcarTodasLeidas(Actor actor) {
        List<Notificacion> pendientes = visibles(actor).stream().filter(n -> !n.isLeida()).toList();
        pendientes.forEach(n -> n.setLeida(true));
        repo.saveAll(pendientes);
    }

    @Transactional
    public void eliminar(long id, Actor actor) {
        repo.delete(propia(id, actor));
    }

    /** Alta de un aviso desde otro microservicio (la clave interna se valida en el controlador). */
    @Transactional
    public NotificacionDto crear(NuevaNotificacion req) {
        if (req == null) throw ApiException.badRequest("Faltan datos del aviso");
        boolean paraUsuario = req.usuarioId() != null;
        boolean paraRol = req.rol() != null && !req.rol().isBlank();
        if (paraUsuario == paraRol) {
            throw ApiException.badRequest("Indica un usuario o un perfil como destinatario (solo uno)");
        }
        if (paraRol && !ROLES.contains(req.rol())) throw ApiException.badRequest("Perfil no válido");
        String titulo = req.titulo() == null ? "" : req.titulo().trim();
        String mensaje = req.mensaje() == null ? "" : req.mensaje().trim();
        if (titulo.isEmpty() || titulo.length() > 80) throw ApiException.badRequest("El título debe tener entre 1 y 80 caracteres");
        if (mensaje.isEmpty() || mensaje.length() > 250) throw ApiException.badRequest("El mensaje debe tener entre 1 y 250 caracteres");

        Notificacion n = new Notificacion();
        n.setUsuarioId(paraUsuario ? req.usuarioId() : null);
        n.setRol(paraRol ? req.rol() : null);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setTipo(req.tipo() == null || req.tipo().isBlank() ? "OTRO" : req.tipo().trim().toUpperCase());
        n.setReferenciaId(req.referenciaId());
        n.setFecha(System.currentTimeMillis());
        n.setLeida(false);
        return aDto(repo.save(n));
    }

    // ---------- helpers ----------
    private List<Notificacion> visibles(Actor actor) {
        return repo.findTop50ByUsuarioIdOrRolOrderByFechaDescIdDesc(actor.id(), actor.rol());
    }

    private Notificacion propia(long id, Actor actor) {
        Notificacion n = repo.findById(id).orElseThrow(() -> ApiException.notFound("El aviso no existe"));
        boolean esMia = (n.getUsuarioId() != null && n.getUsuarioId() == actor.id())
            || (n.getRol() != null && n.getRol().equals(actor.rol()));
        if (!esMia) throw ApiException.forbidden("Este aviso no es tuyo");
        return n;
    }

    static NotificacionDto aDto(Notificacion n) {
        return new NotificacionDto(n.getId(), n.getTitulo(), n.getMensaje(), n.getTipo(), n.getFecha(),
            n.isLeida(), n.getReferenciaId());
    }
}
