package cl.duoc.junta.finanzas.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimiento_financiero")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private int monto;

    @Column(nullable = false, length = 60)
    private String concepto;

    @Column(nullable = false, length = 30)
    private String categoria;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDate fecha;

    @Column(name = "reserva_id")
    private Long reservaId;

    /** Nombre del archivo del comprobante (el archivo vive en el teléfono; ver README). */
    @Column(name = "comprobante_ref", length = 120)
    private String comprobanteRef;

    @Column(name = "usuario_registro_id", nullable = false)
    private Long registradoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoMovimiento estado = EstadoMovimiento.VIGENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private long fechaCreacion;
}
