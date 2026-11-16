# Regras de negócio — EHS Fitness

> Plataforma (auth, roles, isolamento): `D:/Projetos/ehs_microservices/regras-negocio.md`.
> O serviço `fitness` é um app Go; o login e as roles vêm do `users`.
> Pendências de implementação: `TODO.md` (clientes) e `D:/Projetos/ehs_microservices/TODO.md`.

## Escopo dos clientes

- **Web** é a superfície de gestão. O aluno sem profissional cria e mantém os próprios
  treinos, agenda e dieta; o personal trainer e o nutricionista gerenciam os alunos
  vinculados.
- **App Android** é um MVP de consulta para o aluno: exibe início, treinos, agenda,
  dieta e perfil. Não cria, edita nem exclui plano. A gestão acontece apenas no web.
- O servidor não distingue o tipo de cliente. O modo somente leitura do app é regra de
  interface; a autorização real continua sendo validada pela API.

## Acesso e papéis

- Roles: `HEALTH_USER` (aluno), `HEALTH_PERSONAL` (personal trainer) e
  `HEALTH_NUTRITIONIST` (nutricionista). Todas entram com `app: FITNESS`.
- `HEALTH_PERSONAL` e `HEALTH_NUTRITIONIST` ganham empresa automática no cadastro
  ("<nome> personal" / "<nome> nutricionista").
- O gateway valida `aud` + role `HEALTH_*`. O serviço `fitness` valida o vínculo
  aluno ↔ profissional, o domínio do profissional e se o aluno está em modo somente
  leitura.
- `ADMIN` tem acesso administrativo pela regra da plataforma, mas não tem fluxo de
  gestão de alunos no produto.

### Matriz de permissões

| Ação | Aluno sem profissional no domínio | Aluno com profissional no domínio | Personal trainer | Nutricionista |
| --- | --- | --- | --- | --- |
| Ver o próprio plano / do aluno vinculado | Sim | Sim | Próprio + alunos | Próprio + alunos |
| Criar, editar e excluir treino/agenda | Sim | Não | Próprio + alunos vinculados | Próprio (leitura dos alunos) |
| Criar, editar e excluir refeição | Sim | Não | Próprio (leitura dos alunos) | Próprio + alunos vinculados |
| Listar/gerenciar alunos e convites | Não | Não | Sim | Sim |
| Ver config e acompanhamento diário | Sim | Sim | Próprio | Próprio |

- "Profissional no domínio" significa personal para treinos/agenda e nutricionista
  para dieta. O aluno pode ter os dois ao mesmo tempo; cada vínculo bloqueia a edição
  apenas no seu domínio.
- Sem vínculo ativo no domínio, o aluno volta a poder editar aquele domínio.
- Ser profissional não impede manter o próprio plano: o papel acrescenta a gestão de
  alunos, não substitui a experiência de aluno.

## Vínculo aluno ↔ profissional

- O profissional convida o aluno **por e-mail**. O vínculo só existe depois que o aluno
  aceita; antes disso o profissional não vê nenhum dado do aluno.
- O convite só pode ser aceito por uma conta com role `HEALTH_USER` cujo e-mail seja o
  convidado. Se a pessoa ainda não tem conta, precisa se cadastrar como aluno antes de
  aceitar.
- Pode haver **no máximo um profissional ativo por domínio**: um personal e um
  nutricionista. Um novo vínculo no mesmo domínio exige encerrar o anterior.
- Convites pendentes expiram (prazo padrão: 7 dias), podem ser reenviados e revogados.
- Qualquer um dos dois lados pode encerrar o vínculo a qualquer momento.
- Se o aluno perder a role `HEALTH_USER` ou o profissional perder a role do domínio, o
  vínculo deixa de valer e o acesso é revogado.
- O profissional não edita dados da conta do aluno (nome, e-mail, senha, roles). Ele vê
  nome, e-mail, objetivo e o plano.
- Um profissional não pode convidar a própria conta.

## Propriedade e acesso aos dados

- Todo treino, refeição e agendamento pertence ao **aluno** (`student_id`), não ao autor.
  O autor da última escrita é registrado (`author_id`) para auditoria.
- As rotas de leitura e escrita aceitam `student_id` opcional; ausente = o próprio
  usuário do token. O cliente nunca envia `user_id` para escolher outro aluno.
- Escrita do profissional exige vínculo ativo e role do domínio. Escrita do aluno é
  recusada quando há profissional ativo naquele domínio. Falha de permissão = 403.
- Aluno inexistente ou sem vínculo ativo = 404, para não revelar a existência da conta.
- Ao encerrar o vínculo, os planos permanecem com o aluno e ele recupera a edição; o
  profissional perde o acesso.
- Configurações e acompanhamento diário são privados do aluno; o profissional não
  acessa.
- Catálogos de exercícios e alimentos são globais (não pertencem ao aluno).
- Listagens vazias devem devolver `[]`, nunca `null`.

## Treinos

- Um treino tem nome e dia da semana (1–7). Só pode existir **um treino por dia** para
  o mesmo aluno; dia 0 significa "não agendado".
- Nome é único por aluno; cada treino precisa de pelo menos um exercício.
- A agenda é gravada em bloco com os 7 dias (`PUT /fitness/workouts/week`), cada treino
  em no máximo um dia, todos do aluno-alvo. A gravação substitui a semana inteira.
