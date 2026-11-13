# Arquitetura do app Flutter

## Visão geral

O app segue uma separação leve de responsabilidades, com `ViewModel` entre a interface e os serviços remotos. O ponto de entrada é `lib/main.dart`, que inicializa localização, variáveis de ambiente, tema, rotas e restauração de sessão.

```text
View (widgets/telas)
        ↓
ViewModel (estado e casos de uso)
        ↓
Service (HTTP, cache e serialização)
        ↓
API externa / SharedPreferences
```

## Estrutura

```text
lib/
  config/       # ambiente, cliente HTTP autenticado, tema e utilitários
  models/       # contratos e serialização de dados
  services/     # integração HTTP, cache local e operações remotas
  view_models/  # estado observável e coordenação dos casos de uso
  views/        # telas, abas, modais e fluxo de navegação
  widgets/      # componentes visuais reutilizáveis
  main.dart     # bootstrap, rotas e verificação de sessão
```

Os domínios atuais incluem usuário/autenticação, treinos, dieta, hábitos e área profissional. Organize novos arquivos dentro da camada correspondente e, quando o domínio crescer, prefira subpastas por domínio (por exemplo `models/health/` e `views/health/`).

## Responsabilidades

### `config/`

- `AppConfig` centraliza a URL da API, token, usuário autenticado e o cliente HTTP com Bearer token.
- O cliente trata respostas `401`, limpa a sessão e direciona o app ao login.
- `design_system.dart` concentra tema e tokens visuais.
- Não coloque regras de tela ou chamadas específicas de domínio nesta camada.

### `models/`

- Representam dados de domínio e payloads de request/response.
- Cada modelo deve expor conversão consistente entre a API e Dart (`fromJson`/`toJson` quando aplicável).
- Modelos não devem conhecer widgets, `BuildContext` ou HTTP.

### `services/`

- São a fronteira com a API e com armazenamento local intencional.
- Usam `AppConfig.getHttpClient()` para requisições autenticadas.
- Fazem parsing de resposta, montam payloads e lançam/retornam falhas de integração de forma consistente.
- Cache offline, quando necessário, deve ser identificado por usuário e tratado aqui — não nas views.

### `view_models/`

- Coordenam chamadas de serviços e expõem estado para a interface.
- O padrão atual usa `ValueNotifier` para carregamento, erro e coleções.
- Validam entradas e atualizam o estado após operações de criar, editar ou excluir.
- Não devem importar widgets de tela ou navegar diretamente; a view decide a apresentação e a navegação.

### `views/` e `widgets/`

- Views renderizam o estado do ViewModel e encaminham ações do usuário.
- Use `ValueListenableBuilder` para observar `ValueNotifier`.
- Mantenha lógica de negócio, serialização e HTTP fora das telas.
- Extraia padrões reutilizados para `widgets/`; componentes de uso único podem ficar próximos à view do domínio.

## Sessão e ambiente

- O arquivo `app/.env` fornece `URL_API` e, quando necessário, `GOOGLE_CLIENT_ID`.
- Credenciais e tokens não devem ser versionados.
- `SharedPreferences` guarda exclusivamente dados de sessão e caches intencionais, nunca mocks para simular respostas da API.
- A restauração e validação inicial de sessão ocorrem em `main.dart` antes de escolher a rota inicial.

## Fluxo para uma funcionalidade nova

1. Crie ou estenda os modelos de request/response em `models/`.
2. Implemente a operação remota em um serviço do domínio.
3. Exponha o estado e a operação no ViewModel correspondente.
4. Conecte a view ao ViewModel, exibindo carregamento, sucesso e erro.
5. Adicione testes de serviço/ViewModel quando houver lógica nova.

## Regras de manutenção

- Não criar dados simulados persistidos para substituir endpoints ausentes. Registre um `TODO` no serviço, explicando o contrato pendente, e apresente a indisponibilidade na tela.
- Não acessar `SharedPreferences` diretamente em views ou ViewModels, salvo uma necessidade explicitamente justificada.
- Não repetir URLs, headers ou tratamento de autenticação nos serviços; use `AppConfig`.
- Execute `flutter analyze` antes de entregar mudanças e `flutter test` quando houver testes relevantes.

## Evolução recomendada

O padrão atual com `ValueNotifier` é suficiente para a escala presente e deve ser mantido nas correções pequenas. Para funcionalidades novas ou módulos em refatoração, siga esta evolução incremental:

1. **Padronizar resultados de serviço.** Use `ServiceResult<T>` (`ServiceSuccess` ou `ServiceFailure`) em serviços novos ou quando um serviço existente for alterado. A migração dos serviços atuais será progressiva para evitar uma quebra ampla.
2. **Evitar instanciação dentro de ViewModels.** Receba serviços pelo construtor, com uma implementação padrão opcional. Isso reduz acoplamento e permite testes com fakes sem rede.
3. **Agrupar por domínio quando houver crescimento.** Um domínio completo pode evoluir para `features/<domínio>/{models,services,view_models,views,widgets}` sem exigir uma mudança global de uma vez.
4. **Centralizar estados assíncronos.** Para telas mais complexas, prefira um estado explícito (`initial`, `loading`, `success`, `error`) ao conjunto disperso de múltiplos `ValueNotifier`s.
5. **Escolher gerência de estado somente quando necessário.** Se houver estado compartilhado entre várias telas ou regras de atualização complexas, adote uma solução única (por exemplo, `provider` ou `flutter_riverpod`) após uma decisão técnica; não misture bibliotecas de estado por funcionalidade.

### Padrão preferido para novas dependências

```dart
class WorkoutViewModel {
  WorkoutViewModel({WorkoutService? service})
      : _service = service ?? WorkoutService();

  final WorkoutService _service;
}
```

Esse padrão preserva o comportamento atual e permite injetar um serviço de teste. A migração deve ser feita domínio a domínio, começando por serviços ou ViewModels alterados no mesmo trabalho; não faça uma conversão global apenas por padronização.

O projeto já fornece `BaseViewModel`, com `AsyncState` explícito e os notificadores legados de carregamento/erro. ViewModels devem estendê-lo; as telas existentes podem continuar usando `isLoading` e `errorMessage` durante a transição.
