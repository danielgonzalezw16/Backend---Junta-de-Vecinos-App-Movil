package cl.duoc.junta.admin.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cl.duoc.junta.admin.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Emite el JWT (HS256). Los demás microservicios lo validan con la misma clave. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiracion-horas:12}") long horas) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = horas * 60L * 60L * 1000L;
    }

    public String generar(Usuario u) {
        Date ahora = new Date();
        return Jwts.builder()
            .subject(u.getEmail())
            .claim("uid", u.getId())
            .claim("nombre", u.getNombre())
            .claim("rol", u.getRol().name())
            .issuedAt(ahora)
            .expiration(new Date(ahora.getTime() + expiracionMs))
            .signWith(key, Jwts.SIG.HS256)
            .compact();
    }
}
