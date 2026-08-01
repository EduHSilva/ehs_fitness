<script setup lang="ts">
import { useStudents } from '~/features/students/useStudents'

const { students, isProfessional, error: integrationError, load, addStudent, manage } = useStudents()
const name = ref('')
const email = ref('')
const goal = ref('')
const errorMessage = ref('')
onMounted(load)
async function add() {
  errorMessage.value = ''
  if (!name.value.trim() || !email.value.trim()) {
    errorMessage.value = 'Informe nome e e-mail do aluno.'
    return
  }
  try {
    await addStudent({ name: name.value.trim(), email: email.value.trim(), goal: goal.value.trim() })
    name.value = ''
    email.value = ''
    goal.value = ''
  } catch {
    errorMessage.value = integrationError.value || 'Nao foi possivel cadastrar o aluno.'
  }
}
useSeoMeta({ title: 'Alunos — EHS Fitness' })
</script>

<template>
  <div class="app-shell">
    <AppSidebar active="students" /><main class="app-main module-page">
      <header class="app-topbar">
        <div><p>ÁREA PROFISSIONAL</p><h1>Alunos</h1></div>
      </header>
      <section
        v-if="!isProfessional"
        class="page-state"
      >
        <UIcon name="i-lucide-lock" /><h2>Área para contas profissionais</h2><p>Cadastre-se como personal trainer ou nutricionista para administrar alunos.</p>
      </section>
      <template v-else>
        <section class="registration-card">
          <div class="registration-heading">
            <div>
              <p class="eyebrow">
                <span /> novo aluno
              </p><h2>Adicionar aluno</h2>
            </div>
          </div><form
            class="student-form"
            @submit.prevent="add"
          >
            <label>Nome<input
              v-model="name"
              placeholder="Nome completo"
            ></label><label>E-mail<input
              v-model="email"
              type="email"
              placeholder="aluno@email.com"
            ></label><label>Objetivo<input
              v-model="goal"
              placeholder="Ex.: ganho de massa"
            ></label><button
              class="primary-button"
              type="submit"
            >
              <UIcon name="i-lucide-user-plus" /> Cadastrar aluno
            </button>
          </form><p
            v-if="errorMessage"
            class="form-error form-error-left"
          >
            {{ errorMessage }}
          </p>
        </section>
        <section
          v-if="students.length"
          class="student-grid"
        >
          <article
            v-for="student in students"
            :key="student.id"
            class="student-card"
          >
            <span class="student-avatar">{{ student.name.slice(0, 1).toUpperCase() }}</span><div><h2>{{ student.name }}</h2><p>{{ student.email }}</p><small>{{ student.goal || 'Sem objetivo definido' }}</small></div><div class="student-actions">
              <button @click="manage(student.id, 'workouts')">
                <UIcon name="i-lucide-dumbbell" /> Treino
              </button><button @click="manage(student.id, 'diet')">
                <UIcon name="i-lucide-utensils" /> Dieta
              </button><button @click="manage(student.id, 'habits')">
                <UIcon name="i-lucide-list-checks" /> Hábitos
              </button>
            </div>
          </article>
        </section><div
          v-else
          class="page-state"
        >
          <UIcon name="i-lucide-users" /><h2>Nenhum aluno cadastrado</h2><p>Cadastre o primeiro aluno para criar seus planos.</p>
        </div>
      </template>
    </main>
  </div>
</template>
