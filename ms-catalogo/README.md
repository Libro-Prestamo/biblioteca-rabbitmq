# ms-catalogo

Microservicio de catálogo del sistema Biblioteca. Mantiene los libros y su stock, y expone un endpoint REST que `ms-prestamo` consulta de forma **síncrona** antes de aceptar un préstamo.

Parte de la actividad evaluada de RabbitMQ (DSY1107 - Desarrollo Cloud Native I). Este servicio **no usa RabbitMQ**: es el lado síncrono de la arquitectura.

## Stack

- Java 21, Spring Boot
- Spring Web, Spring Data JPA (H2 en memoria)
- Lombok

## Rol en la arquitectura

```
ms-prestamo --GET /public/libros/{id}--> ms-catalogo     (comunicación síncrona, espera respuesta)
```

`ms-prestamo` necesita saber de inmediato si el libro existe y tiene stock, porque de eso depende que el préstamo se acepte o se rechace en la misma petición HTTP. Por eso esta comunicación es síncrona y no pasa por RabbitMQ.

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/public/libros` | Lista todos los libros | 200 |
| GET | `/public/libros/{id}` | Devuelve un libro por id | 200, 404 |

### Ejemplo de respuesta

```json
{
  "id": 1,
  "titulo": "Cien años de soledad",
  "autor": "Gabriel García Márquez",
  "isbn": "978-0307474728",
  "stock": 3,
  "disponible": true
}
```

## Datos de prueba

La base de datos H2 es en memoria, así que empieza vacía en cada arranque. `DataLoader` carga estos libros al iniciar:

| id | Título | Stock | Uso en las pruebas |
|---|---|---|---|
| 1 | Cien años de soledad | 3 | Préstamo exitoso |
| 2 | El Principito | 5 | Préstamo exitoso |
| 3 | Don Quijote de la Mancha | 0 | Caso sin stock (`ms-prestamo` responde 409) |

## Estructura

```
src/main/java/.../ms_catalogo/
├── config/        DataLoader               (carga libros de prueba al arrancar)
├── controller/    LibroController
├── model/         Libro
└── repository/    LibroRepository
```

## Configuración

Usa el puerto por defecto, `8080`. `ms-prestamo` lo referencia con:

```properties
catalogo.url=http://localhost:8080
```

## Ejecutar

```bash
./mvnw spring-boot:run
```

Comprobar que funciona: http://localhost:8080/public/libros/1

## Notas de alcance

- Sin autenticación: los endpoints son públicos, porque esta actividad evalúa la mensajería y no la seguridad (el control de acceso con JWT y Entra ID se trabajó en la evaluación anterior).
- El stock no se descuenta al crear un préstamo; queda fuera del alcance de la actividad.