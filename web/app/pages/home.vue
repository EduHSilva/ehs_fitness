<script setup lang="ts">
import { useHome } from '~/features/home/useHome'
import { useIncomingInvites } from '~/features/students/useIncomingInvites'
import { readStoredInviteToken, storeInviteToken } from '~/features/students/invite'
import { useStudents } from '~/features/students/useStudents'
import { domainLabel } from '~/features/students/types'
import type { StudentCard } from '~/features/students/types'

const router = useRouter()
const route = useRoute()
const { user, isProfessional, domains } = useSession()
const invites = useIncomingInvites()
const inviteToken = ref('')

const {
  students: managedStudents,
  invites: managedInvites,
  loading: adminLoading,
  error: adminError,
  load: loadAdmin,
  manage: manageStudent
} = useStudents()

const {
  loading,
  error,
  today,
  totalWorkouts,
  totalMeals,
  activeDays,
  todayWorkout,
  nextWorkout,
  weekDays,
  consistencyMessage,
  exerciseLabel,
  load
} = useHome()

const firstName = computed<string>(() => (user.value?.name || user.value?.username || 'atleta').split(' ')[0] || 'atleta')
const initials = computed(() => firstName.value.slice(0, 1).toUpperCase())
const activeDaysTrack = computed(() => `${Math.round((activeDays.value / 7) * 100)}%`)

function goTo(path: string) {
  router.push(path)
}

async function acceptInvite() {
  const card = await invites.accept(inviteToken.value)
  if (card) inviteToken.value = ''
}

async function declineInvite() {
  const card = await invites.decline(inviteToken.value)
  if (card) inviteToken.value = ''
}

function openStudent(student: StudentCard) {
  if (!student.student_id) return goTo('/students')
  return manageStudent(student, student.domain === 'PERSONAL' ? 'workouts' : 'diet')
}

onMounted(async () => {
  if (!user.value) return router.replace('/login')
  const queryInvite = typeof route.query.invite === 'string' ? route.query.invite : ''
  storeInviteToken(queryInvite)
  inviteToken.value = queryInvite || readStoredInviteToken()
  if (isProfessional.value) return loadAdmin()
  await load()
})

useSeoMeta({ title: 'Início' })
</script>

