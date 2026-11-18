<script setup lang="ts">
import { BODY_PART_OPTIONS, bodyPartLabel } from '~/features/workouts/bodyParts'
import { useStudentContext } from '~/features/students/useStudentContext'
import { useWorkouts } from '~/features/workouts/useWorkouts'
import { dayLabel } from '~/features/workouts/week'

const { studentId, studentName, canManage, loadContext } = useStudentContext()
const {
  workouts,
  catalog,
  loading,
  saving,
  error: errorMessage,
  actionError,
  formError,
  showForm,
  editingId,
  expandedWorkoutId,
  workoutName,
  selectedExercises,
  exerciseSearch,
  selectedBodyPart,
  pendingDelete,
  deleting,
  showExerciseForm,
  editingExerciseId,
  exerciseName,
  exerciseBodyPart,
  savingExercise,
  exerciseFormError,
  readOnly,
  bodyParts,
  filteredCatalog,
  load,
  openForm,
  openExerciseForm,
  cancelExerciseForm,
  saveCustomExercise,
  addExercise,
  removeExercise,
  moveExercise,
  cancelForm,
  save: saveWorkout,
  requestDelete,
  cancelDelete,
  confirmDelete,
  exerciseDetail,
  isCardioExercise
} = useWorkouts(studentId, canManage)

const deleteOpen = computed({
  get: () => Boolean(pendingDelete.value),
  set: (open: boolean) => { if (!open) cancelDelete() }
})
const deleteDescription = computed(() => pendingDelete.value
  ? `Deseja excluir "${pendingDelete.value.name}"? A agenda dos dias com esse treino fica em descanso.`
  : '')

onMounted(async () => {
  await Promise.all([loadContext(), load()])
})
useSeoMeta({ title: 'Treinos' })
</script>

