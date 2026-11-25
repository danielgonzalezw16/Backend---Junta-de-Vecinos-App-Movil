package cl.duoc.junta.finanzas.config;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.junta.finanzas.model.*;
import cl.duoc.junta.finanzas.repository.MovimientoRepository;

/** Movimientos de demostración (montos ficticios), registrados por la tesorera de prueba (id 3). */
@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner cargarMovimientosDemo(MovimientoRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            LocalDate hoy = LocalDate.now(ZoneId.of("America/Santiago"));
            long ahora = System.currentTimeMillis();
            demo(repo, TipoMovimiento.INGRESO, 35000, "Arriendo sede", "Arriendo", hoy.minusDays(10), ahora);
            demo(repo, TipoMovimiento.INGRESO, 35000, "Arriendo sede", "Arriendo", hoy.minusDays(4), ahora);
            demo(repo, TipoMovimiento.GASTO, 28500, "Cuenta de luz", "Luz", hoy.minusDays(8), ahora);
            demo(repo, TipoMovimiento.GASTO, 15200, "Cuenta de agua", "Agua", hoy.minusDays(7), ahora);
            demo(repo, TipoMovimiento.GASTO, 12000, "Cilindros de gas", "Gas", hoy.minusDays(2), ahora);
        };
    }

    private void demo(MovimientoRepository repo, TipoMovimiento tipo, int monto, String concepto,
                      String categoria, LocalDate fecha, long ahora) {
        repo.save(new Movimiento(null, tipo, monto, concepto, categoria, fecha, null, null, 3L,
            EstadoMovimiento.VIGENTE, ahora));
    }
}
