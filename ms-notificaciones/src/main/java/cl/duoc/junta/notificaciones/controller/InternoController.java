package cl.duoc.junta.notificaciones.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import cl.duoc.junta.notificaciones.dto.Dtos.NuevaNotificacion;
import cl.duoc.junta.notificaciones.error.ApiException;
import cl.duoc.junta.notificaciones.service.NotificacionService;

/** Comunicación entre microservicios: ms-reservas y ms-finanzas crean avisos aquí. No se enruta por el gateway. */
@RestController
@RequestMapping("/internal/notificaciones")
public class InternoController {

    private final NotificacionService service;
    private final String claveInterna;

    public InternoController(NotificacionService service, @Value("${internal.key}") String claveInterna) {
        this.service = service;
        this.claveInterna = claveInterna;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void crear(@RequestHeader(value = "X-Internal-Key", required = false) String clave,
                      @RequestBody NuevaNotificacion request) {
        if (clave == null || !clave.equals(claveInterna)) {
            throw ApiException.unauthorized("Clave interna inválida");
        }
        service.crear(request);
    }
}
