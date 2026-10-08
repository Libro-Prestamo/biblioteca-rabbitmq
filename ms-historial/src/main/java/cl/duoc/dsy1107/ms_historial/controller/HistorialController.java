package cl.duoc.dsy1107.ms_historial.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.dsy1107.ms_historial.model.HistorialEntry;
import cl.duoc.dsy1107.ms_historial.repository.HistorialRepository;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/historial")
@RequiredArgsConstructor 
public class HistorialController {
    
    private final HistorialRepository historialRepository;

    @GetMapping
    public List<HistorialEntry> listar() {
        return historialRepository.findAll();
    }
}
