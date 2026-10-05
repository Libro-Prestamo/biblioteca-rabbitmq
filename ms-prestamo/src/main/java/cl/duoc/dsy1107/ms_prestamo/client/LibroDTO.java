package cl.duoc.dsy1107.ms_prestamo.client;

public record LibroDTO (
    Long id,
    String titulo,
    String autor,
    String isbn,
    Integer stock,
    Boolean disponible
) {}