<template>
  <div class="app-shell">
    <UModal
      v-model:open="deleteOpen"
      title="Excluir treino"
      :description="deleteDescription"
      :dismissible="!deleting"
    >
      <template #footer>
        <div class="form-actions">
          <button
            class="secondary-button"
            :disabled="deleting"
            @click="cancelDelete"
          >
            Cancelar
          </button>
          <button
            class="primary-button"
            :disabled="deleting"
            @click="confirmDelete"
          >
            {{ deleting ? 'Excluindo...' : 'Excluir' }}
          </button>
        </div>
      </template>
    </UModal>
    <AppSidebar active="workouts" />
    <main class="app-main module-page">
      <header class="app-topbar">
        <div><p>SUA BIBLIOTECA</p><h1>Treinos</h1></div>
        <div class="topbar-actions">
          <button
            v-if="!readOnly"
            class="secondary-button"
            @click="openForm()"
          >
            <UIcon name="i-lucide-plus" /> Novo treino
          </button>
          <NuxtLink
            to="/agenda"
            class="secondary-button"
          >
            <UIcon name="i-lucide-calendar-days" /> Agenda
          </NuxtLink>
          <button
            class="round-button"
            aria-label="Atualizar"
            :disabled="loading"
            @click="load"
          >
            <UIcon name="i-lucide-refresh-cw" />
          </button>
        </div>
      </header>
      <p
        v-if="studentName"
        class="student-context"
      >
        <UIcon name="i-lucide-user-round" />
        <template v-if="readOnly">
          Você está consultando os treinos de <b>{{ studentName }}</b>. Apenas o personal trainer do vínculo pode editar.
        </template>
        <template v-else>
          Você está gerenciando os treinos de <b>{{ studentName }}</b>.
        </template>
        <NuxtLink to="/workouts">Voltar para meus treinos</NuxtLink>
      </p>
      <section class="module-intro workout-intro">
        <div>
          <p class="eyebrow">
            <span /> evolucao constante
          </p><h2>Seus treinos cadastrados</h2><p>Revise os exercicios e acompanhe sua rotina.</p>
        </div><UIcon name="i-lucide-dumbbell" />
      </section>
      <p
        v-if="actionError"
        class="form-error form-error-left"
        role="alert"
      >
        {{ actionError }}
      </p>
      <section
        v-if="showForm && !readOnly"
        class="registration-card"
      >
        <div class="registration-heading">
          <div>
            <p class="eyebrow">
              <span /> {{ editingId ? 'editando' : 'novo planejamento' }}
            </p><h2>{{ editingId ? 'Editar treino' : 'Cadastrar treino' }}</h2>
          </div><button
            class="icon-text-button"
            @click="cancelForm"
          >
            <UIcon name="i-lucide-x" /> Cancelar
          </button>
        </div><label>Nome do treino<input
          v-model="workoutName"
          placeholder="Ex.: Peito e triceps"
          maxlength="80"
        ></label><div class="catalog-grid">
          <div>
            <div class="catalog-heading">
              <h3>Exercícios disponíveis</h3><div class="catalog-heading-meta">
                <small>{{ filteredCatalog.length }} de {{ catalog.length }}</small><button
                  v-if="!readOnly && !showExerciseForm"
                  type="button"
                  class="icon-text-button"
                  @click="openExerciseForm()"
                >
                  <UIcon name="i-lucide-plus" /> Criar exercício
                </button>
              </div>
            </div><form
              v-if="showExerciseForm && !readOnly"
              class="custom-exercise-form"
              @submit.prevent="saveCustomExercise"
            >
              <p class="custom-exercise-form-title">
                {{ editingExerciseId ? 'Editando exercício personalizado' : 'Novo exercício personalizado' }}
              </p><label>Nome do exercício<input
                v-model="exerciseName"
                maxlength="80"
                placeholder="Ex.: Tríceps testa"
              ></label><label>Parte do corpo<select v-model="exerciseBodyPart">
                <option
                  value=""
                  disabled
                >
                  Selecione
                </option><option
                  v-for="option in BODY_PART_OPTIONS"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select></label><p
                v-if="exerciseFormError"
                class="form-error form-error-left"
              >
                {{ exerciseFormError }}
              </p><div class="form-actions">
                <button
                  type="button"
                  class="secondary-button"
                  @click="cancelExerciseForm"
                >
                  Cancelar
                </button><button
                  type="submit"
                  class="primary-button"
                  :disabled="savingExercise"
                >
                  {{ savingExercise ? 'Salvando...' : (editingExerciseId ? 'Salvar exercício' : 'Criar exercício') }}
                </button>
              </div>
            </form><div class="exercise-filters">
              <label>Pesquisar exercício<div class="search-input">
                <UIcon name="i-lucide-search" /><input
                  v-model="exerciseSearch"
                  type="search"
                  placeholder="Nome ou parte do corpo"
                >
              </div></label><label>Parte do corpo<select v-model="selectedBodyPart">
                <option value="">
                  Todas as partes do corpo
                </option><option
                  v-for="bodyPart in bodyParts"
                  :key="bodyPart"
                  :value="bodyPart"
                >
                  {{ bodyPartLabel(bodyPart) }}
                </option>
              </select></label>
            </div><div class="selection-list">
              <div
                v-for="exercise in filteredCatalog"
                :key="exercise.id"
                class="selection-item"
              >
                <button
                  type="button"
                  class="selection-add"
                  :disabled="selectedExercises.some(item => item.id === exercise.id)"
                  @click="addExercise(exercise)"
                >
                  <span><b>{{ exercise.name }}</b><small>{{ bodyPartLabel(exercise.body_part) || 'Exercício' }}</small></span><UIcon name="i-lucide-plus" />
                </button><button
                  v-if="exercise.custom && !readOnly"
                  type="button"
                  class="selection-edit"
                  aria-label="Editar exercício"
                  @click="openExerciseForm(exercise)"
                >
                  <UIcon name="i-lucide-pencil" />
                </button>
              </div><p v-if="!catalog.length">
                Carregando exercicios...
              </p><p v-else-if="!filteredCatalog.length">
                Nenhum exercicio encontrado para os filtros selecionados.
              </p>
            </div>
          </div><div>
            <h3>Seu treino <small>({{ selectedExercises.length }})</small></h3><div class="selected-list">
              <p v-if="!selectedExercises.length">
                Selecione os exercicios ao lado.
              </p><article
                v-for="(exercise, index) in selectedExercises"
                :key="exercise.id"
              >
                <div class="selected-exercise-head">
                  <b>{{ exercise.name }}</b><div class="selected-exercise-actions">
                    <button
                      type="button"
                      class="move-exercise"
                      aria-label="Mover exercício para cima"
                      :disabled="index === 0"
                      @click="moveExercise(index, -1)"
                    >
                      <UIcon name="i-lucide-arrow-up" />
                    </button><button
                      type="button"
                      class="move-exercise"
                      aria-label="Mover exercício para baixo"
                      :disabled="index === selectedExercises.length - 1"
                      @click="moveExercise(index, 1)"
                    >
                      <UIcon name="i-lucide-arrow-down" />
                    </button><button
                      type="button"
                      class="remove-exercise"
                      aria-label="Remover exercicio"
                      @click="removeExercise(exercise.id)"
                    >
                      <UIcon name="i-lucide-trash-2" />
                    </button>
                  </div>
                </div><div
                  v-if="isCardioExercise(exercise)"
                  class="exercise-fields exercise-fields-cardio"
                >
                  <label>Duracao (min)<input
                    v-model.number="exercise.duration_minutes"
                    type="number"
                    min="1"
                    max="600"
                  ></label><label>Descanso (s)<input
                    v-model.number="exercise.rest_seconds"
                    type="number"
                    min="0"
                    max="600"
                    step="5"
                  ></label>
                </div><div
                  v-else
                  class="exercise-fields"
                >
                  <label>Series<input
                    v-model.number="exercise.series"
                    type="number"
                    min="1"
                    max="99"
                  ></label><label>Repeticoes<input
                    v-model.number="exercise.repetitions"
                    type="number"
                    min="1"
                    max="999"
                  ></label><label>Carga (kg)<input
                    v-model.number="exercise.load"
                    type="number"
                    min="0"
                    step="0.5"
                  ></label><label>Descanso (s)<input
                    v-model.number="exercise.rest_seconds"
                    type="number"
                    min="0"
                    max="600"
                    step="5"
                  ></label>
                </div>
              </article>
            </div>
          </div>
        </div><p
          v-if="formError"
          class="form-error form-error-left"
        >
          {{ formError }}
        </p><div class="form-actions">
          <button
            class="secondary-button"
            @click="cancelForm"
          >
            Cancelar
          </button><button
            class="primary-button"
            :disabled="saving"
            @click="saveWorkout"
          >
            {{ saving ? 'Salvando...' : (editingId ? 'Salvar alteracoes' : 'Salvar treino') }}
          </button>
        </div>
      </section>
      <div
        v-if="loading"
        class="page-state"
      >
        <UIcon
          name="i-lucide-loader-circle"
          class="spin"
        /><p>Carregando treinos...</p>
      </div><div
        v-else-if="errorMessage"
        class="page-state error-state"
      >
        <UIcon name="i-lucide-circle-alert" /><p>{{ errorMessage }}</p><button
          class="primary-button"
          @click="load"
        >
          Tentar novamente
        </button>
      </div><div
        v-else-if="!workouts.length"
        class="page-state"
      >
        <UIcon name="i-lucide-dumbbell" /><h2>Nenhum treino cadastrado</h2><p v-if="readOnly">
          O profissional responsável ainda não cadastrou treinos.
        </p><p v-else>
          Comece criando seu primeiro treino.
        </p><button
          v-if="!readOnly"
          class="primary-button"
          @click="openForm()"
        >
          Cadastrar treino
        </button>
      </div>
      <section
        v-else
        class="workout-list"
      >
        <article
          v-for="workout in workouts"
          :key="workout.id"
          class="workout-list-card"
        >
          <div class="workout-card-head">
            <button
              class="workout-summary"
              type="button"
              @click="expandedWorkoutId = expandedWorkoutId === workout.id ? null : workout.id"
            >
              <span class="workout-list-icon"><UIcon name="i-lucide-dumbbell" /></span><span><h2>{{ workout.name }}</h2><p>{{ workout.dayOfWeek && workout.dayOfWeek >= 1 && workout.dayOfWeek <= 7 ? dayLabel(workout.dayOfWeek) : 'Sem dia na agenda' }} · {{ workout.exercises?.length ?? 0 }} exercicios</p></span><UIcon :name="expandedWorkoutId === workout.id ? 'i-lucide-chevron-up' : 'i-lucide-chevron-down'" />
            </button>
            <div
              v-if="!readOnly"
              class="workout-card-actions"
            >
              <button
                aria-label="Editar treino"
                @click="openForm(workout)"
              >
                <UIcon name="i-lucide-pencil" />
              </button>
              <button
                aria-label="Excluir treino"
                @click="requestDelete(workout)"
              >
                <UIcon name="i-lucide-trash-2" />
              </button>
            </div>
          </div>
          <ul
            v-if="expandedWorkoutId === workout.id"
            class="exercise-list"
          >
            <li
              v-for="exercise in workout.exercises || []"
              :key="exercise.id"
            >
              <span>{{ exercise.name }}</span><small>{{ exerciseDetail(exercise) }}</small>
            </li>
          </ul>
        </article>
      </section>
    </main>
  </div>
</template>
