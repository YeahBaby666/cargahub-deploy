package utp.cargaweb.cargahub.model.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utp.cargaweb.cargahub.model.Alquiler;
import utp.cargaweb.cargahub.model.Estacion;
import utp.cargaweb.cargahub.model.Usuario;
import utp.cargaweb.cargahub.model.Zona;
import utp.cargaweb.cargahub.model.repository.AlquilerRepository;
import utp.cargaweb.cargahub.model.repository.EstacionRepository;
import utp.cargaweb.cargahub.model.repository.UsuarioRepository;
import utp.cargaweb.cargahub.model.repository.ZonaRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EstacionService {

    private final EstacionRepository estacionRepository;
    private final ZonaRepository zonaRepository;
    private final AlquilerRepository alquilerRepository;
    private final UsuarioRepository usuarioRepository;

    private static final double PRECIO_ALQUILER = 3.50;

    public EstacionService(EstacionRepository estacionRepository, ZonaRepository zonaRepository, 
                           AlquilerRepository alquilerRepository, UsuarioRepository usuarioRepository) {
        this.estacionRepository = estacionRepository;
        this.zonaRepository = zonaRepository;
        this.alquilerRepository = alquilerRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // --- LOGICA DE NEGOCIO ---
    @Transactional
    public void alquilar(String username, Long estacionId) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (alquilerRepository.findByUsuarioAndActivoTrue(usuario).isPresent()) {
            throw new RuntimeException("Ya tienes un alquiler activo. Devuelve la batería primero.");
        }

        Estacion estacion = estacionRepository.findById(estacionId)
                .orElseThrow(() -> new RuntimeException("Estación no encontrada"));

        if (estacion.getBateriasDisponibles() <= 0) {
            throw new RuntimeException("Esta estación no tiene baterías disponibles.");
        }

        estacion.setBateriasDisponibles(estacion.getBateriasDisponibles() - 1);
        estacionRepository.save(estacion);

        Alquiler nuevoAlquiler = new Alquiler(usuario, estacion);
        alquilerRepository.save(nuevoAlquiler);
    }

    @Transactional
    public void devolver(String username, Long estacionId) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Alquiler alquiler = alquilerRepository.findByUsuarioAndActivoTrue(usuario)
                .orElseThrow(() -> new RuntimeException("No tienes ninguna batería para devolver."));

        Estacion estacionDestino = estacionRepository.findById(estacionId)
                .orElseThrow(() -> new RuntimeException("Estación no encontrada"));

        if (estacionDestino.getBateriasDisponibles() >= estacionDestino.getCapacidadTotal()) {
            throw new RuntimeException("Esta estación está llena, busca otra.");
        }

        estacionDestino.setBateriasDisponibles(estacionDestino.getBateriasDisponibles() + 1);
        estacionRepository.save(estacionDestino);

        alquiler.setActivo(false);
        alquiler.setFechaFin(LocalDateTime.now());
        alquiler.setEstacionDestino(estacionDestino);
        alquilerRepository.save(alquiler);
    }

    public boolean usuarioTieneAlquilerActivo(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if(usuario == null) return false;
        return alquilerRepository.findByUsuarioAndActivoTrue(usuario).isPresent();
    }

    // --- GESTIÓN BÁSICA ---
    public List<Estacion> listarTodas() { return estacionRepository.findAll(); }
    public List<Zona> listarZonas() { return zonaRepository.findAll(); }

    @Transactional
    public Estacion crearEstacion(Estacion nueva) {
        if (nueva.getPabellon() == null || nueva.getPabellon().isEmpty()) nueva.setPabellon("General");
        if (nueva.getPiso() == 0) nueva.setPiso(1);
        nueva.setActiva(true);
        return estacionRepository.save(nueva);
    }

    @Transactional
    public Zona crearZona(Zona zona) {
        if (zona.getPabellon() == null || zona.getPabellon().isEmpty()) zona.setPabellon("General");
        if (zona.getPiso() == 0) zona.setPiso(1);
        return zonaRepository.save(zona);
    }

    // --- REPORTES ---
    public Map<String, Object> obtenerReporteGeneral() {
        List<Estacion> todas = estacionRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        
        int totalEstaciones = todas.size();
        int totalBaterias = todas.stream().mapToInt(Estacion::getBateriasDisponibles).sum();
        long criticas = todas.stream().filter(e -> e.getBateriasDisponibles() == 0).count();
        long totalTransacciones = alquilerRepository.count();
        double gananciasTotales = totalTransacciones * PRECIO_ALQUILER;

        stats.put("totalEstaciones", totalEstaciones);
        stats.put("totalBateriasSistema", totalBaterias);
        stats.put("estacionesCriticas", criticas);
        stats.put("transacciones", totalTransacciones);
        stats.put("ganancias", String.format("S/ %.2f", gananciasTotales));
        return stats;
    }

    public List<Map<String, Object>> obtenerHistorialReciente() {
        return alquilerRepository.findAll().stream()
                .sorted((a, b) -> b.getFechaInicio().compareTo(a.getFechaInicio()))
                .limit(20)
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("usuario", a.getUsuario().getUsername());
                    map.put("origen", a.getEstacionOrigen().getNombre());
                    map.put("destino", a.getEstacionDestino() != null ? a.getEstacionDestino().getNombre() : "-");
                    map.put("fecha", a.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
                    map.put("estado", a.isActivo() ? "En curso" : "Finalizado");
                    map.put("monto", "S/ " + PRECIO_ALQUILER);
                    return map;
                })
                .collect(Collectors.toList());
    }

    // --- NUEVO: GENERAR EXCEL ---
    public byte[] generarExcelTransacciones() throws IOException {
        List<Alquiler> alquileres = alquilerRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transacciones");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Usuario", "Estación Salida", "Estación Retorno", "Fecha Inicio", "Fecha Fin", "Estado", "Monto"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data
            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (Alquiler a : alquileres) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getId());
                row.createCell(1).setCellValue(a.getUsuario().getUsername());
                row.createCell(2).setCellValue(a.getEstacionOrigen().getNombre());
                row.createCell(3).setCellValue(a.getEstacionDestino() != null ? a.getEstacionDestino().getNombre() : "-");
                row.createCell(4).setCellValue(a.getFechaInicio().format(formatter));
                row.createCell(5).setCellValue(a.getFechaFin() != null ? a.getFechaFin().format(formatter) : "-");
                row.createCell(6).setCellValue(a.isActivo() ? "ACTIVO" : "FINALIZADO");
                row.createCell(7).setCellValue(PRECIO_ALQUILER);
            }
            
            // Auto-size columns
            for(int i=0; i<columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}