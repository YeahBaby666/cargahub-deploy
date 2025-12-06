package utp.cargaweb.cargahub.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import utp.cargaweb.cargahub.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}