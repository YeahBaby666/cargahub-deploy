package utp.cargaweb.cargahub.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import utp.cargaweb.cargahub.model.Alquiler;
import utp.cargaweb.cargahub.model.Usuario;
import java.util.Optional;

public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {
    // Buscar si este usuario tiene algún alquiler activo (sin fecha de fin)
    Optional<Alquiler> findByUsuarioAndActivoTrue(Usuario usuario);
}