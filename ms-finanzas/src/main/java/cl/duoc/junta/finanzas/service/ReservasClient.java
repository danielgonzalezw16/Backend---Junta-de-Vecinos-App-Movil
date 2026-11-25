package cl.duoc.junta.finanzas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import cl.duoc.junta.finanzas.error.ApiException;

/** Comunicación entre microservicios: consulta a ms-reservas si una reserva existe (reenvía el JWT). */
@Component
public class ReservasClient {

    private final RestClient http = RestClient.create();
    private final String baseUrl;

    public ReservasClient(@Value("${api.reservas.url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean existe(long reservaId, String authorization) {
        try {
            Boolean r = http.get()
                .uri(baseUrl + "/reservas/{id}/exists", reservaId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(Boolean.class);
            return Boolean.TRUE.equals(r);
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo validar la reserva con ms-reservas");
        }
    }

    /** Id del usuario dueño de la reserva, o null si no se pudo consultar (el aviso es opcional). */
    public Long propietario(long reservaId, String authorization) {
        try {
            return http.get()
                .uri(baseUrl + "/reservas/{id}/usuario", reservaId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(Long.class);
        } catch (RestClientException e) {
            return null;
        }
    }
}
