# EHS Fitness — Guia do Repositório

## Visão geral

O repositório contém dois clientes para o EHS Fitness:

- `web/`: aplicação web Nuxt 4 / Vue 3 (publicada em `/app/fitness/`).
- `app/`: aplicativo Android nativo em Kotlin + Jetpack Compose (`com.ehs.fitness`).

Não há código de backend neste repositório. Os dois clientes usam a URL configurada
por ambiente para consumir a API externa. O app Flutter legado foi removido.

## Escopo do produto

- **Web**: superfície de gestão. O aluno sem profissional cria e mantém os próprios
  treinos, agenda e dieta. `HEALTH_PERSONAL` gerencia treinos/agenda dos alunos
  vinculados e `HEALTH_NUTRITIONIST` gerencia a dieta; ambos leem o plano completo.
- **App Android**: MVP somente leitura para o aluno (início, treinos, agenda, dieta e
  perfil). Gestão de plano acontece apenas no web.
- O aluno com profissional ativo no domínio fica somente leitura naquele domínio
  (personal → treinos/agenda; nutricionista → dieta).
- Regras completas e matriz de permissões: `regras-negocio.md`. Pendências: `TODO.md`.

## App Android (`app/`)

- Projeto Gradle em `app/`; módulo da aplicação em `app/app/`.
- Código-fonte: `app/app/src/main/java/com/ehs/fitness/`.
- Configuração local: `app/local.properties` (não versionado), com
  `fitness.apiBaseUrl` e `fitness.googleWebClientId`; as mesmas chaves existem como
  variáveis `FITNESS_API_BASE_URL` e `FITNESS_GOOGLE_WEB_CLIENT_ID`.
- Recursos: `app/app/src/main/res/` (tema claro/escuro, ícones, strings).

Organização do código:

```text
core/
  network/        # cliente HTTP, envelope da API, erros
  prefs/          # SharedPreferences
  session/        # sessão autenticada persistida
  settings/       # tema claro/escuro
  links/          # páginas institucionais (privacidade, senha)
  events/         # aviso de dados alterados entre telas
  ui/theme/       # tokens visuais
  ui/components/  # componentes Compose compartilhados
data/
  remote/         # DTOs e interfaces Retrofit
  mappers/        # DTO -> modelo de domínio
  model/          # modelos usados pelas telas
  repository/     # casos de uso de acesso à API
domain/           # regras puras (auth, workouts, diet) + testes JVM
feature/          # auth, home, workouts, diet, profile (tela + ViewModel)
navigation/       # Routes e FitnessApp (drawer, top bar, bottom bar, NavHost)
di/               # AppContainer (injeção manual)
```

Regras para Kotlin/Compose:

- Chamadas HTTP ficam em `data/remote` (contrato) e `data/repository` (uso). Telas e
  ViewModels não montam URL nem payload.
- Validação, normalização e formatação puras ficam em `domain/`, cobertas por testes
  em `app/app/src/test/java/com/ehs/fitness/domain/`.
- Cada tela tem um ViewModel com `StateFlow`; a tela apenas coleta o estado e dispara
  ações. Erros de rede são convertidos em mensagem com `userMessage(...)`.
- Use os tokens de `core/ui/theme` (`MaterialTheme.colorScheme` e
  `FitnessTheme.colors`); não escreva hex nos componentes.
- O app é somente pt-BR. Envie `Accept-Language: pt_BR` (o backend compara o valor
  exato) e `observation` (não `obs`) nos alimentos.
- O escopo do app é **somente leitura**: não adicione telas ou ações de criar, editar ou
  excluir treino, agenda ou refeição. A gestão do plano acontece apenas no web.
- Novas dependências entram em `app/gradle/libs.versions.toml` e são referenciadas por
  alias em `app/app/build.gradle.kts`.

Comandos, executados em `app/` (Windows: `gradlew.bat`):

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

### Limitações conhecidas do backend de fitness

O app contorna o que dá e o restante está registrado no TODO do backend:

