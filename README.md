# Backend — Junta de Vecinos Zaror

| Carpeta | Rol | Puerto |
|---|---|---|
| `api-gateway` | Punto de entrada; valida el JWT y enruta | **9090** (publicado en el PC) |
| `ms-admin` | Cuentas, login (emite JWT), perfil | 9090 (solo red interna Docker) |
| `ms-reservas` | Dependencias, reservas, historial | 9090 (solo red interna Docker) |
| `ms-finanzas` | Pagos, ingresos y gastos | 9090 (solo red interna Docker) |
| `ms-notificaciones` | Avisos por usuario o perfil | 9090 (solo red interna Docker) |
| `mysql` (Docker) | Una base por microservicio | 3306 (solo red interna Docker) |

> Todo usa el **9090**. Cada servicio corre en su propio contenedor, así que pueden compartir el número; solo el gateway se publica hacia el PC. No se usa el 8080.

```bash
build-all.bat   # o ./build-all.sh   (compila con el mirror de Maven del curso)
docker compose up -d --build
```
- Spring Boot 4.0.5, Java 21, Spring Security (resource server, HS256), Spring Data JPA, MySQL 8.4.
- La clave JWT compartida se define en `docker-compose.yml` (`SECRET`, mínimo 32 caracteres) y en cada `application.properties` (valor por defecto para correr desde el IDE).
- Para correr un servicio desde el IDE necesita un MySQL en `localhost:3306` (usuario `root`, variables `DB_USER`, `DB_PASSWORD`, `DB_URL`); `ms-finanzas` necesita `ms-reservas` en `localhost:9092` y `ms-notificaciones` en `localhost:9095`. Fuera de Docker, para correr varios servicios a la vez en un mismo PC, ponga una variable `SERVER_PORT` distinta a cada uno (p. ej. 9092 y 9095, que son los valores que esperan por defecto `RESERVAS_URL` y `NOTIFICACIONES_URL`); dentro de Docker todos van en 9090.
- Avisos: `ms-reservas` y `ms-finanzas` crean avisos en `ms-notificaciones` por `POST /internal/notificaciones` con la clave `INTERNAL_KEY` (definida en `docker-compose.yml`). Si ese servicio no responde, la reserva o el pago igual se completan y solo se omite el aviso.
- Cuentas demo (clave `Demo1234`): `vecino@junta.cl`, `secretaria@junta.cl`, `tesoreria@junta.cl`. Los ids 1, 2 y 3 son usados por los datos de demostración de los otros servicios.

## Acceso desde la app móvil
La app se conecta a la **IP del computador** que ejecuta Docker (puerto 9090 del gateway), nunca a `localhost`. Use `mostrar-ip.bat` / `mostrar-ip.sh` para ver la IP y permita el puerto 9090 en el firewall. Los microservicios no se publican hacia el PC: todas las pruebas se hacen por el gateway (`IP:9090`).
