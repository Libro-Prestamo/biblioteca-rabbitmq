package cl.duoc.dsy1107.ms_historial.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Getter
@Setter 
@NoArgsConstructor 
public class HistorialEntry {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipoEvento;
    private Long prestamoId;
    private Long libroId;
    private String usuario;
    private Instant fecha;

}
