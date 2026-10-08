package cl.duoc.dsy1107.ms_historial.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.dsy1107.ms_historial.model.HistorialEntry;

public interface HistorialRepository extends JpaRepository<HistorialEntry, Long> {

}
