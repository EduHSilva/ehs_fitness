# Regras de negócio — EHS Fitness

> Plataforma (auth, roles, isolamento): `D:/Projetos/ehs_microservices/regras-negocio.md`.
> O serviço `fitness` é um app Go; o login e as roles vêm do `users`.
> Pendências de implementação: `TODO.md` (clientes) e `D:/Projetos/ehs_microservices/TODO.md`.

## Marca

- Nome exibido na web e no app: **Fitness**. Cor de marca indigo (`#4F46E5`).
- Web (sidebar, landing, login) e app (drawer, login, perfil) exibem "Desenvolvido por
  EHS Solutions".

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
| Ver config e acompanhamento diário | Sim | Sim | Próprio + alunos (registros somente leitura) | Próprio + alunos (registros somente leitura) |

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
- Um profissional não pode convidar a própria conta, nem uma conta que já tenha role de
  profissional (`409 inviteProfessional`): quem gerencia não é convidado como aluno.
- O aluno consulta os próprios vínculos ativos em `GET /fitness/students/links` para saber
  quem gerencia cada domínio; a consulta exige `HEALTH_USER` (403 caso contrário).

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
- O catálogo base de exercícios é curado e global; o catálogo de alimentos é global.
- Cada usuário pode criar e editar exercícios na própria biblioteca, visíveis apenas
  para ele (`POST`/`PUT /fitness/workout/exercises`). O profissional usa a própria
  biblioteca ao montar planos dos alunos; a base curada não é editável pelos clientes.
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
- A ordem dos exercícios no treino é definida no cadastro (`position`) e preservada na
  leitura; o app executa os exercícios nessa ordem.
- O descanso entre séries é definido por exercício, em segundos (`rest_seconds`), no
  cadastro do treino no web; o app usa esse valor no cronômetro de execução.
- Cardio é prescrito por tempo em `duration_seconds` (0 = exercício de séries e
  repetições). No cadastro web o cardio usa duração em **minutos** e assume 1 série/0
  repetições; a execução no app mostra uma etapa única por tempo em vez de séries.
- Não há status/ciclo de vida do treino. Padrão da interface: 3 séries × 12 repetições.
- Só o aluno autorizado ou o profissional do domínio edita/exclui.
- Durante a execução no app, a carga do exercício atual pode ser ajustada e gravada no
  plano (`PUT /fitness/workout/load`, `workout_id` + `exercise_id` + `load`) quando houver
  conexão. A regra de escrita do plano vale: aluno com profissional ativo recebe `403` e o
  ajuste fica só na execução local.

## Dieta

- Refeição: nome (único por aluno), horário e pelo menos um alimento.
- Alimento: referência ao catálogo (valores por 100 g), quantidade e observação. O campo
  correto é `observation` (não `obs`). O catálogo global é a TACO (597 itens).
- A resposta de refeição expõe `food_id`, `food_name` e os macros por 100 g
  (`calories`, `protein`, `carbs`, `fat`, `fiber`) para exibição; os clientes escalam
  pelos gramas e podem somar o total da refeição.
- O backend não calcula totais nem metas de macros. A busca de alimentos ignora acento
  e caixa e combina as palavras do nome (limite 30); a UI exige 2 caracteres.
- Só o aluno autorizado ou o nutricionista do domínio edita/exclui.

## Acompanhamento diário

- Registro diário com creatina (sim/não), treino (sim/não) e cardio (minutos), um por dia
  por aluno (upsert). Sem parâmetros, a consulta devolve apenas **hoje** (404 se não
  houver registro); com `from`/`to` (YYYY-MM-DD, inclusive, máximo 366 dias) devolve a
  lista ordenada por data.
- O treino diário registra apenas que o aluno treinou naquele dia; quando existir o
  registro real da execução, esse campo pode ser derivado dela.
- Configurações do aluno: `showCreatine`, `showWorkout` e `dailyCardio` (minutos; 0
  desativa) controlam o que aparece no app. Qualquer profissional com vínculo ativo
  (personal **ou** nutricionista) lê e ajusta a configuração do aluno via `student_id`.
- O histórico diário é escrito apenas pelo próprio aluno; o profissional com vínculo
  ativo lê os registros do aluno em modo somente leitura (`student_id` em
  `GET /config/history`).
- A primeira consulta sem configuração cria o default e devolve 200; nunca 500.
- O web expõe `/tracking` (Acompanhamento) para definir as opções do próprio usuário ou
  de um aluno vinculado; o app mostra o cartão de acompanhamento no início somente
  quando ao menos uma opção está ativa.
- O web expõe `/history` (Registros) com calendário mensal, filtro por tipo e estatísticas
  básicas do mês. A tela é somente leitura para o aluno e para o profissional vinculado,
  e mostra apenas os tipos ativos no acompanhamento.
- O app também expõe a tela Registros (calendário mensal, filtros e estatísticas); cada
  aluno vê apenas os próprios registros.

## App Android (MVP)

- Telas de consulta: início, treinos, agenda semanal, alimentação, registros e perfil;
  além delas, a tela de execução do treino.
- Não há botões ou rotas de criar, editar ou excluir treino, agenda ou refeição, nem
  formulários de exercício/alimento.
