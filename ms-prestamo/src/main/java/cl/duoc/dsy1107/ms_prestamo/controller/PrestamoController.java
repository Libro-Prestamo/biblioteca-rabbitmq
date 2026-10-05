package cl.duoc.dsy1107.ms_prestamo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.dsy1107.ms_prestamo.model.Prestamo;
import cl.duoc.dsy1107.ms_prestamo.service.PrestamoService;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/prestamos")
@RequiredArgsConstructor 
public class PrestamoController {
    
    private final PrestamoService prestamoService;

    public record CrearPrestamoRequest(Long libroId, String usuario) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prestamo crear(@RequestBody CrearPrestamoRequest request) {
        return prestamoService.crearPrestamo(request.libroId(), request.usuario());
    }

    @PostMapping("/{id}/devolucion")
    public Prestamo devolver(@PathVariable Long id) {
        return prestamoService.devolverPrestamo(id);        
    }
}
