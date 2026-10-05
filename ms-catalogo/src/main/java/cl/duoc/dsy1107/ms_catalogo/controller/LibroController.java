package cl.duoc.dsy1107.ms_catalogo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.dsy1107.ms_catalogo.model.Libro;
import cl.duoc.dsy1107.ms_catalogo.repository.LibroRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public/libros")
@RequiredArgsConstructor 
public class LibroController {
    
    private final LibroRepository libroRepository;

    @GetMapping
    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    @GetMapping("/{id}")
    public Libro obtener(@PathVariable Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));
    }

}