<template>
  <div class="app-shell">
    <AppSidebar active="home" />
    <main class="app-main">
      <header class="app-topbar">
        <div>
          <p>{{ isProfessional ? 'ÁREA PROFISSIONAL' : 'SEU PAINEL DE BEM-ESTAR' }}</p>
          <h1>Olá, {{ firstName }}</h1>
        </div>
        <div class="topbar-actions">
          <button
            class="round-button"
            aria-label="Atualizar painel"
            :disabled="loading"
            @click="load"
          >
            <UIcon
              name="i-lucide-refresh-cw"
              :class="{ spin: loading }"
            />
          </button>
          <button
            class="profile-button"
            aria-label="Perfil"
            @click="goTo('/profile')"
          >
            <img
              v-if="user?.photo"
              :src="user.photo"
              alt=""
            ><span v-else>{{ initials }}</span>
          </button>
        </div>
      </header>

      <section
        v-if="inviteToken"
        class="invite-section invite-banner"
      >
        <div>
          <p class="eyebrow">
            <span /> convite recebido
          </p>
          <h2>Um profissional quer acompanhar seu plano</h2>
          <p v-if="invites.feedback.value">
            {{ invites.feedback.value }}
          </p>
          <p v-else-if="invites.error.value">
            {{ invites.error.value }}
          </p>
          <p v-else>
            Aceite para permitir que ele gerencie seu plano. Você pode recusar sem perder seus dados.
          </p>
        </div>
        <div class="form-actions">
          <button
            class="secondary-button"
            :disabled="invites.pending.value"
            @click="declineInvite"
          >
            Recusar
          </button>
          <button
            class="primary-button"
            :disabled="invites.pending.value"
            @click="acceptInvite"
          >
            {{ invites.pending.value ? 'Aguarde...' : 'Aceitar convite' }}
          </button>
        </div>
      </section>

      <template v-if="isProfessional">
        <section class="welcome-card">
          <div>
            <p>SEUS ALUNOS</p>
            <h2>Seus alunos,<br>em um só lugar.</h2>
            <button
              class="light-button"
              @click="goTo('/students')"
            >
              Gerenciar alunos <UIcon name="i-lucide-arrow-right" />
            </button>
          </div>
          <UIcon
            name="i-lucide-users"
            class="welcome-icon"
          />
        </section>

        <section
          class="overview-grid"
          aria-label="Resumo da área profissional"
        >
          <article>
            <span class="metric-icon blue"><UIcon name="i-lucide-users" /></span>
            <p>ALUNOS</p>
            <strong>{{ managedStudents.length }} <small>ativos</small></strong>
          </article>
          <article>
            <span class="metric-icon orange"><UIcon name="i-lucide-mail-plus" /></span>
            <p>CONVITES</p>
            <strong>{{ managedInvites.length }} <small>pendentes</small></strong>
          </article>
          <article>
            <span class="metric-icon teal"><UIcon name="i-lucide-badge-check" /></span>
            <p>ÁREAS</p>
            <strong>{{ domains.length ? domains.map(domainLabel).join(' + ') : '—' }}</strong>
          </article>
        </section>

        <div
          v-if="adminLoading"
          class="page-state"
        >
          <UIcon
            name="i-lucide-loader-circle"
            class="spin"
          />
          <p>Carregando alunos...</p>
        </div>
        <div
          v-else-if="adminError"
          class="page-state error-state"
        >
          <UIcon name="i-lucide-circle-alert" />
          <p>{{ adminError }}</p>
          <button
            class="primary-button"
            @click="loadAdmin"
          >
            Tentar novamente
          </button>
        </div>
        <template v-else>
          <section
            v-if="managedInvites.length"
            class="invite-section"
          >
            <div class="section-heading">
              <div>
                <p>AGUARDANDO RESPOSTA</p>
                <h2>Convites pendentes</h2>
              </div>
              <span class="count-badge">{{ managedInvites.length }}</span>
            </div>
            <div class="invite-list">
              <article
                v-for="invite in managedInvites"
                :key="invite.id"
                class="invite-card"
              >
                <div>
                  <h3>{{ invite.email }}</h3>
                  <p>{{ domainLabel(invite.domain) }} · {{ invite.goal || 'Sem objetivo definido' }}</p>
                </div>
                <div class="student-actions">
                  <button @click="goTo('/students')">
                    <UIcon name="i-lucide-settings-2" /> Gerenciar
                  </button>
                </div>
              </article>
            </div>
          </section>

          <section
            v-if="managedStudents.length"
            class="student-grid"
          >
            <article
              v-for="student in managedStudents"
              :key="student.link_id"
              class="student-card"
            >
              <span class="student-avatar">{{ student.name.slice(0, 1).toUpperCase() }}</span>
              <div>
                <div class="student-title">
                  <h2>{{ student.name }}</h2>
                  <span class="domain-badge">{{ domainLabel(student.domain) }}</span>
                </div>
                <p>{{ student.email }}</p>
                <small>{{ student.goal || 'Sem objetivo definido' }}</small>
              </div>
              <div class="student-actions">
                <button @click="openStudent(student)">
                  <UIcon name="i-lucide-arrow-up-right" /> Abrir plano
                </button>
              </div>
            </article>
          </section>
          <div
            v-else
            class="page-state"
          >
            <UIcon name="i-lucide-users" />
            <h2>Nenhum aluno vinculado</h2>
            <p>Envie o primeiro convite para acompanhar um aluno.</p>
            <button
              class="primary-button"
              @click="goTo('/students')"
            >
              Convidar aluno
            </button>
          </div>
        </template>
      </template>
      <template v-else>
        <div
          v-if="loading"
          class="page-state"
        >
          <UIcon
            name="i-lucide-loader-circle"
            class="spin"
          />
          <p>Carregando seu painel...</p>
        </div>
        <div
          v-else-if="error"
          class="page-state error-state"
        >
          <UIcon name="i-lucide-circle-alert" />
          <p>{{ error }}</p>
          <button
            class="primary-button"
            @click="load"
          >
            Tentar novamente
          </button>
        </div>
        <template v-else>
          <section class="welcome-card">
            <div>
              <p>SEU FOCO DE HOJE</p>
              <h2 v-if="todayWorkout">
                Hoje é dia de<br>{{ todayWorkout.name }}
              </h2>
              <h2 v-else-if="nextWorkout">
                {{ nextWorkout.label }} ·<br>{{ nextWorkout.workout.name }}
              </h2>
              <h2 v-else>
                Sua evolução<br>começa agora.
              </h2>
              <button
                class="light-button"
                @click="goTo('/workouts')"
              >
                {{ todayWorkout ? 'Ver treino de hoje' : 'Abrir treinos' }} <UIcon name="i-lucide-arrow-right" />
              </button>
            </div>
            <UIcon
              name="i-lucide-sparkles"
              class="welcome-icon"
            />
          </section>

          <section
            class="overview-grid"
            aria-label="Resumo da conta"
          >
            <article>
              <span class="metric-icon blue"><UIcon name="i-lucide-dumbbell" /></span>
              <p>TREINOS</p>
              <strong>{{ totalWorkouts }} <small>na biblioteca</small></strong>
            </article>
            <article>
              <span class="metric-icon teal"><UIcon name="i-lucide-utensils" /></span>
              <p>REFEIÇÕES</p>
              <strong>{{ totalMeals }} <small>no plano</small></strong>
            </article>
            <article>
              <span class="metric-icon orange"><UIcon name="i-lucide-flame" /></span>
              <p>DIAS ATIVOS</p>
              <strong>{{ activeDays }} <small>/ 7</small></strong>
              <div class="metric-track orange-track">
                <span :style="{ width: activeDaysTrack }" />
              </div>
            </article>
          </section>

          <section class="content-grid">
            <div>
              <div class="section-heading">
                <div>
                  <p>SEU PLANO</p>
                  <h2>{{ nextWorkout && nextWorkout.day === today ? 'Treino de hoje' : 'Próximo treino' }}</h2>
                </div>
                <button
                  class="link-button"
                  @click="goTo('/workouts')"
                >
                  Ver todos <UIcon name="i-lucide-arrow-right" />
                </button>
              </div>
              <article
                v-if="nextWorkout"
                class="workout-card"
              >
                <div class="workout-image">
                  <UIcon name="i-lucide-dumbbell" />
                </div>
                <div class="workout-info">
                  <span>{{ nextWorkout.label.toUpperCase() }}</span>
                  <h3>{{ nextWorkout.workout.name }}</h3>
                  <p><UIcon name="i-lucide-list-checks" /> {{ exerciseLabel(nextWorkout.workout) }}</p>
                </div>
                <NuxtLink
                  to="/workouts"
                  class="start-button"
                  aria-label="Abrir treinos"
                ><UIcon name="i-lucide-arrow-right" /></NuxtLink>
              </article>
              <div
                v-else
                class="page-state"
              >
                <UIcon name="i-lucide-calendar-days" />
                <h2>Nenhum treino planejado</h2>
                <p>Cadastre um treino e distribua os dias na agenda semanal.</p>
                <button
                  class="primary-button"
                  @click="goTo('/agenda')"
                >
                  Abrir agenda
                </button>
              </div>
            </div>
            <div class="weekly-card">
              <div class="section-heading">
                <div>
                  <p>CONSISTÊNCIA</p>
                  <h2>Meta semanal</h2>
                </div>
                <strong>{{ activeDays }} <small>/ 7</small></strong>
              </div>
              <div
                class="week-days"
                aria-label="Dias da semana com treino"
              >
                <span
                  v-for="day in weekDays"
                  :key="day.day"
                  :class="{ done: Boolean(day.workout), today: day.today }"
                  :title="day.workout ? `${day.short} · ${day.workout}` : 'Descanso'"
                >{{ day.short }}</span>
              </div>
              <p>{{ consistencyMessage }}</p>
            </div>
          </section>
        </template>
      </template>
    </main>
  </div>
</template>
