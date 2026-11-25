#!/usr/bin/env bash
# Muestra la IP de este computador en la red (la que debe usar la app móvil).
echo "IP(s) de este computador:"
(hostname -I 2>/dev/null || ipconfig getifaddr en0 2>/dev/null) | tr ' ' '\n' | grep -E '^[0-9]+\.' 
echo; echo 'En la app: login > "Servidor: ..." > escribir IP:9090 (ej: 192.168.1.50:9090)'
