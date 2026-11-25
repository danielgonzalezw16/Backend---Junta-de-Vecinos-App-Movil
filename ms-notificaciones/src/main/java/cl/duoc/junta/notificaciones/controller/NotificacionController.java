package cl.duoc.junta.notificaciones.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import cl.duoc.junta.notificaciones.dto.Dtos.NotificacionDto;
import cl.duoc.junta.notificaciones.security.Actor;
import cl.duoc.junta.notificaciones.service.NotificacionService;

/** Avisos del usuario autenticado (se consumen desde la app móvil a través del gateway). */
@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificacionDto> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listar(Actor.de(jwt));
    }

    // Se declara antes que /{id}/leida para que "leidas" no se interprete como un id.
    @PutMapping("/leidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarTodasLeidas(@AuthenticationPrincipal Jwt jwt) {
        service.marcarTodasLeidas(Actor.de(jwt));
    }

    @PutMapping("/{id}/leida")
    public NotificacionDto marcarLeida(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        return service.marcarLeida(id, Actor.de(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminar(id, Actor.de(jwt));
    }
}
