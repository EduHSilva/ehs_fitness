<script setup lang="ts">
import { useStudents } from '~/features/students/useStudents'
import { domainLabel, domainShortLabel } from '~/features/students/types'
import type { StudentCard, StudentDomain } from '~/features/students/types'

const {
  students,
  invites,
  loading,
  error,
  actionError,
  saving,
  busyLinkId,
  busyInviteId,
  feedback,
  isProfessional,
  availableDomains,
  canManage,
  load,
  invite,
  resend,
  revoke,
  saveGoal,
  endLink,
  manage
} = useStudents()

const email = ref('')
const goal = ref('')
const domain = ref<StudentDomain>(availableDomains.value[0] ?? 'PERSONAL')
const formError = ref('')
const editingLinkId = ref<number | null>(null)
const goalDraft = ref('')
const pendingEnd = ref<StudentCard | null>(null)

const endOpen = computed({
  get: () => Boolean(pendingEnd.value),
  set: (open: boolean) => { if (!open) pendingEnd.value = null }
})

onMounted(load)

async function submitInvite() {
  formError.value = ''
  if (!email.value.trim()) {
    formError.value = 'Informe o e-mail do aluno.'
    return
  }
  if (goal.value.trim().length > 255) {
    formError.value = 'O objetivo deve ter no máximo 255 caracteres.'
    return
  }
  const sent = await invite({ email: email.value.trim(), domain: domain.value, goal: goal.value.trim() })
  if (sent) {
    email.value = ''
    goal.value = ''
  }
}

function startGoal(student: StudentCard) {
  editingLinkId.value = student.link_id
  goalDraft.value = student.goal
}

function cancelGoal() {
  editingLinkId.value = null
  goalDraft.value = ''
}

async function persistGoal(student: StudentCard) {
  await saveGoal(student.link_id, goalDraft.value.trim())
  cancelGoal()
}

function requestEnd(student: StudentCard) {
  pendingEnd.value = student
}

async function confirmEnd() {
  const student = pendingEnd.value
  if (!student) return
  await endLink(student.link_id)
  pendingEnd.value = null
}

function expiryLabel(value: string) {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString('pt-BR')
}

useSeoMeta({ title: 'Alunos' })
</script>

