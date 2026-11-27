<script setup lang="ts">
import { useStudentContext } from '~/features/students/useStudentContext'
import { useTrackingHistory } from '~/features/tracking/useTrackingHistory'
import {
  TYPE_ICONS,
  TYPE_LABELS,
  WEEKDAY_LABELS,
  dayTypes,
  formatMinutes,
  type CalendarCell
} from '~/features/tracking/history'

const route = useRoute()
const { studentId, studentName, loadContext } = useStudentContext()
const {
  loading,
  refreshing,
  monthLoading,
  error,
  config,
  filter,
  types,
  filters,
  stats,
  grid,
  label,
  hasRecords,
  hasTracking,
  load,
  refresh,
  changeMonth,
  goToday,
  changeFilter,
  recordFor
} = useTrackingHistory(studentId)

const trackingLink = computed(() => ({ path: '/tracking', query: route.query }))

function typesFor(date: string) {
  return dayTypes(recordFor(date), config.value)
    .filter(type => filter.value === 'all' || type === filter.value)
}

/** Minutos de cardio visíveis no dia, respeitando o filtro atual. */
function cardioMinutes(date: string) {
  return typesFor(date).includes('cardio') ? recordFor(date)?.cardio ?? 0 : 0
}

function cellLabel(cell: NonNullable<CalendarCell>) {
  const names = typesFor(cell.date).map(type => TYPE_LABELS[type])
  const cardio = cardioMinutes(cell.date)
  const detail = cardio > 0 ? `, ${formatMinutes(cardio)} de cardio` : ''
  return `${cell.day} de ${label.value}: ${names.length ? names.join(', ') : 'sem registro'}${detail}`
}

onMounted(async () => {
  await Promise.all([loadContext(), load()])
})
definePageMeta({ key: route => route.fullPath })
useSeoMeta({ title: 'Registros' })
</script>

