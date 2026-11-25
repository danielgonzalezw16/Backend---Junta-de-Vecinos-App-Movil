package cl.duoc.junta.admin.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.junta.admin.dto.Dtos.*;
import cl.duoc.junta.admin.error.ApiException;
import cl.duoc.junta.admin.model.Rol;
import cl.duoc.junta.admin.model.Usuario;
import cl.duoc.junta.admin.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository repo, JwtService jwt) {
        this.repo = repo;
        this.jwt = jwt;
    }

    public static UsuarioDto aDto(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombre(), u.getRut(), u.getEmail(), u.getRol().name(),
            u.isNotificaciones(), u.getTelefono() == null ? "" : u.getTelefono(),
            u.getDireccion() == null ? "" : u.getDireccion(),
            u.getCargo() == null ? "" : u.getCargo(), u.getFechaCreacion());
    }

    /** Crea una cuenta. (MVP académico: el perfil lo elige quien se registra; en producción lo asigna el administrador.) */
    @Transactional
    public AuthResponse registrar(RegistroRequest r) {
        String nombre = Validaciones.nombre(r.nombre());
        String rut = Validaciones.rut(r.rut());
        String email = Validaciones.email(r.email());
        Validaciones.clave(r.password());
        Rol rol;
        try {
            rol = r.rol() == null ? Rol.VECINO : Rol.valueOf(r.rol());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Perfil no válido");
        }
        if (repo.existsByRut(rut)) throw ApiException.conflict("Este RUT ya está registrado en la aplicación");
        if (repo.existsByEmail(email)) throw ApiException.conflict("Este correo ya está registrado en la aplicación");

        Usuario u = nuevo(nombre, rut, email, r.password(), rol, r.notificaciones() == null || r.notificaciones());
        repo.save(u);
        return new AuthResponse(jwt.generar(u), aDto(u));
    }

    public Usuario nuevo(String nombre, String rut, String email, String clave, Rol rol, boolean notificaciones) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setRut(rut);
        u.setEmail(email);
        u.setRol(rol);
        u.setNotificaciones(notificaciones);
        u.setPasswordHash(encoder.encode(clave));
        u.setActivo(true);
        u.setFechaCreacion(System.currentTimeMillis());
        return u;
    }

    public AuthResponse login(LoginRequest r) {
        String email = r.email() == null ? "" : r.email().trim().toLowerCase();
        Usuario u = repo.findByEmail(email).orElse(null);
        if (u == null || !u.isActivo() || r.password() == null
            || !encoder.matches(r.password(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }
        return new AuthResponse(jwt.generar(u), aDto(u));
    }

    public UsuarioDto obtener(long id) {
        return aDto(buscar(id));
    }

    @Transactional
    public UsuarioDto actualizarPerfil(long id, PerfilRequest r) {
        Usuario u = buscar(id);
        u.setNombre(Validaciones.nombre(r.nombre()));
        u.setTelefono(Validaciones.telefono(r.telefono()));
        u.setDireccion(Validaciones.textoOpcional(r.direccion(), 80, "La dirección"));
        u.setCargo(Validaciones.textoOpcional(r.cargo(), 40, "El cargo"));
        if (r.notificaciones() != null) u.setNotificaciones(r.notificaciones());
        return aDto(repo.save(u));
    }

    @Transactional
    public void cambiarClave(long id, ClaveRequest r) {
        Usuario u = buscar(id);
        if (r.claveActual() == null || !encoder.matches(r.claveActual(), u.getPasswordHash())) {
            throw ApiException.badRequest("La contraseña actual no es correcta");
        }
        Validaciones.clave(r.claveNueva());
        if (r.claveActual().equals(r.claveNueva())) {
            throw ApiException.badRequest("La nueva contraseña debe ser distinta a la actual");
        }
        u.setPasswordHash(encoder.encode(r.claveNueva()));
        repo.save(u);
    }

    private Usuario buscar(long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("No se encontró la cuenta"));
    }
}
