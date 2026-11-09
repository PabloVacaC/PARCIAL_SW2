# Etapa 1: Compilar Angular con Node 22 (requerido por Angular CLI)
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build --configuration=production

# Etapa 2: Servir con Nginx
FROM nginx:alpine
# Copiar el build compilado
COPY --from=build /app/dist/codeforge-frontend/browser /usr/share/nginx/html
# Copiar configuración con proxy reverso
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