<template>
  <div class="app-shell">
    <AppSidebar active="history" />
    <main class="app-main module-page">
      <header class="app-topbar">
        <div><p>HISTÓRICO DIÁRIO</p><h1>Registros</h1></div>
        <div class="topbar-actions">
          <button
            class="round-button"
            aria-label="Atualizar registros"
            :disabled="loading || refreshing || monthLoading"
            @click="refresh"
          >
            <UIcon
              name="i-lucide-refresh-cw"
              :class="{ spin: refreshing }"
            />
          </button>
        </div>
      </header>

      <StudentSwitcher />
      <p
        v-if="studentName"
        class="student-context"
      >
        <UIcon name="i-lucide-user-round" />
        Você está consultando os registros de <b>{{ studentName }}</b> em modo somente leitura.
        <NuxtLink to="/history">Voltar para meus registros</NuxtLink>
      </p>

      <div
        v-if="loading"
        class="page-state"
      >
        <UIcon
          name="i-lucide-loader-circle"
          class="spin"
        /><p>Carregando registros...</p>
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
      <div
        v-else-if="!hasTracking"
        class="page-state"
      >
        <UIcon name="i-lucide-heart-pulse" /><h2>Acompanhamento desativado</h2>
        <p>Ative creatina, treino ou cardio no acompanhamento para começar a registrar.</p>
        <NuxtLink
          :to="trackingLink"
          class="primary-button"
        >
          Configurar acompanhamento
        </NuxtLink>
      </div>
      <template v-else>
        <section class="history-toolbar">
          <div class="history-month-nav">
            <button
              class="round-button"
              aria-label="Mês anterior"
              @click="changeMonth(-1)"
            >
              <UIcon name="i-lucide-chevron-left" />
            </button>
            <strong>{{ label }}</strong>
            <button
              class="round-button"
              aria-label="Próximo mês"
              @click="changeMonth(1)"
            >
              <UIcon name="i-lucide-chevron-right" />
            </button>
            <button
              class="icon-text-button"
              type="button"
              @click="goToday"
            >
              Hoje
            </button>
          </div>
          <div
            class="history-filters"
            role="group"
            aria-label="Filtrar registros por tipo"
          >
            <button
              v-for="item in filters"
              :key="item.value"
              type="button"
              class="history-filter"
              :class="{ active: filter === item.value }"
              :aria-pressed="filter === item.value"
              @click="changeFilter(item.value)"
            >
              <UIcon :name="item.icon" /> {{ item.label }}
            </button>
          </div>
        </section>

        <section class="history-stats">
          <article class="history-stat">
            <span class="metric-icon blue"><UIcon name="i-lucide-calendar-check" /></span>
            <strong>{{ stats.registeredDays }}</strong>
            <small>dias com registro</small>
          </article>
          <article
            v-if="types.includes('creatine')"
            class="history-stat"
          >
            <span class="metric-icon orange"><UIcon :name="TYPE_ICONS.creatine" /></span>
            <strong>{{ stats.creatineDays }}</strong>
            <small>dias de creatina</small>
          </article>
          <article
            v-if="types.includes('workout')"
            class="history-stat"
          >
            <span class="metric-icon blue"><UIcon :name="TYPE_ICONS.workout" /></span>
            <strong>{{ stats.workoutDays }}</strong>
            <small>dias de treino</small>
          </article>
          <article
            v-if="types.includes('cardio')"
            class="history-stat"
          >
            <span class="metric-icon teal"><UIcon :name="TYPE_ICONS.cardio" /></span>
            <strong>{{ formatMinutes(stats.cardioMinutes) }}</strong>
            <small>
              {{ stats.cardioDays }} {{ stats.cardioDays === 1 ? 'dia' : 'dias' }} de cardio
              <template v-if="stats.cardioDays">
                · média {{ formatMinutes(stats.cardioAverage) }}/dia
              </template>
            </small>
          </article>
        </section>

        <section class="history-card">
          <div
            class="history-weekdays"
            aria-hidden="true"
          >
            <span
              v-for="(weekday, index) in WEEKDAY_LABELS"
              :key="index"
            >{{ weekday }}</span>
          </div>
          <div
            class="history-grid"
            role="grid"
            :aria-label="label"
            :aria-busy="monthLoading"
          >
            <div
              v-for="(cell, index) in grid"
              :key="index"
              class="history-day"
              :class="{ empty: !cell, today: cell?.today }"
              :role="cell ? 'gridcell' : undefined"
              :aria-label="cell ? cellLabel(cell) : undefined"
            >
              <template v-if="cell">
                <span class="history-day-index">{{ cell.day }}</span>
                <div
                  v-if="typesFor(cell.date).length"
                  class="history-day-icons"
                >
                  <span
                    v-for="type in typesFor(cell.date)"
                    :key="type"
                    :class="type"
                    :title="TYPE_LABELS[type]"
                  >
                    <UIcon :name="TYPE_ICONS[type]" />
                  </span>
                </div>
                <span
                  v-if="cardioMinutes(cell.date) > 0"
                  class="history-day-cardio"
                >{{ formatMinutes(cardioMinutes(cell.date)) }}</span>
              </template>
            </div>
          </div>
          <div
            v-if="types.length"
            class="history-legend"
          >
            <span
              v-for="type in types"
              :key="type"
            ><i :class="type"><UIcon :name="TYPE_ICONS[type]" /></i>{{ TYPE_LABELS[type] }}</span>
          </div>
          <p
            v-if="monthLoading"
            class="history-empty"
          >
            <UIcon
              name="i-lucide-loader-circle"
              class="spin"
            /> Carregando {{ label }}...
          </p>
          <p
            v-else-if="!hasRecords"
            class="history-empty"
          >
            <UIcon name="i-lucide-calendar-x" /> Nenhum registro em {{ label }}.
          </p>
        </section>
      </template>
    </main>
  </div>
</template>
