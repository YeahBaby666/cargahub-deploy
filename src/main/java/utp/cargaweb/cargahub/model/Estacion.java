package utp.cargaweb.cargahub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "estaciones")
public class Estacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej: "Mesa 4"
    
    // Jerarquía del Mapa Personalizado
    private String pabellon; // Ej: "Pabellón A" (Actúa como el nombre del mapa)
    private int piso;        // Ej: 1
    
    // Coordenadas relativas al plano/imagen (0 a 100%)
    // Esto permite que el mapa sea cualquier imagen y los puntos se ajusten
    private double coordX; 
    private double coordY;

    // Estado
    private int bateriasDisponibles;
    private int capacidadTotal;
    private boolean activa;
}