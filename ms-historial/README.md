# ms-historial

Microservicio **consumidor** del sistema Biblioteca. Registra en una base de datos cada evento de préstamo (`prestamo.creado` y `prestamo.devuelto`) para auditoría y estadísticas de uso.

Parte de la actividad evaluada de RabbitMQ (DSY1107 - Desarrollo Cloud Native I).

## Stack

- Java 21, Spring Boot
- Spring for RabbitMQ (AMQP)
- Spring Web, Spring Data JPA (H2 en memoria)
- Lombok

## Rol en la arquitectura

```
ms-prestamo --publish--> exchange biblioteca.events --(prestamo.*)--> historial.queue --> ms-historial --> H2
```

El registro histórico es un efecto secundario del préstamo y no condiciona su respuesta, por lo que se procesa de forma **asíncrona**. Si este servicio falla o está lento, no afecta la creación de préstamos.

## Cola y binding

| Elemento | Valor |
|---|---|
| Cola | `historial.queue` |
| Binding | `prestamo.*` (cubre `prestamo.creado` y `prestamo.devuelto`) |
| Mensaje esperado | JSON de `PrestamoCreadoEvent` o `PrestamoDevueltoEvent` |

Como a la misma cola llegan dos tipos de evento con campos distintos, el listener recibe el mensaje como `Map` y el service lee el campo `tipo` para distinguirlos.

La cola y el binding los declara `ms-prestamo` en su `RabbitConfig`; este servicio solo consume.

## Endpoint

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/historial` | Lista todos los eventos registrados |

### Ejemplo de respuesta

```json
[
  {
    "id": 1,
    "tipoEvento": "prestamo.creado",
    "prestamoId": 1,
    "libroId": 2,
    "usuario": "usuario1@test.com",
    "fecha": "2026-10-08T21:27:03.937394Z"
  },
  {
    "id": 2,
    "tipoEvento": "prestamo.devuelto",
    "prestamoId": 1,
    "libroId": 2,
    "usuario": "usuario1@test.com",
    "fecha": "2026-10-08T21:27:08.766989600Z"
  }
]
```

## Estructura

```
src/main/java/.../ms_historial/
├── config/        RabbitConfig               (conversor JSON)
├── controller/    HistorialController        (GET /historial)
├── listener/      HistorialListener          (@RabbitListener sobre historial.queue)
├── model/         HistorialEntry             (entidad JPA)
├── repository/    HistorialRepository
└── service/       HistorialService           (guarda cada evento y escribe un log)
```

## Configuración (`application.properties`)

```properties
server.port=8082
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## Ejecutar

1. RabbitMQ levantado (`docker compose up -d` desde la raíz del repo).
2. `ms-prestamo` arrancado al menos una vez (crea el exchange y las colas).
3. Arrancar este servicio:
```bash
   ./mvnw spring-boot:run
```

## Comprobar que funciona

Al crear y devolver un préstamo en `ms-prestamo`, la consola muestra:

```
Historial registrado: prestamo.creado | préstamo 1 | libro 2 | usuario usuario1@test.com
Historial registrado: prestamo.devuelto | préstamo 1 | libro 2 | usuario usuario1@test.com
```

y `http://localhost:8082/historial` devuelve ambas entradas.

> La base H2 es en memoria: los datos se pierden al reiniciar el servicio.