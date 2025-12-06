package utp.cargaweb.cargahub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import utp.cargaweb.cargahub.model.Usuario;
import utp.cargaweb.cargahub.model.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Endpoint para ver quién está logueado actualmente.
     * Usado por el Frontend para mostrar "Hola, Aldo" o el botón de Admin.
     */
    @GetMapping("/api/login-status")
    public Map<String, Object> getStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            response.put("loggedIn", true);
            response.put("username", auth.getName());
            
            // Extraer el rol (ADMIN o ESTUDIANTE)
            String role = auth.getAuthorities().stream().findFirst().get().getAuthority();
            response.put("role", role);
        } else {
            response.put("loggedIn", false);
        }
        return response;
    }

    /**
     * NUEVO: Endpoint para registrar estudiantes.
     * Ruta: POST /api/register
     */
    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody Usuario nuevoUsuario) {
        // 1. Validar si ya existe
        if (usuarioRepository.findByUsername(nuevoUsuario.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El usuario ya existe"));
        }

        // 2. Forzar rol de ESTUDIANTE (Por seguridad, nadie se puede registrar como Admin por aquí)
        nuevoUsuario.setRol("ESTUDIANTE");
        
        // 3. Guardar (La contraseña se guarda tal cual por el NoOpPasswordEncoder)
        usuarioRepository.save(nuevoUsuario);

        return ResponseEntity.ok(Map.of("message", "Usuario registrado correctamente"));
    }
}