package cl.duoc.junta.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import cl.duoc.junta.admin.dto.Dtos.*;
import cl.duoc.junta.admin.security.Actor;
import cl.duoc.junta.admin.service.UsuarioService;

/** Perfil del usuario autenticado (cualquier rol). */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public UsuarioDto me(@AuthenticationPrincipal Jwt jwt) {
        return service.obtener(Actor.de(jwt).id());
    }

    @PutMapping("/me")
    public UsuarioDto actualizar(@AuthenticationPrincipal Jwt jwt, @RequestBody PerfilRequest request) {
        return service.actualizarPerfil(Actor.de(jwt).id(), request);
    }

    @PutMapping("/me/clave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarClave(@AuthenticationPrincipal Jwt jwt, @RequestBody ClaveRequest request) {
        service.cambiarClave(Actor.de(jwt).id(), request);
    }
}
