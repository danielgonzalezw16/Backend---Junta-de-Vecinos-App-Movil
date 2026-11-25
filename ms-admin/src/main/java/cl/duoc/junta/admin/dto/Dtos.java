package cl.duoc.junta.admin.dto;

/** Contratos JSON del microservicio (la app móvil usa los mismos nombres de campo). */
public final class Dtos {

    private Dtos() {}

    public record RegistroRequest(String nombre, String rut, String email, String password,
                                  String rol, Boolean notificaciones) {}

    public record LoginRequest(String email, String password) {}

    public record PerfilRequest(String nombre, String telefono, String direccion,
                                String cargo, Boolean notificaciones) {}

    public record ClaveRequest(String claveActual, String claveNueva) {}

    public record UsuarioDto(Long id, String nombre, String rut, String email, String rol,
                             boolean notificaciones, String telefono, String direccion,
                             String cargo, long fechaRegistro) {}

    public record AuthResponse(String token, UsuarioDto usuario) {}
}
