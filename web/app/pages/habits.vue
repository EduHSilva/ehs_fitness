<script setup lang="ts">
import { useHabits } from '~/features/habits/useHabits'
import { useStudentContext } from '~/features/students/useStudentContext'

const { studentId, studentName } = useStudentContext()
const { habits, name, category, error: errorMessage, categories, load, add: addHabit, remove: removeHabit } = useHabits(studentId)

onMounted(load)
useSeoMeta({ title: 'Habitos — EHS Fitness' })
</script>

<template>
  <div class="app-shell">
    <AppSidebar active="habits" />
    <main class="app-main module-page">
      <header class="app-topbar">
        <div>
          <p>SUA ROTINA</p>
          <h1>Habitos</h1>
        </div>
      </header>
      <p
        v-if="studentName"
        class="student-context"
      >
        <UIcon name="i-lucide-user-round" /> Você está gerenciando os hábitos de <b>{{ studentName }}</b>. <NuxtLink to="/habits">Voltar para meus hábitos</NuxtLink>
      </p>

      <section class="module-intro habits-intro">
        <div>
          <p class="eyebrow">
            <span /> constancia diaria
          </p>
          <h2>Construa bons habitos</h2>
          <p>Registre pequenas acoes que ajudam voce a chegar mais longe.</p>
        </div>
        <UIcon name="i-lucide-list-checks" />
      </section>

      <section class="registration-card">
        <div class="registration-heading">
          <div>
            <p class="eyebrow">
              <span /> novo habito
            </p>
            <h2>Adicionar habito</h2>
          </div>
        </div>
        <form
          class="habit-form"
          @submit.prevent="addHabit"
        >
          <label>
            Nome do habito
            <input
              v-model="name"
              placeholder="Ex.: Beber 2 litros de agua"
              maxlength="100"
            >
          </label>
          <label>
            Categoria
            <select v-model="category">
              <option
                value=""
                disabled
              >Selecione uma categoria</option>
              <option
                v-for="item in categories"
                :key="item"
                :value="item"
              >{{ item }}</option>
            </select>
          </label>
          <button
            class="primary-button"
            type="submit"
          >
            <UIcon name="i-lucide-plus" /> Adicionar
          </button>
        </form>
        <p
          v-if="errorMessage"
          class="form-error form-error-left"
        >
          {{ errorMessage }}
        </p>
      </section>

      <section
        v-if="habits.length"
        class="habit-grid"
        aria-label="Habitos cadastrados"
      >
        <article
          v-for="habit in habits"
          :key="habit.id"
          class="habit-card"
        >
          <span class="habit-icon"><UIcon name="i-lucide-check" /></span>
          <div><h2>{{ habit.name }}</h2><p>{{ habit.category }}</p></div>
          <button
            type="button"
            :aria-label="`Remover ${habit.name}`"
            @click="removeHabit(habit.id)"
          >
            <UIcon name="i-lucide-trash-2" />
          </button>
        </article>
      </section>
      <div
        v-else
        class="page-state"
      >
        <UIcon name="i-lucide-list-checks" /><h2>Nenhum habito cadastrado</h2><p>Adicione o primeiro habito que deseja praticar.</p>
      </div>
    </main>
  </div>
</template>
