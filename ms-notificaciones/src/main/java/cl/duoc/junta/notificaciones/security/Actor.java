package cl.duoc.junta.notificaciones.security;

import org.springframework.security.oauth2.jwt.Jwt;

/** Usuario autenticado, extraído de los claims del JWT (uid, nombre, rol). */
public record Actor(long id, String nombre, String rol) {

    public static Actor de(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        String nombre = jwt.getClaimAsString("nombre");
        String rol = jwt.getClaimAsString("rol");
        return new Actor(uid == null ? -1L : uid.longValue(), nombre == null ? "" : nombre, rol == null ? "" : rol);
    }

    public boolean esRol(String r) {
        return r.equals(rol);
    }
}
