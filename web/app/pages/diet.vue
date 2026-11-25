<script setup lang="ts">
import { useDiet } from '~/features/diet/useDiet'
import { useStudentContext } from '~/features/students/useStudentContext'

const { studentId, studentName, canManageDiet, managerName, loadContext } = useStudentContext()
const diet = useDiet(studentId, canManageDiet)
const { meals, foods, loading, saving, searching, error: errorMessage, actionError, formError, showForm, editingId, mealName, mealHour, foodQuery, selectedFoods, pendingDelete, deleting, readOnly, load: loadMeals, addFood, removeFood, cancelForm, startEdit, save: saveMeal, requestDelete, cancelDelete, confirmDelete, foodMacros, mealMacros, dayMacros, dayCalories, hasMacros } = diet

const deleteOpen = computed({
  get: () => Boolean(pendingDelete.value),
  set: (open: boolean) => { if (!open) cancelDelete() }
})
const deleteDescription = computed(() => pendingDelete.value
  ? `Deseja excluir "${pendingDelete.value.name}"? Essa ação não pode ser desfeita.`
  : '')

onMounted(async () => {
  await Promise.all([loadContext(), loadMeals()])
})
definePageMeta({ key: route => route.fullPath })
useSeoMeta({ title: 'Alimentação' })
</script>

