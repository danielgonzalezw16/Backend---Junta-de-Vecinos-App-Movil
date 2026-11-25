package cl.duoc.junta.reservas.dto;

public final class Dtos {

    private Dtos() {}

    public record ReservaRequest(String fecha, String horaInicio, String horaFin, Integer personas,
                                 String motivo, Boolean requiereSonido, Long dependenciaId) {}

    public record EstadoRequest(String estado, String observacion) {}

    public record ReservaDto(Long id, Long usuarioId, String solicitante, Long dependenciaId, String fecha,
                             String horaInicio, String horaFin, int personas, String motivo,
                             boolean requiereSonido, String estado, long fechaCreacion) {}

    public record DependenciaDto(Long id, String nombre, String descripcion, String ubicacion,
                                 int capacidadReferencial) {}

    public record HistorialDto(Long id, String estadoAnterior, String estadoNuevo,
                               String usuarioResponsable, String observacion, long fechaCambio) {}
}
