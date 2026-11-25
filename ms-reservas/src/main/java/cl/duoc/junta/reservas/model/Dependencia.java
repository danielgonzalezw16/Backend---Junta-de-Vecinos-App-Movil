package cl.duoc.junta.reservas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dependencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dependencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Column(length = 100)
    private String ubicacion;

    @Column(name = "capacidad_referencial")
    private int capacidadReferencial;

    @Column(nullable = false)
    private boolean activa = true;
}
