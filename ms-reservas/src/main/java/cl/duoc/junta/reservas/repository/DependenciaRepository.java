package cl.duoc.junta.reservas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.junta.reservas.model.Dependencia;

@Repository
public interface DependenciaRepository extends JpaRepository<Dependencia, Long> {
    List<Dependencia> findByActivaTrue();
}