- Perfil continua permitindo nome, tema, política de privacidade, troca de senha e sair.
- O app permite **executar** um treino: mostra o exercício e a série atuais e inicia o
  cronômetro de descanso ao concluir a série. Cardio aparece como uma etapa única por
  tempo (`duration_seconds`). O progresso é local (SharedPreferences), não vai para a
  API e é retomado depois de sair da tela ou encerrar o app; "Encerrar treino" descarta
  a execução. A exceção é a carga do exercício atual, que o aluno pode ajustar na
  execução e que é gravada no plano quando online (ver Treinos).
- O início exibe o cartão de acompanhamento diário quando a configuração definida no web
  ativa creatina, treino e/ou cardio: o aluno marca a creatina e o treino do dia e
  registra os minutos de cardio (upsert em `/fitness/config/history`). O cartão leva à
  tela Registros, que lista o mês corrente com calendário e estatísticas.
- Profissionais usam o web para gestão. Se entrarem no app, veem apenas os próprios
  dados, sem superfície de gestão. Contas com `HEALTH_USER` + role de profissional
  continuam sendo alunos no app.
- O app usa `GET /fitness/students/links` para trocar o aviso "acesse a versão web" por
  "Gerenciado por <profissional>" no domínio com vínculo ativo (personal → treinos e
  agenda; nutricionista → dieta). Sem vínculo, o aviso continua sendo a referência ao web.
- Convites, aceite/recusa e encerramento de vínculo são feitos no web; o app não gerencia
  vínculos.
- A remoção do CRUD do app só pode acontecer depois que o web tiver paridade de agenda,
  edição e exclusão, para não perder capacidade de gestão.

## Contrato do backend (atualizado em 2026-11-18)

Rotas expostas pelo gateway em `/fitness/**` (Swagger em `/fitness/swagger/index.html`).
`student_id` é opcional nas rotas de plano: ausente = o próprio usuário do token.

- `POST /fitness/students` — convite. Body `{email, domain, goal}`. Exige role do
  domínio (`HEALTH_PERSONAL`/`HEALTH_NUTRITIONIST`). `409` para convite pendente
  duplicado, aluno já vinculado no domínio ou conta com role de profissional; `400` para
  e-mail/domínio inválido ou a própria conta.
- `GET /fitness/students?domain=` — profissional: `students[]` (link_id, student_id,
  name, email, goal, domain, since) e `invites[]` pendentes.
- `GET /fitness/students/links` — aluno: vínculos ativos vistos do lado do aluno
  (`link_id`, `domain`, `professional_id`, `professional_name`, `goal`, `since`);
  `403` para conta sem `HEALTH_USER`, lista vazia sem vínculos. É o que permite ao web
  esconder o CRUD antes do 403 e ao app indicar quem gerencia o plano.
- `GET /fitness/students/{link_id}` — cartão do vínculo (próprio profissional ou aluno).
- `PATCH /fitness/students/links/{link_id}` — profissional dono edita `{goal}`.
- `DELETE /fitness/students/links/{link_id}` — qualquer lado encerra o vínculo ativo.
- `GET /fitness/students/invites?direction=sent|received` — `direction` explícito
  resolve a precedência de papéis (conta `HEALTH_PERSONAL` + `HEALTH_USER` vê as duas
  listas); sem o parâmetro, profissional recebe os enviados e aluno os recebidos.
  `direction` desconhecido responde `400`. Recebidos trazem `professional_name`.
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
- `duration_seconds` no exercício do treino (cardio por tempo; 0 = séries/repetições).
- `PUT /fitness/workout/load` — body `{workout_id, exercise_id, load, student_id?}`;
  grava a carga de um exercício do treino (0 = sem carga) respeitando a mesma regra de
  escrita do plano (`400` carga/ids inválidos, `404` treino ou exercício inexistente,
  `403` aluno com profissional ativo).
- `GET/PUT /fitness/workouts/week` devolvem os 7 dias (`workoutId` nulo = descanso);
  listas vazias voltam como `[]`; refeição expõe `food_id` e `food_name` (o legado
  `tag_id` carrega o mesmo id); ids inexistentes respondem `404`; `GET /fitness/config/user`
  cria o default no primeiro acesso e aceita `student_id` para qualquer profissional com
  vínculo ativo; `/fitness/config/history` carrega `creatine`, `trained` e `cardio`
  (404 sem registro no dia, sem parâmetros); com `from`/`to` devolve a lista do intervalo
  e aceita `student_id` para leitura pelo profissional vinculado.
- CORS do serviço aceita `PATCH` (além de GET/POST/PUT/DELETE), para chamadas diretas do
  navegador a `PATCH /fitness/students/links/:id`.
- Swagger (`fitness/docs`) regenerado em 2026-11-18: inclui `duration_seconds`,
  `student_id` na configuração, `GET /students/links` e `direction` nos convites.

## Pendências de implementação

Os clientes já seguem o contrato acima (web como superfície de gestão, app somente
leitura com "Gerenciado por"). Continuam abertos:

- Hábitos: fora do MVP atual (treinos, dieta e profissionais). A tela web deve ser
  removida/ocultada até o produto priorizar e existir contrato.
- Cálculo de calorias/consistência no dashboard da home (a dieta mostra o total do dia;
  a home ainda deriva o resumo dos dados carregados).

