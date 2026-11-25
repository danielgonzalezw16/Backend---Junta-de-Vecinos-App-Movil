package cl.duoc.junta.reservas.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.duoc.junta.reservas.model.EstadoReserva;
import cl.duoc.junta.reservas.model.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findAllByOrderByFechaDescHoraInicioAsc();

    /** Regla de negocio: cuenta reservas activas que se superponen con el horario pedido (excluyendo una reserva). */
    @Query("select count(r) from Reserva r where r.dependenciaId = :dependenciaId and r.fecha = :fecha "
        + "and r.estado in :estados and r.horaInicio < :horaFin and r.horaFin > :horaInicio "
        + "and r.id <> :excluirId")
    long contarConflictos(@Param("dependenciaId") Long dependenciaId,
                          @Param("fecha") LocalDate fecha,
                          @Param("horaInicio") String horaInicio,
                          @Param("horaFin") String horaFin,
                          @Param("estados") Collection<EstadoReserva> estados,
                          @Param("excluirId") Long excluirId);
}
