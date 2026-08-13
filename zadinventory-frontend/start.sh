#!/bin/sh
# Sobe o Auth Proxy e o Nginx e derruba o container se QUALQUER um dos dois cair.
#
# Antes o CMD era "auth-proxy & nginx -g 'daemon off;'": se o proxy morresse,
# o Nginx continuava de pe, o probe TCP do Cloud Run na 8080 continuava
# passando e todo /api/ virava 502 num container marcado como saudavel.
#
# Nao usa "wait -n" porque o ash do Alpine nem sempre suporta essa flag.

set -eu

# O Auth Proxy escuta numa porta interna fixa; quem responde na porta do
# Cloud Run (PORT, 8080 por padrao) e o Nginx, definido em default.conf.
PORT=8081 /usr/local/bin/auth-proxy &
PID_PROXY=$!

nginx -g 'daemon off;' &
PID_NGINX=$!

while kill -0 "$PID_PROXY" 2>/dev/null && kill -0 "$PID_NGINX" 2>/dev/null; do
    sleep 5
done

echo "auth-proxy ou nginx terminou - encerrando o container" >&2
exit 1
