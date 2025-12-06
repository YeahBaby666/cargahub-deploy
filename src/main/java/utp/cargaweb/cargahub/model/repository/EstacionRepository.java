package utp.cargaweb.cargahub.model.repository;

import utp.cargaweb.cargahub.model.Estacion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Esta interfaz es la magia de Spring Data.
 * Sin escribir código, nos da métodos para "pintar" en la base de datos:
 * save(), findById(), findAll(), delete()...
 */
public interface EstacionRepository extends JpaRepository<Estacion, Long> {
    // Podemos añadir consultas personalizadas aquí si las necesitamos
}

