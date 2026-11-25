<script setup lang="ts">
import { useStudentContext } from '~/features/students/useStudentContext'
import { useWeekPlan } from '~/features/workouts/useWeekPlan'
import { WEEK_DAYS } from '~/features/workouts/week'

const { studentId, studentName, canManageWorkouts, managerName, loadContext } = useStudentContext()
const {
  days,
  workouts,
  loading,
  saving,
  error,
  actionError,
  feedback,
  readOnly,
  plannedCount,
  load,
  assign,
  save
} = useWeekPlan(studentId, canManageWorkouts)

function selected(day: number) {
  return days.value.find(item => item.dayOfWeek === day)?.workoutId ?? ''
}

function change(day: number, event: Event) {
  const value = (event.target as HTMLSelectElement).value
  assign(day, value ? Number(value) : null)
}

function dayName(day: number) {
  return days.value.find(item => item.dayOfWeek === day)?.name || ''
}

onMounted(async () => {
  await Promise.all([loadContext(), load()])
})
definePageMeta({ key: route => route.fullPath })
useSeoMeta({ title: 'Agenda' })
</script>

<template>
  <div class="app-shell">
    <AppSidebar active="agenda" />
    <main class="app-main module-page">
      <header class="app-topbar">
        <div><p>SUA ROTINA</p><h1>Agenda semanal</h1></div>
        <div class="topbar-actions">
          <NuxtLink
            :to="{ path: '/workouts', query: $route.query }"
            class="secondary-button"
          >
            <UIcon name="i-lucide-dumbbell" /> Treinos
          </NuxtLink>
          <button
            class="round-button"
            aria-label="Atualizar agenda"
            :disabled="loading"
            @click="load"
          >
            <UIcon name="i-lucide-refresh-cw" />
          </button>
        </div>
      </header>
      <StudentSwitcher />
      <p
        v-if="studentName"
        class="student-context"
      >
        <UIcon name="i-lucide-user-round" />
        <template v-if="readOnly">
          Você está consultando a agenda de <b>{{ studentName }}</b>. Apenas o personal trainer do vínculo pode editar.
        </template>
        <template v-else>
          Você está gerenciando a agenda de <b>{{ studentName }}</b>.
        </template>
        <NuxtLink to="/agenda">Voltar para minha agenda</NuxtLink>
      </p>
      <p
        v-if="!studentName && readOnly && managerName('PERSONAL')"
        class="notice-banner"
      >
        <UIcon name="i-lucide-lock" /> Sua agenda é gerenciada por <b>{{ managerName('PERSONAL') }}</b>.
      </p>
      <section class="module-intro workout-intro">
        <div>
          <p class="eyebrow">
            <span /> uma semana por vez
          </p><h2>Distribua seus treinos</h2><p>Cada dia recebe no máximo um treino. Os dias sem treino ficam como descanso.</p>
        </div><UIcon name="i-lucide-calendar-days" />
      </section>

      <div
        v-if="loading"
        class="page-state"
      >
        <UIcon
          name="i-lucide-loader-circle"
          class="spin"
        /><p>Carregando agenda...</p>
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
      <template v-else>
        <section class="agenda-card">
          <div class="section-heading">
            <div>
              <p>SEMANA ATUAL</p>
              <h2>{{ plannedCount }} de 7 dias com treino</h2>
            </div>
          </div>
          <div class="agenda-list">
            <article
              v-for="day in WEEK_DAYS"
              :key="day.day"
              class="agenda-row"
              :class="{ rest: !selected(day.day) }"
            >
              <span class="agenda-day">{{ day.label }}</span>
              <template v-if="readOnly">
                <span class="agenda-workout">{{ dayName(day.day) || 'Descanso' }}</span>
              </template>
              <template v-else>
                <select
                  :value="selected(day.day)"
                  :aria-label="`Treino de ${day.label}`"
                  @change="change(day.day, $event)"
                >
                  <option value="">
                    Descanso
                  </option>
                  <option
                    v-for="workout in workouts"
                    :key="workout.id"
                    :value="workout.id"
                  >
                    {{ workout.name }}
                  </option>
                </select>
              </template>
            </article>
          </div>
          <p
            v-if="!workouts.length"
            class="agenda-hint"
          >
            Nenhum treino disponível ainda. Edite os treinos para poder distribuí-los na semana.
          </p>
        </section>

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
        <div
          v-if="!readOnly"
          class="form-actions"
        >
          <button
            class="primary-button"
            :disabled="saving"
            @click="save"
          >
            {{ saving ? 'Salvando...' : 'Salvar agenda' }}
          </button>
        </div>
      </template>
    </main>
  </div>
</template>
