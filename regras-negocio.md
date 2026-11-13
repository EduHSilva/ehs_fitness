# Regras de negócio — EHS Fitness

> Plataforma (auth, roles, isolamento): `D:/Projetos/ehs_microservices/regras-negocio.md`.
> O serviço `fitness` é um app Go; o login e as roles vêm do `users`.

## Acesso e papéis

- Roles: `HEALTH_USER` (aluno/usuário), `HEALTH_PERSONAL` (personal trainer) e
  `HEALTH_NUTRITIONIST` (nutricionista). Todas entram com `app: FITNESS`.
- Hoje as três roles têm **o mesmo poder na API**: não existe checagem de role no serviço
  `fitness`. A distinção é apenas de perfil/empresa.
- `HEALTH_PERSONAL` e `HEALTH_NUTRITIONIST` ganham empresa automática no cadastro
  ("<nome> personal" / "<nome> nutricionista").

## Dados do usuário

- Todo dado (treino, refeição, config, histórico) pertence ao usuário do token; não há
  compartilhamento entre contas. Id de outro usuário responde 404.
- Catálogos de exercícios e alimentos são globais (não pertencem ao usuário).

## Treinos

- Um treino tem nome e dia da semana (1–7). Só pode existir **um treino por dia** para
  o mesmo usuário; dia 0 significa "não agendado".
- A gravação da semana é feita em bloco: envia os 7 dias, cada treino em no máximo um dia,
  todos do próprio usuário. Isso substitui a semana inteira.
- Nome é único por usuário; cada treino precisa de pelo menos um exercício.
- Exercício: referência ao catálogo, séries, repetições, carga, descanso e observações.
  Não há status/ciclo de vida do treino. Padrão da interface: 3 séries × 12 repetições.

## Dieta

- Refeição: nome (único por usuário), horário e pelo menos um alimento.
- Alimento: referência ao catálogo (valores por 100 g), quantidade e observação.
- O backend não calcula totais nem metas de macros; a busca de alimentos é por trecho do
  nome (case-insensitive, limite 30) e a UI exige 2 caracteres.
- Só o dono edita/exclui refeições.

## Acompanhamento diário

- Existe um registro diário com creatina (sim/não) e cardio (minutos), um por dia por
  usuário (upsert). A consulta devolve apenas **hoje** (404 se não houver registro).
- Configurações do usuário: `showCreatine` e `dailyCardio` controlam o que aparece.

## Não implementado (não usar como regra)

- Vínculo aluno ↔ profissional, cadastro/gestão de alunos e compartilhamento de treinos.
- Hábitos (a tela existe no front, mas não há API).
- Cálculo de calorias/consistência no dashboard (hoje é conteúdo fixo na tela).
