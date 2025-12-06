package utp.cargaweb.cargahub.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "alquileres")
public class Alquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Dónde la sacó
    @ManyToOne
    @JoinColumn(name = "estacion_origen_id", nullable = false)
    private Estacion estacionOrigen;

    // Dónde la devolvió (puede ser null si aún la tiene)
    @ManyToOne
    @JoinColumn(name = "estacion_destino_id")
    private Estacion estacionDestino;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    // Estado activo o finalizado
    private boolean activo;

    public Alquiler(Usuario usuario, Estacion estacionOrigen) {
        this.usuario = usuario;
        this.estacionOrigen = estacionOrigen;
        this.fechaInicio = LocalDateTime.now();
        this.activo = true;
    }
}