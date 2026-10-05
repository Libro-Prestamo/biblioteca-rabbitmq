package cl.duoc.dsy1107.ms_catalogo.config;

import cl.duoc.dsy1107.ms_catalogo.model.Libro;
import cl.duoc.dsy1107.ms_catalogo.repository.LibroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final LibroRepository libroRepository;

    @Override
    public void run(String... args) {
        if (libroRepository.count() == 0) {
            libroRepository.save(new Libro(null, "Cien años de soledad", "Gabriel García Márquez", "978-0307474728", 3, true));
            libroRepository.save(new Libro(null, "El Principito", "Antoine de Saint-Exupéry", "978-0156012195", 5, true));
            libroRepository.save(new Libro(null, "Don Quijote de la Mancha", "Miguel de Cervantes", "978-0060934347", 0, false));
        }
    }
}