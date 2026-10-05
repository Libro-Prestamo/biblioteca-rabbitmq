package cl.duoc.dsy1107.ms_prestamo.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.dsy1107.ms_prestamo.client.LibroClient;
import cl.duoc.dsy1107.ms_prestamo.client.LibroDTO;
import cl.duoc.dsy1107.ms_prestamo.event.PrestamoCreadoEvent;
import cl.duoc.dsy1107.ms_prestamo.event.PrestamoDevueltoEvent;
import cl.duoc.dsy1107.ms_prestamo.event.PrestamoEventPublisher;
import cl.duoc.dsy1107.ms_prestamo.model.EstadoPrestamo;
import cl.duoc.dsy1107.ms_prestamo.model.Prestamo;
import cl.duoc.dsy1107.ms_prestamo.repository.PrestamoRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class PrestamoService {
    
    private final PrestamoRepository prestamoRepository;
    private final LibroClient libroClient;
    private final PrestamoEventPublisher publisher;

    public Prestamo crearPrestamo(Long libroId, String usuario) {
        //1. SINCRONO: busdca libro disponible en ms-catalogo
        LibroDTO libro;
        try {
            libro = libroClient.consultarLibro(libroId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El libro no existe");
        }

        if (libro == null || libro.stock() == null || libro.stock() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Libro sin stock");
        }

        //2. Guardar prestamo
        Instant ahora = Instant.now();
        Prestamo prestamo = new Prestamo();
        prestamo.setLibroId(libroId);
        prestamo.setUsuario(usuario);
        prestamo.setFechaPrestamo(ahora);
        prestamo.setFechaDevolucionEstimada(ahora.plus(14, ChronoUnit.DAYS));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        Prestamo guardado = prestamoRepository.save(prestamo);

        //3. ASINCRONO: publicar por RabbitMQ
        publisher.publicarCreado(new PrestamoCreadoEvent(
            UUID.randomUUID().toString(),
            "prestamo.creado",
            guardado.getId(),
            guardado.getLibroId(),
            guardado.getUsuario(),
            guardado.getFechaPrestamo(),
            guardado.getFechaDevolucionEstimada(),
            guardado.getEstado().name()
        ));

        return guardado;
    }

    public Prestamo devolverPrestamo(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Préstamo no encontrado"));

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El préstamo ya fue devuelto");
        }

        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucionReal(Instant.now());
        Prestamo guardado = prestamoRepository.save(prestamo);

        publisher.publicarDevuelto(new PrestamoDevueltoEvent(
            UUID.randomUUID().toString(),
            "prestamo.devuelto",
            guardado.getId(),
            guardado.getLibroId(),
            guardado.getUsuario(),
            guardado.getFechaDevolucionReal(),
            guardado.getEstado().name()
        ));

        return guardado;
    }

}
