package utp.cargaweb.cargahub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "zonas")
public class Zona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej: "Aula 101"
    private String color;  // Ej: "#bfdbfe" (Azul claro)
    
    // Ubicación
    private String pabellon;
    private int piso;

    // Dimensiones en Porcentaje (%) para ser responsivo
    private double x;
    private double y;
    private double width;
    private double height;
}