# ---------- Etapa 1: Build Angular ----------
FROM node:22-alpine AS angular-build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

RUN npx ng build --configuration production


# ---------- Etapa 2: Build Auth Proxy ----------
FROM golang:1.23-alpine AS proxy-build

WORKDIR /proxy

COPY proxy/go.mod ./
RUN go mod download

COPY proxy/main.go ./

RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
    go build -o auth-proxy main.go


# ---------- Etapa 3: Runtime ----------
FROM nginx:alpine

COPY default.conf /etc/nginx/conf.d/default.conf

COPY --from=angular-build \
    /app/dist/zadinventory-frontend/browser \
    /usr/share/nginx/html

COPY --from=proxy-build \
    /proxy/auth-proxy \
    /usr/local/bin/auth-proxy

RUN chmod +x /usr/local/bin/auth-proxy

EXPOSE 8080

# O Auth Proxy escuta em 8081 (fixo, interno ao container).
# O Nginx escuta em 8080, que é a porta que o Cloud Run expõe (PORT=8080).
CMD ["/bin/sh", "-c", "PORT=8081 /usr/local/bin/auth-proxy & nginx -g 'daemon off;'"]