- A agenda é lida em `GET /fitness/workouts/week` (7 dias; `workoutId` nulo = descanso) e
  gravada com `PUT /fitness/workouts/week`. `PUT /fitness/workout` aceita `dayOfWeek` no
  DTO, mas o handler o ignora.
- `GET /fitness/workouts` e `GET /fitness/diet/meals` devolvem `data: []` quando vazios
  (os clientes também tratam `null` como lista vazia). A refeição traz
  `food_id`/`food_name`; a edição remonta o formulário a partir desse `GET`.
- `GET /fitness/workout?id=` e `GET /fitness/diet/meal?id=` respondem 404 para id
  inexistente.
- Existe vínculo aluno ↔ profissional com convites por e-mail (`/fitness/students`):
  o profissional usa `student_id` (nunca `user_id`) para ler/gravar o plano do aluno
  vinculado; o backend responde 403 quando o aluno tem profissional ativo no domínio.
  Não há endpoint para o aluno listar os próprios vínculos ativos (ver TODO do backend).
- `/users/auth/me` devolve `token: null`: os clientes preservam o token local até um 401.
- Carga, descanso e observações são opcionais: campo vazio vai como `null` no payload.

## Web (`web/`)

- Rotas e composição visual: `web/app/pages/`.
- Componentes visuais compartilhados: `web/app/components/`.
- Infraestrutura compartilhada, como cliente autenticado: `web/app/composables/`.
- Tipos transversais: `web/app/types/`.
- Funcionalidades de domínio: `web/app/features/<domínio>/`.
- Proxy da API: `web/server/api/[...path].ts`.
- Configuração local: `web/.env`, com `URL_API`.

Cada domínio web segue esta estrutura:

```text
features/<domínio>/
  types.ts             # modelos do domínio
  <domínio>.api.ts     # contrato e chamadas HTTP
  use<Domínio>.ts      # estado, validações e casos de uso da tela
  *.repository.ts      # somente persistência local intencional
```

Regras para Vue/Nuxt:

- Páginas não devem conter chamadas HTTP, tipos de domínio, acesso a `localStorage`, transformação de payload ou regras de negócio.
- Chamadas HTTP ficam em `*.api.ts`; estado, validação e tratamento de carregamento/erro ficam em `use<Domínio>.ts`.
- Não use mocks persistidos no navegador para simular recursos da API. Se o contrato ainda não estiver disponível, crie a função de integração, marque o ponto com `TODO` e apresente o erro ao usuário.
- O armazenamento local de token e usuário autenticado é permitido como persistência de sessão, não como fonte de dados de domínio.
- A área profissional é identificada pelas roles `HEALTH_PERSONAL`/`HEALTH_NUTRITIONIST`. O aluno com profissional ativo no domínio deve ver o plano em modo somente leitura (sem botões de criar/editar/excluir); o gate depende do status do vínculo na API.
- Ao integrar o plano de um aluno, envie `student_id` (nunca `user_id`) e valide o papel no domínio: personal escreve treinos/agenda e nutricionista escreve dieta; ambos leem o plano completo.
- O web é a única superfície de gestão de plano; o app é consulta. Não mova criação/edição para o app.

Comandos, executados em `web/`:

```bash
pnpm dev
pnpm lint
pnpm typecheck
pnpm build
pnpm preview
```

O projeto declara `pnpm@11.9.0`. Use ESLint antes de entregar alterações web. Caso `typecheck` falhe por dependências ausentes ou incompatíveis, registre a falha e não a contorne alterando tipos sem necessidade.

## Qualidade e mudanças

- Adicione testes focados ao introduzir lógica nova: testes JVM em
  `app/app/src/test/java/com/ehs/fitness/domain/` no Android e testes para
  composables/serviços no web quando a infraestrutura estiver disponível.
- Preserve alterações não relacionadas já presentes no diretório de trabalho.
- Não versione `.env`, `local.properties`, tokens ou outras credenciais.
- Prefira commits curtos e específicos, por exemplo `refactor workout web feature`.
