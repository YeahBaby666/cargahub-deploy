package utp.cargaweb.cargahub.model.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import utp.cargaweb.cargahub.model.Usuario;
import utp.cargaweb.cargahub.model.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UsuarioRepository repository) {
        return args -> {
            // Crear ADMIN si no existe
            if (repository.findByUsername("admin").isEmpty()) {
                repository.save(new Usuario("admin", "admin123", "ADMIN"));
                System.out.println(">>> Usuario ADMIN creado: admin / admin123");
            }

            // Crear ESTUDIANTE si no existe
            if (repository.findByUsername("aldo").isEmpty()) {
                repository.save(new Usuario("aldo", "1234", "ESTUDIANTE"));
                System.out.println(">>> Usuario ESTUDIANTE creado: aldo / 1234");
            }
        };
    }
}