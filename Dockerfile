# syntax=docker/dockerfile:1

# --------------------------------------------------
# BUILD - Nuxt (SPA/estático) em web/
# --------------------------------------------------
FROM node:22-alpine AS build

WORKDIR /app

RUN corepack enable

COPY web/package.json web/pnpm-lock.yaml web/pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile

COPY web/ ./

# Publicado pelo nginx dos microservices em /app/fitness/.
ARG NUXT_APP_BASE_URL=/app/fitness/
# "/" mantém as chamadas /api/* na mesma origem; o nginx deste container
# repassa /app/fitness/api/* para o gateway.
ARG URL_API=/
# Vazio esconde o botão "Entrar com Google" (o GSI só é carregado com client ID).
ARG NUXT_PUBLIC_GOOGLE_CLIENT_ID=

ENV NUXT_APP_BASE_URL=$NUXT_APP_BASE_URL
ENV URL_API=$URL_API
ENV NUXT_PUBLIC_GOOGLE_CLIENT_ID=$NUXT_PUBLIC_GOOGLE_CLIENT_ID

RUN pnpm generate

# --------------------------------------------------
# RUNTIME - nginx estático
# --------------------------------------------------
FROM nginx:1.28-alpine AS runtime

COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/.output/public /usr/share/nginx/html/app/fitness

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1/app/fitness/ || exit 1
