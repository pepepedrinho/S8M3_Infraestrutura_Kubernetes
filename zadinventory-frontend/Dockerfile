# ---------- Etapa 1: Build Angular ----------
FROM node:22-alpine AS angular-build

WORKDIR /app

COPY zadinventory/package*.json ./
RUN npm install

COPY zadinventory/ .
RUN npx ng build --configuration production

# ---------- Etapa 2: Runtime (Nginx) ----------
FROM nginx:alpine

COPY default.conf /etc/nginx/conf.d/default.conf
COPY --from=angular-build /app/dist/zadinventory-frontend/browser /usr/share/nginx/html

EXPOSE 8080