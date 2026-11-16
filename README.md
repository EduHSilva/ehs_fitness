# Fitness

Aplicativo de fitness (treinos e alimentação) com dois clientes neste monorepo:

- `web/` — aplicação web Nuxt 4 / Vue 3, publicada em `/app/fitness/`.
- `app/` — aplicativo Android nativo em Kotlin + Jetpack Compose (`com.ehs.fitness`).

O app Flutter que existia em `legacy/flutter/` foi removido; o app Android é o cliente
mobile oficial. O escopo-alvo é: **web** concentra a gestão (aluno autônomo, personal e
nutricionista) e o **app** é um MVP somente leitura para o aluno. As regras estão em
`regras-negocio.md`; a migração pendente, em `TODO.md`.

## Web

```bash
cd web
pnpm install
pnpm dev
```

Em produção o app é servido pelo nginx dos microservices em
`http://localhost/app/fitness/`. As chamadas `/api/*` do cliente são
repassadas pelo nginx do container para o gateway dos microservices.

## App Android

```bash
cd app
./gradlew :app:assembleDebug       # APK de debug
./gradlew :app:testDebugUnitTest   # testes JVM das regras de domínio
```

No Windows use `gradlew.bat`. O APK sai em `app/app/build/outputs/apk/debug/`.

Configuração em `app/local.properties` (não versionado) ou por variável de ambiente:

| Chave | Variável | Padrão |
| --- | --- | --- |
| `fitness.apiBaseUrl` | `FITNESS_API_BASE_URL` | `http://10.0.2.2/` |
| `fitness.googleWebClientId` | `FITNESS_GOOGLE_WEB_CLIENT_ID` | vazio (botão Google oculto) |

O padrão `10.0.2.2` é o host da máquina visto pelo emulador Android e aponta para o
nginx local. Para o gateway direto, use `http://10.0.2.2:8080/`.

Telas atuais: landing/login/cadastro, início, treinos (criar, editar, excluir), agenda
semanal, alimentação (listar, criar e excluir refeição) e perfil (nome, tema, política,
sair). A criação/edição ainda existe no app por compatibilidade; o escopo-alvo é somente
leitura e a remoção depende da paridade de gestão no web (`TODO.md`).

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
