<script setup lang="ts">
import { useStudentContext } from '~/features/students/useStudentContext'
import { useWorkouts } from '~/features/workouts/useWorkouts'

const { studentId, studentName } = useStudentContext()
const workoutManager = useWorkouts(studentId)
const { workouts, catalog, loading, saving, error: errorMessage, formError, showForm, expandedWorkoutId, workoutName, selectedExercises, exerciseSearch, selectedBodyPart, bodyParts, filteredCatalog, load: loadWorkouts, openForm, addExercise, removeExercise, cancelForm, save: saveWorkout, exerciseDetail } = workoutManager

onMounted(loadWorkouts)
useSeoMeta({ title: 'Treinos — EHS Fitness' })
</script>

<template>
  <div class="app-shell">
    <AppSidebar active="workouts" />
    <main class="app-main module-page">
      <header class="app-topbar">
        <div><p>SUA BIBLIOTECA</p><h1>Treinos</h1></div><div class="topbar-actions">
          <button
            class="secondary-button"
            @click="openForm"
          >
            <UIcon name="i-lucide-plus" /> Novo treino
          </button><button
            class="round-button"
            aria-label="Atualizar"
            :disabled="loading"
            @click="loadWorkouts"
          >
            <UIcon name="i-lucide-refresh-cw" />
          </button>
        </div>
      </header>
      <p
        v-if="studentName"
        class="student-context"
      >
        <UIcon name="i-lucide-user-round" /> Você está gerenciando o treino de <b>{{ studentName }}</b>. <NuxtLink to="/workouts">Voltar para meus treinos</NuxtLink>
      </p>
      <section class="module-intro workout-intro">
        <div>
          <p class="eyebrow">
            <span /> evolucao constante
          </p><h2>Seus treinos cadastrados</h2><p>Revise os exercicios e acompanhe sua rotina.</p>
        </div><UIcon name="i-lucide-dumbbell" />
      </section>
      <section
        v-if="showForm"
        class="registration-card"
      >
        <div class="registration-heading">
          <div>
            <p class="eyebrow">
              <span /> novo planejamento
            </p><h2>Cadastrar treino</h2>
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
              <h3>Exercicios disponiveis</h3><small>{{ filteredCatalog.length }} de {{ catalog.length }}</small>
            </div><div class="exercise-filters">
              <label class="search-input">Pesquisar exercicio
                <UIcon name="i-lucide-search" /><input
                  v-model="exerciseSearch"
                  type="search"
                  placeholder="Nome ou parte do corpo"
                >
              </label><label>Parte do corpo<select v-model="selectedBodyPart">
                <option value="">
                  Todas as partes do corpo
                </option><option
                  v-for="bodyPart in bodyParts"
                  :key="bodyPart"
                  :value="bodyPart"
                >
                  {{ bodyPart }}
                </option>
              </select></label>
            </div><div class="selection-list">
              <button
                v-for="exercise in filteredCatalog"
                :key="exercise.id"
                type="button"
                :disabled="selectedExercises.some(item => item.id === exercise.id)"
                @click="addExercise(exercise)"
              >
                <span><b>{{ exercise.name }}</b><small>{{ exercise.body_part || 'Exercicio' }}</small></span><UIcon name="i-lucide-plus" />
              </button><p v-if="!catalog.length">
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
                v-for="exercise in selectedExercises"
                :key="exercise.id"
              >
                <div>
                  <b>{{ exercise.name }}</b><button
                    type="button"
                    aria-label="Remover exercicio"
                    @click="removeExercise(exercise.id)"
                  >
                    <UIcon name="i-lucide-trash-2" />
                  </button>
                </div><div class="exercise-fields">
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
            {{ saving ? 'Salvando...' : 'Salvar treino' }}
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
          @click="loadWorkouts"
        >
          Tentar novamente
        </button>
      </div><div
        v-else-if="!workouts.length"
        class="page-state"
      >
        <UIcon name="i-lucide-dumbbell" /><h2>Nenhum treino cadastrado</h2><p>Comece criando seu primeiro treino.</p><button
          class="primary-button"
          @click="openForm"
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
          <button
            class="workout-summary"
            type="button"
            @click="expandedWorkoutId = expandedWorkoutId === workout.id ? null : workout.id"
          >
            <span class="workout-list-icon"><UIcon name="i-lucide-dumbbell" /></span><span><h2>{{ workout.name }}</h2><p>{{ workout.exercises?.length ?? 0 }} exercicios</p></span><UIcon :name="expandedWorkoutId === workout.id ? 'i-lucide-chevron-up' : 'i-lucide-chevron-down'" />
          </button><ul
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
