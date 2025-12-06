package utp.cargaweb.cargahub.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import utp.cargaweb.cargahub.model.Zona;
import java.util.List;

public interface ZonaRepository extends JpaRepository<Zona, Long> {
    List<Zona> findByPabellonAndPiso(String pabellon, int piso);
}