<template>
  <div class="app-shell">
    <UModal
      v-model:open="deleteOpen"
      title="Excluir refeição"
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
    <AppSidebar active="diet" />
    <main class="app-main module-page">
      <header class="app-topbar">
        <div><p>SEU PLANO ALIMENTAR</p><h1>Alimentação</h1></div><div class="topbar-actions">
          <button
            v-if="!readOnly"
            class="secondary-button"
            @click="showForm = true"
          >
            <UIcon name="i-lucide-plus" /> Nova refeição
          </button><button
            class="round-button"
            aria-label="Atualizar"
            :disabled="loading"
            @click="loadMeals"
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
          Você está consultando a dieta de <b>{{ studentName }}</b>. Apenas o nutricionista do vínculo pode editar.
        </template>
        <template v-else>
          Você está gerenciando a dieta de <b>{{ studentName }}</b>.
        </template>
        <NuxtLink to="/diet">Voltar para minha alimentação</NuxtLink>
      </p>
      <p
        v-if="studentName && readOnly"
        class="notice-banner"
      >
        <UIcon name="i-lucide-eye" /> Você está visualizando a dieta de <b>{{ studentName }}</b> em modo somente leitura.
      </p>
      <p
        v-if="!studentName && readOnly && managerName('NUTRITIONIST')"
        class="notice-banner"
      >
        <UIcon name="i-lucide-lock" /> Sua dieta é gerenciada por <b>{{ managerName('NUTRITIONIST') }}</b>.
      </p>
      <section class="module-intro">
        <div>
          <p class="eyebrow">
            <span /> rotina equilibrada
          </p><h2>Suas refeições planejadas</h2><p>Consulte e organize as refeições da sua rotina.</p>
        </div><UIcon name="i-lucide-utensils" />
      </section>
      <p
        v-if="actionError"
        class="form-error form-error-left"
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
              <span /> {{ editingId ? 'editar refeição' : 'nova refeição' }}
            </p><h2>{{ editingId ? 'Editar refeição' : 'Cadastrar refeição' }}</h2>
          </div><button
            class="icon-text-button"
            @click="cancelForm"
          >
            <UIcon name="i-lucide-x" /> Cancelar
          </button>
        </div><div class="form-two-columns">
          <label>Nome da refeição<input
            v-model="mealName"
            placeholder="Ex.: Café da manhã"
            maxlength="80"
          ></label><label>Horário<input
            v-model="mealHour"
            type="time"
          ></label>
        </div><label>Buscar alimento<div class="search-input"><UIcon name="i-lucide-search" /><input
          v-model="foodQuery"
          placeholder="Digite ao menos 2 letras"
        ></div></label><div
          v-if="foodQuery.length >= 2"
          class="food-results"
        >
          <p v-if="searching">
            Buscando alimentos...
          </p><button
            v-for="food in foods"
            :key="food.id"
            type="button"
            @click="addFood(food)"
          >
            <span>
              <b>{{ food.name }}</b>
              <small v-if="hasMacros(food)">{{ foodMacros(food, 100) }} (por 100 g)</small>
            </span><UIcon name="i-lucide-plus" />
          </button><p v-if="!searching && !foods.length">
            Nenhum alimento encontrado.
          </p>
        </div><div class="selected-list food-list">
          <h3>Alimentos da refeição <small>({{ selectedFoods.length }})</small></h3><p v-if="!selectedFoods.length">
            Busque e adicione os alimentos da refeição.
          </p><article
            v-for="food in selectedFoods"
            :key="food.food_id"
          >
            <div>
              <b>{{ food.name }}</b><button
                type="button"
                aria-label="Remover alimento"
                @click="removeFood(food.food_id)"
              >
                <UIcon name="i-lucide-trash-2" />
              </button>
            </div><div class="food-fields">
              <label>Quantidade (g)<input
                v-model.number="food.quantity"
                type="number"
                min="1"
              ></label><label>Observação<input
                v-model="food.observation"
                placeholder="Ex.: cozido"
              ></label>
            </div><p
              v-if="hasMacros(food)"
              class="nutrition-note"
            >
              {{ foodMacros(food, food.quantity) }}
            </p>
          </article>
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
            @click="saveMeal"
          >
            {{ saving ? 'Salvando...' : editingId ? 'Salvar alterações' : 'Salvar refeição' }}
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
        /><p>Carregando refeicoes...</p>
      </div><div
        v-else-if="errorMessage"
        class="page-state error-state"
      >
        <UIcon name="i-lucide-circle-alert" /><p>{{ errorMessage }}</p><button
          class="primary-button"
          @click="loadMeals"
        >
          Tentar novamente
        </button>
      </div><div
        v-else-if="!meals.length"
        class="page-state"
      >
        <UIcon name="i-lucide-utensils" /><h2>Nenhuma refeição cadastrada</h2><p>{{ readOnly ? 'Este aluno ainda não possui refeições cadastradas.' : 'Comece organizando a sua primeira refeição.' }}</p><button
          v-if="!readOnly"
          class="primary-button"
          @click="showForm = true"
        >
          Cadastrar refeição
        </button>
      </div>
      <template v-else>
        <section
          v-if="dayMacros"
          class="day-summary"
        >
          <span class="metric-icon orange"><UIcon name="i-lucide-flame" /></span>
          <div>
            <p>TOTAL DO DIA</p>
            <strong>{{ dayCalories }} kcal</strong>
            <small>{{ dayMacros }}</small>
          </div>
        </section>
        <section class="meal-grid">
          <article
            v-for="meal in meals"
            :key="meal.id"
            class="meal-card"
          >
            <div class="meal-card-icon">
              <UIcon name="i-lucide-salad" />
            </div><div class="meal-card-heading">
              <span>{{ meal.hour || 'Sem horário' }}</span><h2>{{ meal.name }}</h2><p>{{ meal.foods?.length ?? 0 }} item(ns)</p>
              <div
                v-if="!readOnly"
                class="record-actions"
              >
                <button
                  type="button"
                  :aria-label="`Editar ${meal.name}`"
                  @click="startEdit(meal)"
                >
                  <UIcon name="i-lucide-pencil" />
                </button>
                <button
                  type="button"
                  class="meal-card-delete"
                  :aria-label="`Excluir ${meal.name}`"
                  @click="requestDelete(meal)"
                >
                  <UIcon name="i-lucide-trash-2" />
                </button>
              </div>
            </div><ul v-if="meal.foods?.length">
              <li
                v-for="(food, index) in meal.foods"
                :key="index"
              >
                <span>{{ food.food_name || food.food?.name || food.name || 'Alimento' }}</span><small>{{ food.quantity ? `${food.quantity} g` : '' }} {{ food.observation || '' }}<span
                  v-if="hasMacros(food)"
                  class="meal-food-macros"
                >{{ foodMacros(food, Number(food.quantity) || 0) }}</span></small>
              </li>
            </ul>
            <p
              v-if="mealMacros(meal)"
              class="meal-totals"
            >
              Total: {{ mealMacros(meal) }}
            </p>
          </article>
        </section>
      </template>
    </main>
  </div>
</template>
