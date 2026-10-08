package cl.duoc.dsy1107.ms_historial.service;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.dsy1107.ms_historial.model.HistorialEntry;
import cl.duoc.dsy1107.ms_historial.repository.HistorialRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class HistorialService {
    
    private static final Logger log = LoggerFactory.getLogger(HistorialService.class);

    private final HistorialRepository historialRepository;

    public void registrar(Map<String, Object> evento) {
        HistorialEntry entry = new HistorialEntry();
        entry.setTipoEvento((String) evento.get("tipo"));
        entry.setPrestamoId(aLong(evento.get("prestamoId")));
        entry.setLibroId(aLong(evento.get("libroId")));
        entry.setUsuario((String) evento.get("usuario"));

        //prestamo.creado trae fechaPrestamo: prestamo.devuelto trae fechaDevolucionReal
        Object fecha = evento.containsKey("fechaPrestamo")
            ? evento.get("fechaPrestamo")
            : evento.get("fechaDevolucionReal");
        entry.setFecha(fecha != null ? Instant.parse(fecha.toString()) : Instant.now());

        historialRepository.save(entry);
        log.info("Historial registrado: {} | préstamo {} | libro {} | usuario {}",
            entry.getTipoEvento(), entry.getPrestamoId(), entry.getLibroId(), entry.getUsuario());
    }

    private Long aLong(Object valor) {
        return valor == null ? null : ((Number) valor).longValue();
    }

}
