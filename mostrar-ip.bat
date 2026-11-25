@echo off
rem Muestra la IP de este computador en la red (la que debe usar la app movil).
echo IP(s) de este computador:
ipconfig | findstr /R /C:"IPv4"
echo.
echo En la app: pantalla de login ^> "Servidor: ..." ^> escribir  IP:9090   (ej: 192.168.1.50:9090)
