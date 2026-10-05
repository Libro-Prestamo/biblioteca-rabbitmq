package cl.duoc.dsy1107.ms_prestamo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.dsy1107.ms_prestamo.model.Prestamo;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long>{
    
}
