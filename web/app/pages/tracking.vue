<script setup lang="ts">
import { useStudentContext } from '~/features/students/useStudentContext'
import { useTracking } from '~/features/tracking/useTracking'

const { studentId, studentName, canManage, loadContext } = useStudentContext()
const {
  loading,
  saving,
  error,
  actionError,
  feedback,
  showCreatine,
  dailyCardio,
  readOnly,
  cardioEnabled,
  load,
  save
} = useTracking(studentId, canManage)

onMounted(async () => {
  await Promise.all([loadContext(), load()])
})

useSeoMeta({ title: 'Acompanhamento' })
</script>

<template>
  <div class="app-shell">
    <AppSidebar active="tracking" />
    <main class="app-main module-page">
      <header class="app-topbar">
        <div>
          <p>REGISTRO DIÁRIO</p>
          <h1>Acompanhamento</h1>
        </div>
        <div class="topbar-actions">
          <button
            class="round-button"
            aria-label="Atualizar acompanhamento"
            :disabled="loading"
            @click="load"
          >
            <UIcon
              name="i-lucide-refresh-cw"
              :class="{ spin: loading }"
            />
          </button>
        </div>
      </header>

      <p
        v-if="studentName"
        class="student-context"
      >
        <UIcon name="i-lucide-user-round" />
        <template v-if="readOnly">
          Você está consultando o acompanhamento de <b>{{ studentName }}</b>.
        </template>
        <template v-else>
          Você está definindo o acompanhamento diário de <b>{{ studentName }}</b>.
        </template>
        <NuxtLink to="/tracking">Voltar para meu acompanhamento</NuxtLink>
      </p>

      <section class="module-intro workout-intro">
        <div>
          <p class="eyebrow">
            <span /> creatina e cardio
          </p>
          <h2>O que acompanhar todos os dias</h2>
          <p>As opções ativas aparecem no app do aluno para o registro diário.</p>
        </div>
        <UIcon name="i-lucide-heart-pulse" />
      </section>

      <div
        v-if="loading"
        class="page-state"
      >
        <UIcon
          name="i-lucide-loader-circle"
          class="spin"
        /><p>Carregando acompanhamento...</p>
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
        v-else
        class="registration-card"
      >
        <div class="registration-heading">
          <div>
            <p class="eyebrow">
              <span /> solicitações
            </p>
            <h2>Registros solicitados</h2>
          </div>
        </div>
        <p class="skills-hint">
          Ative o que o aluno deve registrar. O personal trainer e o nutricionista podem ajustar
          esse acompanhamento de um aluno vinculado.
        </p>

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

        <div class="tracking-options">
          <label class="tracking-option">
            <input
              v-model="showCreatine"
              type="checkbox"
              :disabled="readOnly || saving"
            >
            <span>
              <b>Creatina</b>
              <small>Pedir o registro diário de creatina.</small>
            </span>
          </label>
          <label class="tracking-option">
            <input
              v-model="cardioEnabled"
              type="checkbox"
              :disabled="readOnly || saving"
            >
            <span>
              <b>Cardio diário</b>
              <small>Pedir o registro diário de cardio em minutos.</small>
            </span>
          </label>
          <label
            v-if="cardioEnabled"
            class="tracking-cardio-goal"
          >
            Meta diária de cardio (min)<input
              v-model.number="dailyCardio"
              type="number"
              min="1"
              max="600"
              step="1"
              :disabled="readOnly || saving"
            >
          </label>
        </div>

        <div class="form-actions">
          <button
            class="primary-button"
            :disabled="readOnly || saving"
            @click="save"
          >
            {{ saving ? 'Salvando...' : 'Salvar acompanhamento' }}
          </button>
        </div>
      </section>
    </main>
  </div>
</template>
