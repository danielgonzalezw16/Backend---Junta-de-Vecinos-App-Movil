package cl.duoc.junta.admin.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.junta.admin.model.Rol;
import cl.duoc.junta.admin.repository.UsuarioRepository;
import cl.duoc.junta.admin.service.UsuarioService;

/** Cuentas de demostración (datos ficticios). Contraseña de las tres: Demo1234. Solo se crean si la tabla está vacía. */
@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner cargarCuentasDemo(UsuarioRepository repo, UsuarioService service) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(service.nuevo("Camila Rojas", "111111111", "vecino@junta.cl", "Demo1234", Rol.VECINO, true));
            repo.save(service.nuevo("Pedro Soto", "222222222", "secretaria@junta.cl", "Demo1234", Rol.SECRETARIA, true));
            repo.save(service.nuevo("Ana Muñoz", "123456785", "tesoreria@junta.cl", "Demo1234", Rol.TESORERIA, true));
        };
    }
}
