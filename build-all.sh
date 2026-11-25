#!/usr/bin/env bash
# Compila los 4 proyectos (usa el mirror de Maven de configuracion-maven). Luego: docker compose up -d --build
set -e
for s in api-gateway ms-admin ms-reservas ms-finanzas ms-notificaciones; do
  (cd "$s" && ./mvnw clean package -DskipTests -s ../configuracion-maven/settings.xml)
done
