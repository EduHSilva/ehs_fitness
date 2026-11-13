# Fitness

Aplicativo de fitness (dieta e treinos) com dois clientes neste monorepo:

- `web/` — aplicação web Nuxt 4 / Vue 3, publicada em `/app/fitness/`.
- `legacy/flutter/` — app Flutter legado, mantido apenas como base para a
  futura migração para Kotlin em `app/`.

## Web

```bash
cd web
pnpm install
pnpm dev
```

Em produção o app é servido pelo nginx dos microservices em
`http://localhost/app/fitness/`. As chamadas `/api/*` do cliente são
repassadas pelo nginx do container para o gateway dos microservices.

## Docker

Local (build da imagem):

```bash
docker compose up -d --build
# http://localhost/app/fitness/
```

Produção (imagem publicada no GHCR):

```bash
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

- A imagem do GitHub Actions é `ghcr.io/eduhsilva/fitness-ui`, com tags `latest` e `<sha>`.
- Para fixar uma versão, defina `IMAGE_TAG=<sha>`; `FITNESS_UI_IMAGE` substitui a imagem completa.
- A rede externa padrão em produção é `microservices_backend` (mesma convenção de
  `admin`, `finances`, `study` e `portfolio`); se a rede tiver outro nome, defina
  `EHS_MICROSERVICES_NETWORK`.
- O serviço publica internamente a porta 80 com alias `fitness-ui`; não há porta no
  host. A rota `/app/fitness/` do nginx dos microservices encaminha para `fitness-ui:80`.
- Não são necessários segredos no compose do front.

Login Google: o `NUXT_PUBLIC_GOOGLE_CLIENT_ID` é compilado no bundle
estático, então precisa existir no build (local:
`FITNESS_GOOGLE_CLIENT_ID=... docker compose build`; CI: var de repositório
`FITNESS_GOOGLE_CLIENT_ID`). Vazio = botão oculto.

## Flutter (legado)

```bash
cd legacy/flutter
flutter pub get
flutter run
```

Configure `legacy/flutter/.env` com `URL_API` antes de executar.
