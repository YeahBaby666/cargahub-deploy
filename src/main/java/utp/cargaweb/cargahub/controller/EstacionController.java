package utp.cargaweb.cargahub.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utp.cargaweb.cargahub.model.Estacion;
import utp.cargaweb.cargahub.model.Zona;
import utp.cargaweb.cargahub.model.service.EstacionService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estaciones")
public class EstacionController {

    private final EstacionService service;

    public EstacionController(EstacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Estacion> obtenerTodas() { return service.listarTodas(); }

    @GetMapping("/zonas")
    public List<Zona> obtenerZonas() { return service.listarZonas(); }

    @GetMapping("/mi-estado")
    public ResponseEntity<?> miEstado(Principal principal) {
        boolean tieneBateria = service.usuarioTieneAlquilerActivo(principal.getName());
        return ResponseEntity.ok(Map.of("tieneBateria", tieneBateria));
    }

    @PostMapping("/{id}/accion")
    public ResponseEntity<?> realizarAccion(@PathVariable Long id, @RequestParam String tipo, Principal principal) {
        try {
            if ("alquilar".equals(tipo)) {
                service.alquilar(principal.getName(), id);
                return ResponseEntity.ok(Map.of("mensaje", "¡Alquiler exitoso! Tienes 1 hora."));
            } else if ("devolver".equals(tipo)) {
                service.devolver(principal.getName(), id);
                return ResponseEntity.ok(Map.of("mensaje", "¡Devolución exitosa! Gracias."));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Acción no válida"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- ADMIN ---
    
    @PostMapping("/admin/crear")
    public ResponseEntity<?> crear(@RequestBody Estacion estacion) {
        try {
            return ResponseEntity.ok(service.crearEstacion(estacion));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/admin/zona/crear")
    public ResponseEntity<?> crearZona(@RequestBody Zona zona) {
        return ResponseEntity.ok(service.crearZona(zona));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> verEstadisticas() {
        return ResponseEntity.ok(service.obtenerReporteGeneral());
    }

    @GetMapping("/admin/historial")
    public ResponseEntity<List<Map<String, Object>>> verHistorial() {
        return ResponseEntity.ok(service.obtenerHistorialReciente());
    }

    // NUEVO: ENDPOINT PARA DESCARGAR EXCEL
    @GetMapping("/admin/exportar-excel")
    public ResponseEntity<byte[]> descargarExcel() {
        try {
            byte[] archivo = service.generarExcelTransacciones();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_cargahub.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(archivo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}