package cl.duoc.junta.reservas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Auditoría de cada cambio de estado de una reserva. */
@Entity
@Table(name = "historial_reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    @Column(name = "estado_anterior", length = 15)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 15)
    private String estadoNuevo;

    @Column(name = "usuario_responsable", length = 60)
    private String usuarioResponsable;

    @Column(length = 200)
    private String observacion;

    @Column(name = "fecha_cambio", nullable = false)
    private long fechaCambio;
}
