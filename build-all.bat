@echo off
rem Compila los 4 proyectos (usa el mirror de Maven de configuracion-maven). Luego: docker compose up -d --build
for %%S in (api-gateway ms-admin ms-reservas ms-finanzas ms-notificaciones) do (
  pushd %%S
  call .\mvnw clean package -DskipTests -s ..\configuracion-maven\settings.xml
  popd
)
