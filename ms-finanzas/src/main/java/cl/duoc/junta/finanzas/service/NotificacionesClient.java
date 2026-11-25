package cl.duoc.junta.finanzas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Comunicación entre microservicios: crea avisos en ms-notificaciones (clave interna, sin JWT).
 * Es "mejor esfuerzo": si ms-notificaciones no responde, la operación principal (reserva, pago) igual se completa.
 */
@Component
public class NotificacionesClient {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesClient.class);

    public record NuevaNotificacion(Long usuarioId, String rol, String titulo, String mensaje,
                                    String tipo, Long referenciaId) {}

    private final RestClient http = RestClient.create();
    private final String baseUrl;
    private final String claveInterna;

    public NotificacionesClient(@Value("${api.notificaciones.url}") String baseUrl,
                                @Value("${internal.key}") String claveInterna) {
        this.baseUrl = baseUrl;
        this.claveInterna = claveInterna;
    }

    public void avisarAUsuario(Long usuarioId, String titulo, String mensaje, String tipo, Long referenciaId) {
        enviar(new NuevaNotificacion(usuarioId, null, titulo, mensaje, tipo, referenciaId));
    }

    public void avisarARol(String rol, String titulo, String mensaje, String tipo, Long referenciaId) {
        enviar(new NuevaNotificacion(null, rol, titulo, mensaje, tipo, referenciaId));
    }

    private void enviar(NuevaNotificacion n) {
        try {
            http.post()
                .uri(baseUrl + "/internal/notificaciones")
                .header("X-Internal-Key", claveInterna)
                .contentType(MediaType.APPLICATION_JSON)
                .body(n)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo crear el aviso en ms-notificaciones: {}", e.getMessage());
        }
    }
}
