package cl.duoc.junta.notificaciones.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aviso para un usuario concreto (usuarioId) o para todos los de un perfil (rol, p. ej. SECRETARIA).
 * En los avisos por perfil el estado "leída" es compartido (MVP).
 */
@Entity
@Table(name = "notificacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(length = 20)
    private String rol;

    @Column(nullable = false, length = 80)
    private String titulo;

    @Column(nullable = false, length = 250)
    private String mensaje;

    /** RESERVA, PAGO, RECORDATORIO u OTRO. */
    @Column(nullable = false, length = 20)
    private String tipo;

    /** Id del recurso relacionado (p. ej. la reserva). */
    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(nullable = false)
    private long fecha;

    @Column(nullable = false)
    private boolean leida;
}
