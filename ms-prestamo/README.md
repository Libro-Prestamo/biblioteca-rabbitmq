# ms-prestamo

Microservicio de préstamos del sistema Biblioteca. Es el **productor** de eventos: gestiona la creación y devolución de préstamos, consulta el stock de forma **síncrona** a `ms-catalogo` y publica eventos de forma **asíncrona** a RabbitMQ.

Parte de la actividad evaluada de RabbitMQ (DSY1107 - Desarrollo Cloud Native I).

## Stack

- Java 21, Spring Boot
- Spring Web, Spring Data JPA (H2 en memoria)
- Spring for RabbitMQ (AMQP)
- Lombok

## Rol en la arquitectura

```
Cliente --POST /prestamos--> ms-prestamo --GET /public/libros/{id}--> ms-catalogo   (síncrono)
                                  |
                                  +--publish--> exchange biblioteca.events (topic)
                                                   |-- prestamo.creado --> notificaciones.queue --> ms-notificaciones
                                                   |-- prestamo.*       --> historial.queue     --> ms-historial
```

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/prestamos` | Crea un préstamo. Body: `{"libroId": 1, "usuario": "correo"}` | 201, 404 (libro no existe), 409 (sin stock) |
| POST | `/prestamos/{id}/devolucion` | Registra la devolución | 200, 404, 409 (ya devuelto) |

## Flujo de `POST /prestamos`

1. **Síncrono:** consulta a `ms-catalogo` si el libro existe y tiene stock (`LibroClient`).
2. Guarda el préstamo con estado `ACTIVO`.
3. **Asíncrono:** publica el evento `prestamo.creado` en RabbitMQ y responde al cliente sin esperar a los consumidores.

## Configuración de RabbitMQ

| Elemento | Valor |
|---|---|
| Exchange | `biblioteca.events` (topic, durable) |
| Cola de notificaciones | `notificaciones.queue`, binding `prestamo.creado` |
| Cola de historial | `historial.queue`, binding `prestamo.*` |
| Routing keys publicadas | `prestamo.creado`, `prestamo.devuelto` |

La configuración está en `config/RabbitConfig.java`. Los mensajes viajan como JSON.

### Ejemplo de payload `prestamo.creado`

```json
{
  "eventId": "a3f1c2e0-7b4d-4a1e-9c3f-1234567890ab",
  "tipo": "prestamo.creado",
  "prestamoId": 101,
  "libroId": 5,
  "usuario": "usuario1@test.com",
  "fechaPrestamo": "2026-10-05T15:30:00Z",
  "fechaDevolucionEstimada": "2026-10-19T15:30:00Z",
  "estado": "ACTIVO"
}
```

## Estructura

```
src/main/java/.../ms_prestamo/
├── client/        LibroClient, LibroDTO         (llamada REST síncrona a ms-catalogo)
├── config/        RabbitConfig                  (exchange, colas, bindings, conversor JSON)
├── controller/    PrestamoController
├── event/         PrestamoCreadoEvent, PrestamoDevueltoEvent, PrestamoEventPublisher
├── model/         Prestamo, EstadoPrestamo
├── repository/    PrestamoRepository
└── service/       PrestamoService
```

## Configuración (`application.properties`)

```properties
server.port=8081
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
catalogo.url=http://localhost:8080
```

## Ejecutar

1. Levantar RabbitMQ desde la raíz del repo:
```bash
   docker compose up -d
```
   Consola de administración: http://localhost:15672 (usuario `guest`, clave `guest`).
2. Levantar `ms-catalogo` (puerto 8080).
3. Levantar este servicio:
```bash
   ./mvnw spring-boot:run
```

> Este servicio debe arrancar antes que los consumidores, porque es quien declara el exchange y las colas.

## Probar

```bash
curl -X POST http://localhost:8081/prestamos \
  -H "Content-Type: application/json" \
  -d '{"libroId": 1, "usuario": "usuario1@test.com"}'
```

Después, en la consola de RabbitMQ (pestaña **Queues**) se ven los mensajes en `notificaciones.queue` e `historial.queue`, o ya consumidos si los servicios consumidores están corriendo.