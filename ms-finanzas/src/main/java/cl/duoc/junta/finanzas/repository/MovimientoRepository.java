package cl.duoc.junta.finanzas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.junta.finanzas.model.EstadoMovimiento;
import cl.duoc.junta.finanzas.model.Movimiento;
import cl.duoc.junta.finanzas.model.TipoMovimiento;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByEstadoOrderByFechaDescIdDesc(EstadoMovimiento estado);

    boolean existsByReservaIdAndTipoAndEstado(Long reservaId, TipoMovimiento tipo, EstadoMovimiento estado);
}
