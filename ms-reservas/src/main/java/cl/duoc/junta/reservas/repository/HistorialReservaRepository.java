package cl.duoc.junta.reservas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.junta.reservas.model.HistorialReserva;

@Repository
public interface HistorialReservaRepository extends JpaRepository<HistorialReserva, Long> {
    List<HistorialReserva> findByReservaIdOrderByFechaCambioAsc(Long reservaId);
    void deleteByReservaId(Long reservaId);
}