- A leitura da agenda deve ser própria (`GET /fitness/workouts/week`), em vez de cada
  cliente derivar a semana do `dayOfWeek`.
- Exercício: referência ao catálogo, séries, repetições, carga, descanso e observações.
  Carga, descanso e observações são opcionais e vão como `null` quando vazios.
- Não há status/ciclo de vida do treino. Padrão da interface: 3 séries × 12 repetições.
- Só o aluno autorizado ou o profissional do domínio edita/exclui.

## Dieta

- Refeição: nome (único por aluno), horário e pelo menos um alimento.
- Alimento: referência ao catálogo (valores por 100 g), quantidade e observação. O campo
  correto é `observation` (não `obs`).
- A resposta de refeição expõe `food_id` e `food_name` para permitir exibir o alimento e
  remontar o payload de edição.
- O backend não calcula totais nem metas de macros. A busca de alimentos é por trecho do
  nome (case-insensitive, limite 30) e a UI exige 2 caracteres.
- Só o aluno autorizado ou o nutricionista do domínio edita/exclui.

## Acompanhamento diário

- Registro diário com creatina (sim/não) e cardio (minutos), um por dia por aluno
  (upsert). A consulta devolve apenas **hoje** (404 se não houver registro).
- Configurações do aluno: `showCreatine` e `dailyCardio` controlam o que aparece.
- A primeira consulta sem configuração cria o default e devolve 200; nunca 500.
- Esses dados são privados do aluno; profissionais não têm acesso.

## App Android (MVP)

- Telas: início, treinos, agenda semanal, alimentação e perfil, todas somente leitura.
- Não há botões ou rotas de criar, editar ou excluir treino, agenda ou refeição, nem
  formulários de exercício/alimento.
- Perfil continua permitindo nome, tema, política de privacidade, troca de senha e sair.
- Profissionais usam o web para gestão. Se entrarem no app, veem apenas os próprios
  dados, sem superfície de gestão.
- Convites, aceite/recusa e encerramento de vínculo são feitos no web; o app não gerencia
  vínculos.
- A remoção do CRUD do app só pode acontecer depois que o web tiver paridade de agenda,
  edição e exclusão, para não perder capacidade de gestão.

## Contrato do backend (implementado em 2026-11-16)

Rotas expostas pelo gateway em `/fitness/**` (Swagger em `/fitness/swagger/index.html`).
`student_id` é opcional nas rotas de plano: ausente = o próprio usuário do token.

- `POST /fitness/students` — convite. Body `{email, domain, goal}`. Exige role do
  domínio (`HEALTH_PERSONAL`/`HEALTH_NUTRITIONIST`). `409` para convite pendente
  duplicado ou aluno já vinculado no domínio; `400` para e-mail/domínio inválido ou a
  própria conta.
- `GET /fitness/students?domain=` — profissional: `students[]` (link_id, student_id,
  name, email, goal, domain, since) e `invites[]` pendentes.
- `GET /fitness/students/{link_id}` — cartão do vínculo (próprio profissional ou aluno).
- `PATCH /fitness/students/links/{link_id}` — profissional dono edita `{goal}`.
- `DELETE /fitness/students/links/{link_id}` — qualquer lado encerra o vínculo ativo.
- `GET /fitness/students/invites` — profissional: convites enviados; aluno: recebidos
  (com `professional_name`).
- `POST /fitness/students/invites/{id}/resend` — pendente ou expirado (novo token/prazo).
- `DELETE /fitness/students/invites/{id}` — revoga um convite pendente.
- `POST /fitness/students/invites/accept` e `/decline` — body `{token}`. O aceite exige
  conta ativa com `HEALTH_USER` e o e-mail convidado; aluno já vinculado no domínio
  recebe `409`.
- Convite por e-mail via RabbitMQ (`FITNESS_INVITE`); o e-mail pode não ter conta ainda
  (o `student_id` é preenchido só quando a conta existe ou no aceite). Prazo padrão de
  7 dias (`FITNESS_INVITE_TTL_HOURS`), com expiração automática.
- Plano por aluno: `student_id`/`author_id`; escrita do aluno bloqueada enquanto houver
  profissional ativo no domínio; `403` para papel/vínculo inválido e `404` para aluno
  inexistente/não vinculado.
- `GET/PUT /fitness/workouts/week` devolvem os 7 dias (`workoutId` nulo = descanso);
  listas vazias voltam como `[]`; refeição expõe `food_id` e `food_name` (o legado
  `tag_id` carrega o mesmo id); ids inexistentes respondem `404`; `GET /fitness/config/user`
  cria o default no primeiro acesso.

## Pendências de implementação

O backend acima está pronto. Falta nos clientes:

- Web: telas de convite (e-mail + domínio + objetivo), aceite/recusa pelo link
  `?invite=`, lista de alunos/convites, edição do objetivo e encerramento; consumir
  `student_id` (remover `user_id`), `GET /workouts/week` e `food_id`; exibir o link de
  Swagger no admin.
- Gate de somente leitura por domínio no web e remoção do CRUD no app.
- Hábitos: fora do MVP atual (treinos, dieta e profissionais). A tela web deve ser
  removida/ocultada até o produto priorizar e existir contrato.
- Cálculo de calorias/consistência no dashboard (hoje é derivado dos dados carregados).

Enquanto os clientes não forem atualizados, vale o comportamento atual deles; o alvo
acima é o que deve orientar as mudanças.

