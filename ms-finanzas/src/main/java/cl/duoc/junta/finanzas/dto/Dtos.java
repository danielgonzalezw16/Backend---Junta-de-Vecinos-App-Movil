package cl.duoc.junta.finanzas.dto;

public final class Dtos {

    private Dtos() {}

    public record MovimientoRequest(String tipo, Integer monto, String concepto, String categoria,
                                    String fecha, Long reservaId, String comprobanteRef) {}

    public record MovimientoDto(Long id, String tipo, int monto, String concepto, String categoria,
                                String fecha, Long reservaId, String comprobanteRef, long registradoPor) {}
}
