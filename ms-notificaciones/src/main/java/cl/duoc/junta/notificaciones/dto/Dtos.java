package cl.duoc.junta.notificaciones.dto;

public final class Dtos {

    private Dtos() {}

    /** Lo que recibe la app. */
    public record NotificacionDto(Long id, String titulo, String mensaje, String tipo, long fecha,
                                  boolean leida, Long referenciaId) {}

    /** Lo que envían los otros microservicios (reservas, finanzas) para crear un aviso. */
    public record NuevaNotificacion(Long usuarioId, String rol, String titulo, String mensaje,
                                    String tipo, Long referenciaId) {}
}
