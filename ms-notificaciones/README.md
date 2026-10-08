# ms-notificaciones

Microservicio **consumidor** del sistema Biblioteca. Escucha los eventos `prestamo.creado` publicados por `ms-prestamo` y simula el envío de una notificación al usuario.

Parte de la actividad evaluada de RabbitMQ (DSY1107 - Desarrollo Cloud Native I).

## Stack

- Java 21, Spring Boot
- Spring for RabbitMQ (AMQP)
- Jackson (conversión JSON), Lombok

## Rol en la arquitectura

```
ms-prestamo --publish--> exchange biblioteca.events --(prestamo.creado)--> notificaciones.queue --> ms-notificaciones
```

La notificación es un efecto secundario del préstamo: el usuario no necesita esperar a que se envíe para recibir la confirmación de su préstamo, por eso se procesa de forma **asíncrona**. Si este servicio está caído, los mensajes se acumulan en la cola y se procesan cuando vuelve a arrancar.

## Cola y binding

| Elemento | Valor |
|---|---|
| Cola | `notificaciones.queue` |
| Binding | `prestamo.creado` (sobre el exchange `biblioteca.events`) |
| Mensaje esperado | `PrestamoCreadoEvent` (JSON) |

La cola y el binding los declara `ms-prestamo` en su `RabbitConfig`; este servicio solo consume.

## Estructura

```
src/main/java/.../ms_notificaciones/
├── config/        RabbitConfig               (conversor JSON)
├── event/         PrestamoCreadoEvent        (formato del mensaje)
├── listener/      NotificacionListener       (@RabbitListener sobre notificaciones.queue)
└── service/       NotificacionService        (simula el envío, escribe un log)
```

## Configuración (`application.properties`)

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

Este servicio no expone endpoints HTTP: solo escucha la cola.

## Ejecutar

1. RabbitMQ levantado (`docker compose up -d` desde la raíz del repo).
2. `ms-prestamo` arrancado al menos una vez (crea el exchange y las colas).
3. Arrancar este servicio:
```bash
   ./mvnw spring-boot:run
```

## Comprobar que funciona

Al crear un préstamo en `ms-prestamo`, la consola de este servicio muestra un log como:

```
Notificación enviada a usuario1@test.com: préstamo 1 ... creado
```

Las devoluciones (`prestamo.devuelto`) **no** generan notificación, porque ese evento no está enlazado a esta cola.