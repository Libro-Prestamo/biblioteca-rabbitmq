package cl.duoc.dsy1107.ms_prestamo.event;

import java.time.Instant;

public record PrestamoCreadoEvent(
    String eventId,
    String tipo,
    Long prestamoId,
    Long libroId,
    String usuario,
    Instant fechaPrestamo,
    Instant fechaDevolucionEstimada,
    String estado
) {}
