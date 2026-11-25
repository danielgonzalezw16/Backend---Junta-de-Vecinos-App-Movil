package cl.duoc.junta.notificaciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.junta.notificaciones.model.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /** Avisos propios o dirigidos al perfil del usuario, los más recientes primero. */
    List<Notificacion> findTop50ByUsuarioIdOrRolOrderByFechaDescIdDesc(Long usuarioId, String rol);
}
