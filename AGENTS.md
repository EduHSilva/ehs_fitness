# EHS Fitness — Guia do Repositório

## Visão geral

O repositório contém dois clientes para o EHS Fitness:

- `app/`: aplicativo Flutter.
- `web/`: aplicação web Nuxt 4 / Vue 3.

Não há código de backend neste repositório. Os dois clientes usam a URL configurada por ambiente para consumir a API externa.

## Flutter (`app/`)

- Código-fonte: `app/lib/`.
- Organização: `config/`, `models/`, `services/`, `view_models/`, `views/` e `widgets/`.
- Recursos: `app/assets/i18n/` e `app/assets/images/`.
- Configuração local: `app/.env`, com `URL_API`.

Comandos, executados em `app/`:

```bash
flutter pub get
flutter run
flutter analyze
flutter test
```

Use `flutter_lints`, nomes de arquivos em `snake_case.dart`, classes em `PascalCase` e membros em `camelCase`. Mantenha chamadas à API em `services/` e componentes compartilhados em `widgets/`.

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

- Adicione testes focados ao introduzir lógica nova: `*_test.dart` em Flutter e testes para composables/serviços no web quando a infraestrutura estiver disponível.
- Preserve alterações não relacionadas já presentes no diretório de trabalho.
- Não versione `.env`, tokens ou outras credenciais.
- Prefira commits curtos e específicos, por exemplo `refactor workout web feature`.
