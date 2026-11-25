package cl.duoc.junta.finanzas.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import cl.duoc.junta.finanzas.dto.Dtos.*;
import cl.duoc.junta.finanzas.security.Actor;
import cl.duoc.junta.finanzas.service.MovimientoService;

@RestController
@RequestMapping("/movimientos")
public class MovimientoController {

    private final MovimientoService service;

    public MovimientoController(MovimientoService service) {
        this.service = service;
    }

    @GetMapping
    public List<MovimientoDto> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listar(Actor.de(jwt));
    }

    @GetMapping("/{id}")
    public MovimientoDto obtener(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        return service.obtener(id, Actor.de(jwt));
    }

    @PostMapping
    @PreAuthorize("hasRole('TESORERIA')")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoDto crear(@RequestBody MovimientoRequest request, @AuthenticationPrincipal Jwt jwt,
                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return service.crear(request, Actor.de(jwt), authorization);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TESORERIA')")
    public MovimientoDto actualizar(@PathVariable long id, @RequestBody MovimientoRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return service.actualizar(id, request, Actor.de(jwt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TESORERIA')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void anular(@PathVariable long id) {
        service.anular(id);
    }
}
