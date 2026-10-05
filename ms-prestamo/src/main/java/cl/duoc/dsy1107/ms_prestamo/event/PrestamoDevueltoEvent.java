package cl.duoc.dsy1107.ms_prestamo.event;

import java.time.Instant;

public record PrestamoDevueltoEvent(
    String eventId,
    String tipo,
    Long prestamoId,
    Long libroId,
    String usuario,
    Instant fechaDevolucionReal,
    String estado
) {}
