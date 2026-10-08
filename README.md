# Biblioteca · Comunicación entre microservicios con RabbitMQ

Actividad evaluada de **DSY1107 - Desarrollo Cloud Native I** (Duoc UC). Extiende el sistema Biblioteca (dominio Libro–Préstamo) a una arquitectura de microservicios que combina comunicación **síncrona** (REST) y **asíncrona** (RabbitMQ).

## Idea central

No toda comunicación entre servicios necesita una respuesta inmediata:

- **Síncrono:** `ms-prestamo` debe saber *en el momento* si el libro existe y tiene stock, porque de eso depende aceptar o rechazar el préstamo.
- **Asíncrono:** notificar al usuario y registrar el historial son efectos secundarios que no deben bloquear ni hacer fallar la operación principal.

## Arquitectura

```
                          (síncrono, REST)
Cliente --POST /prestamos--> ms-prestamo --GET /public/libros/{id}--> ms-catalogo
                                 |
                                 | publish (asíncrono)
                                 v
                    exchange "biblioteca.events" (topic)
                       |                        |
        prestamo.creado|                        |prestamo.*
                       v                        v
             notificaciones.queue         historial.queue
                       |                        |
                       v                        v
              ms-notificaciones           ms-historial
```

## Microservicios

| Servicio | Rol | Puerto | Descripción |
|---|---|---|---|
| [`ms-catalogo`](ms-catalogo) | Proveedor síncrono | 8080 | Catálogo de libros y stock |
| [`ms-prestamo`](ms-prestamo) | **Productor** | 8081 | Crea/devuelve préstamos, consulta stock y publica eventos |
| [`ms-notificaciones`](ms-notificaciones) | **Consumidor 1** | — | Simula el envío de notificaciones |
| [`ms-historial`](ms-historial) | **Consumidor 2** | 8082 | Guarda el historial de eventos |

## Configuración de RabbitMQ

| Elemento | Valor |
|---|---|
| Exchange | `biblioteca.events` (topic, durable) |
| Cola de notificaciones | `notificaciones.queue`, binding `prestamo.creado` |
| Cola de historial | `historial.queue`, binding `prestamo.*` |
| Routing keys | `prestamo.creado`, `prestamo.devuelto` |

El exchange, las colas y los bindings se declaran en `ms-prestamo/.../config/RabbitConfig.java`.

### Ejemplo de mensaje (`prestamo.creado`)

```json
{
  "eventId": "a3f1c2e0-7b4d-4a1e-9c3f-1234567890ab",
  "tipo": "prestamo.creado",
  "prestamoId": 1,
  "libroId": 2,
  "usuario": "usuario1@test.com",
  "fechaPrestamo": "2026-10-08T21:27:03.937394Z",
  "fechaDevolucionEstimada": "2026-10-22T21:27:03.937394Z",
  "estado": "ACTIVO"
}
```

## Cómo ejecutar

Requisitos: Java 21 y Docker.

Levantar, cada uno en su propia terminal y en este orden:

```bash
# 1. RabbitMQ (desde la raíz del repo)
docker compose up -d

# 2. Servicios (cada uno dentro de su carpeta)
cd ms-catalogo        && ./mvnw spring-boot:run
cd ms-prestamo        && ./mvnw spring-boot:run   # debe arrancar antes que los consumidores
cd ms-notificaciones  && ./mvnw spring-boot:run
cd ms-historial       && ./mvnw spring-boot:run
```

Consola de administración de RabbitMQ: http://localhost:15672 (usuario `guest`, clave `guest`).

> `ms-prestamo` debe arrancar primero porque es quien declara el exchange y las colas.

## Cómo probar

En PowerShell:

```powershell
# Crear un préstamo (flujo síncrono + evento prestamo.creado)
Invoke-RestMethod -Method Post -Uri http://localhost:8081/prestamos -ContentType "application/json" -Body '{"libroId":2,"usuario":"usuario1@test.com"}'

# Devolverlo (evento prestamo.devuelto)
Invoke-RestMethod -Method Post -Uri http://localhost:8081/prestamos/1/devolucion
```

| Caso | Resultado esperado |
|---|---|
| Crear préstamo del libro 1 o 2 | 201, con log en `ms-notificaciones` y `ms-historial` |
| Crear préstamo del libro 3 (stock 0) | 409 Libro sin stock, no se publica ningún evento |
| Crear préstamo del libro 99 | 404 El libro no existe, no se publica ningún evento |
| Devolver un préstamo | 200, log solo en `ms-historial` (notificaciones no escucha `devuelto`) |

Ver el historial guardado: http://localhost:8082/historial

## Qué demuestra cada resultado

- **Síncrono:** si el libro no existe o no tiene stock, el cliente recibe el error en la misma petición y no se publica nada.
- **Asíncrono y desacoplado:** `ms-prestamo` responde sin esperar a los consumidores; si un consumidor está apagado, sus mensajes quedan en la cola y se procesan al volver.
- **Routing:** el mismo evento `prestamo.creado` llega a las dos colas, pero `prestamo.devuelto` solo a `historial.queue`, según los bindings.

## Notas de alcance

- Los servicios usan H2 en memoria: los datos se pierden al reiniciar.
- Sin autenticación: la actividad evalúa mensajería; la seguridad con JWT y Entra ID se trabajó en la evaluación anterior.
- El stock no se descuenta al crear un préstamo.