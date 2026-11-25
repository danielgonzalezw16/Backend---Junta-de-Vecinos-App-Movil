package cl.duoc.junta.reservas.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /** Nombre del solicitante al momento de crear la reserva (viene del JWT). */
    @Column(nullable = false, length = 60)
    private String solicitante;

    @Column(name = "dependencia_id", nullable = false)
    private Long dependenciaId;

    @Column(nullable = false)
    private LocalDate fecha;

    /** Formato HH:mm (comparable como texto). */
    @Column(name = "hora_inicio", nullable = false, length = 5)
    private String horaInicio;

    @Column(name = "hora_fin", nullable = false, length = 5)
    private String horaFin;

    @Column(nullable = false)
    private int personas;

    @Column(nullable = false, length = 100)
    private String motivo;

    @Column(name = "requiere_sonido", nullable = false)
    private boolean requiereSonido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoReserva estado;

    @Column(name = "fecha_creacion", nullable = false)
    private long fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private long fechaActualizacion;
}
