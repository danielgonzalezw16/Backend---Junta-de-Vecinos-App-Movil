package cl.duoc.junta.reservas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import cl.duoc.junta.reservas.dto.Dtos.*;
import cl.duoc.junta.reservas.security.Actor;
import cl.duoc.junta.reservas.service.ReservaService;

@RestController
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    // ----- Reservas -----
    @GetMapping("/reservas")
    public List<ReservaDto> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listar(Actor.de(jwt));
    }

    @GetMapping("/reservas/{id}")
    public ReservaDto obtener(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        return service.obtener(id, Actor.de(jwt));
    }

    @PostMapping("/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaDto crear(@RequestBody ReservaRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.crear(request, Actor.de(jwt));
    }

    @PutMapping("/reservas/{id}")
    public ReservaDto actualizar(@PathVariable long id, @RequestBody ReservaRequest request,
                                 @AuthenticationPrincipal Jwt jwt) {
        return service.actualizar(id, request, Actor.de(jwt));
    }

    @PutMapping("/reservas/{id}/estado")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ReservaDto cambiarEstado(@PathVariable long id, @RequestBody EstadoRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return service.cambiarEstado(id, request, Actor.de(jwt));
    }

    @DeleteMapping("/reservas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminar(id, Actor.de(jwt));
    }

    /** Lo usa ms-finanzas para validar que la reserva existe antes de registrar un pago. */
    @GetMapping("/reservas/{id}/exists")
    public boolean existe(@PathVariable long id) {
        return service.existe(id);
    }

    /** Lo usa ms-finanzas para saber a quién avisar de un pago. */
    @GetMapping("/reservas/{id}/usuario")
    public long propietario(@PathVariable long id) {
        return service.propietario(id);
    }

    @GetMapping("/reservas/{id}/historial")
    @PreAuthorize("hasAnyRole('SECRETARIA','TESORERIA')")
    public List<HistorialDto> historial(@PathVariable long id) {
        return service.historial(id);
    }

    // ----- Dependencias -----
    @GetMapping("/dependencias")
    public List<DependenciaDto> dependencias() {
        return service.listarDependencias();
    }
}
