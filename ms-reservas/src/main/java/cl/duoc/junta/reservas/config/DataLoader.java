package cl.duoc.junta.reservas.config;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.junta.reservas.model.*;
import cl.duoc.junta.reservas.repository.*;

/** Datos de demostración (ficticios): la dependencia principal y una reserva aprobada de la vecina de prueba. */
@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner cargarDemo(DependenciaRepository dependencias, ReservaRepository reservas,
                                 HistorialReservaRepository historial) {
        return args -> {
            if (dependencias.count() == 0) {
                dependencias.save(new Dependencia(null, "Sede social",
                    "Salón principal con cocina y baños", "Los Jazmines 425, Cerrillos", 80, true));
            }
            if (reservas.count() == 0) {
                long ahora = System.currentTimeMillis();
                Reserva r = new Reserva(null, 1L, "Camila Rojas", 1L,
                    LocalDate.now(ZoneId.of("America/Santiago")).plusDays(3), "18:00", "22:00", 25,
                    "Cumpleaños familiar", true, EstadoReserva.APROBADA, ahora, ahora);
                reservas.save(r);
                historial.save(new HistorialReserva(null, r.getId(), null, "PENDIENTE", "Camila Rojas", "Solicitud creada", ahora));
                historial.save(new HistorialReserva(null, r.getId(), "PENDIENTE", "APROBADA", "Pedro Soto", "Datos de demostración", ahora));
            }
        };
    }
}