<template>
  <div class="app-shell">
    <UModal
      v-model:open="endOpen"
      title="Encerrar vínculo"
      :description="pendingEnd ? `Encerrar o acompanhamento de ${pendingEnd.name}? O aluno mantém o plano e volta a editar sozinho.` : ''"
      :dismissible="busyLinkId === null"
    >
      <template #footer>
        <div class="form-actions">
          <button
            class="secondary-button"
            :disabled="busyLinkId !== null"
            @click="pendingEnd = null"
          >
            Cancelar
          </button>
          <button
            class="primary-button"
            :disabled="busyLinkId !== null"
            @click="confirmEnd"
          >
            {{ busyLinkId !== null ? 'Encerrando...' : 'Encerrar vínculo' }}
          </button>
        </div>
      </template>
    </UModal>
    <AppSidebar active="students" />
    <main class="app-main module-page">
      <header class="app-topbar">
        <div><p>ÁREA PROFISSIONAL</p><h1>Alunos</h1></div>
        <div class="topbar-actions">
          <button
            class="round-button"
            aria-label="Atualizar alunos"
            :disabled="loading"
            @click="load"
          >
            <UIcon name="i-lucide-refresh-cw" />
          </button>
        </div>
      </header>

      <section
        v-if="!isProfessional"
        class="page-state"
      >
        <UIcon name="i-lucide-lock" /><h2>Área para contas profissionais</h2><p>Cadastre-se como personal trainer ou nutricionista para administrar alunos.</p>
      </section>

      <template v-else>
        <p
          v-if="feedback"
          class="notice-banner"
          role="status"
        >
          <UIcon name="i-lucide-circle-check" /> {{ feedback }}
        </p>
        <p
          v-if="actionError"
          class="form-error form-error-left"
          role="alert"
        >
          {{ actionError }}
        </p>

        <section class="registration-card">
          <div class="registration-heading">
            <div>
              <p class="eyebrow">
                <span /> convite por e-mail
              </p><h2>Convidar aluno</h2>
            </div>
          </div>
          <p class="skills-hint">
            O aluno recebe um link por e-mail e só vê o acompanhamento depois de aceitar. Você define a área:
            o personal cuida de treinos e agenda; o nutricionista cuida da dieta.
          </p>
          <form
            class="student-form"
            @submit.prevent="submitInvite"
          >
            <label>E-mail<input
              v-model="email"
              type="email"
              placeholder="aluno@email.com"
            ></label>
            <label>Área<select v-model="domain">
              <option
                v-for="item in availableDomains"
                :key="item"
                :value="item"
              >
                {{ domainLabel(item) }}
              </option>
            </select></label>
            <label>Objetivo<input
              v-model="goal"
              maxlength="255"
              placeholder="Ex.: ganho de massa"
            ></label>
            <button
              class="primary-button"
              type="submit"
              :disabled="saving || !availableDomains.length"
            >
              <UIcon name="i-lucide-send" /> {{ saving ? 'Enviando...' : 'Enviar convite' }}
            </button>
          </form>
          <p
            v-if="formError"
            class="form-error form-error-left"
            role="alert"
          >
            {{ formError }}
          </p>
        </section>

        <section
          v-if="invites.length"
          class="invite-section"
        >
          <div class="section-heading">
            <div>
              <p>AGUARDANDO RESPOSTA</p>
              <h2>Convites pendentes</h2>
            </div>
            <span class="count-badge">{{ invites.length }}</span>
          </div>
          <div class="invite-list">
            <article
              v-for="item in invites"
              :key="item.id"
              class="invite-card"
            >
              <div>
                <h3>{{ item.email }}</h3>
                <p>{{ domainLabel(item.domain) }} · {{ item.goal || 'Sem objetivo definido' }}</p>
                <small>Enviado em {{ expiryLabel(item.created_at) }} · expira em {{ expiryLabel(item.expires_at) }}</small>
              </div>
              <div class="student-actions">
                <button
                  :disabled="busyInviteId !== null"
                  @click="resend(item.id)"
                >
                  <UIcon name="i-lucide-send" /> Reenviar
                </button>
                <button
                  :disabled="busyInviteId !== null"
                  @click="revoke(item.id)"
                >
                  <UIcon name="i-lucide-x" /> Revogar
                </button>
              </div>
            </article>
          </div>
        </section>

        <div
          v-if="loading"
          class="page-state"
        >
          <UIcon
            name="i-lucide-loader-circle"
            class="spin"
          /><p>Carregando alunos...</p>
        </div>
        <div
          v-else-if="error"
          class="page-state error-state"
        >
          <UIcon name="i-lucide-circle-alert" /><p>{{ error }}</p><button
            class="primary-button"
            @click="load"
          >
            Tentar novamente
          </button>
        </div>
        <section
          v-else-if="students.length"
          class="student-grid"
        >
          <article
            v-for="student in students"
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
              <template v-if="editingLinkId === student.link_id">
                <div class="goal-editor">
                  <input
                    v-model="goalDraft"
                    maxlength="255"
                    placeholder="Objetivo do aluno"
                  >
                  <button
                    :disabled="busyLinkId !== null"
                    @click="persistGoal(student)"
                  >
                    Salvar
                  </button>
                  <button @click="cancelGoal">
                    Cancelar
                  </button>
                </div>
              </template>
              <small v-else>{{ student.goal || 'Sem objetivo definido' }}</small>
            </div>
            <div class="student-actions">
              <button
                :title="canManage('PERSONAL') ? 'Gerenciar treinos' : 'Consultar treinos'"
                @click="manage(student, 'workouts')"
              >
                <UIcon name="i-lucide-dumbbell" />
                {{ canManage('PERSONAL') ? 'Treinos' : 'Ver treinos' }}
              </button>
              <button
                :title="canManage('NUTRITIONIST') ? 'Gerenciar dieta' : 'Consultar dieta'"
                @click="manage(student, 'diet')"
              >
                <UIcon name="i-lucide-utensils" />
                {{ canManage('NUTRITIONIST') ? 'Dieta' : 'Ver dieta' }}
              </button>
              <button @click="startGoal(student)">
                <UIcon name="i-lucide-target" /> Objetivo
              </button>
              <button
                class="danger-action"
                :disabled="busyLinkId !== null"
                @click="requestEnd(student)"
              >
                <UIcon name="i-lucide-unlink" /> Encerrar
              </button>
            </div>
            <small class="student-domain-note">{{ domainShortLabel(student.domain) }}</small>
          </article>
        </section>
        <div
          v-else
          class="page-state"
        >
          <UIcon name="i-lucide-users" /><h2>Nenhum aluno vinculado</h2><p>Envie o primeiro convite por e-mail para acompanhar um aluno.</p>
        </div>
      </template>
    </main>
  </div>
</template>
