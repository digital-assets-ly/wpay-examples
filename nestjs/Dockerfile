FROM node:lts-alpine3.21 AS builder
RUN npm install pm2 -g
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup
EXPOSE 8080
CMD ["pm2-runtime", "dist/main.js"]
