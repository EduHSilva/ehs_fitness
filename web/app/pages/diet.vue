<script setup lang="ts">
import { useDiet } from '~/features/diet/useDiet'
import { useStudentContext } from '~/features/students/useStudentContext'

const { studentId, studentName } = useStudentContext()
const diet = useDiet(studentId)
const { meals, foods, loading, saving, searching, error: errorMessage, formError, showForm, mealName, mealHour, foodQuery, selectedFoods, load: loadMeals, addFood, removeFood, cancelForm, save: saveMeal } = diet

onMounted(loadMeals)
useSeoMeta({ title: 'Alimentacao — EHS Fitness' })
</script>

<template>
  <div class="app-shell">
    <AppSidebar active="diet" />
    <main class="app-main module-page">
      <header class="app-topbar">
        <div><p>SEU PLANO ALIMENTAR</p><h1>Alimentacao</h1></div><div class="topbar-actions">
          <button
            class="secondary-button"
            @click="showForm = true"
          >
            <UIcon name="i-lucide-plus" /> Nova refeicao
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
      <p
        v-if="studentName"
        class="student-context"
      >
        <UIcon name="i-lucide-user-round" /> Você está gerenciando a dieta de <b>{{ studentName }}</b>. <NuxtLink to="/diet">Voltar para minha alimentação</NuxtLink>
      </p>
      <section class="module-intro">
        <div>
          <p class="eyebrow">
            <span /> rotina equilibrada
          </p><h2>Suas refeicoes planejadas</h2><p>Consulte e organize as refeicoes da sua rotina.</p>
        </div><UIcon name="i-lucide-utensils" />
      </section>
      <section
        v-if="showForm"
        class="registration-card"
      >
        <div class="registration-heading">
          <div>
            <p class="eyebrow">
              <span /> nova refeicao
            </p><h2>Cadastrar refeicao</h2>
          </div><button
            class="icon-text-button"
            @click="cancelForm"
          >
            <UIcon name="i-lucide-x" /> Cancelar
          </button>
        </div><div class="form-two-columns">
          <label>Nome da refeicao<input
            v-model="mealName"
            placeholder="Ex.: Cafe da manha"
            maxlength="80"
          ></label><label>Horario<input
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
            <span>{{ food.name }}</span><UIcon name="i-lucide-plus" />
          </button><p v-if="!searching && !foods.length">
            Nenhum alimento encontrado.
          </p>
        </div><div class="selected-list food-list">
          <h3>Alimentos da refeicao <small>({{ selectedFoods.length }})</small></h3><p v-if="!selectedFoods.length">
            Busque e adicione os alimentos da refeicao.
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
              ></label><label>Observacao<input
                v-model="food.obs"
                placeholder="Ex.: cozido"
              ></label>
            </div>
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
            {{ saving ? 'Salvando...' : 'Salvar refeicao' }}
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
        <UIcon name="i-lucide-utensils" /><h2>Nenhuma refeicao cadastrada</h2><p>Comece organizando a sua primeira refeicao.</p><button
          class="primary-button"
          @click="showForm = true"
        >
          Cadastrar refeicao
        </button>
      </div>
      <section
        v-else
        class="meal-grid"
      >
        <article
          v-for="meal in meals"
          :key="meal.id"
          class="meal-card"
        >
          <div class="meal-card-icon">
            <UIcon name="i-lucide-salad" />
          </div><div class="meal-card-heading">
            <span>{{ meal.hour || 'Sem horario' }}</span><h2>{{ meal.name }}</h2><p>{{ meal.foods?.length ?? 0 }} item(ns)</p>
          </div><ul v-if="meal.foods?.length">
            <li
              v-for="(food, index) in meal.foods"
              :key="index"
            >
              <span>{{ food.food?.name || food.name || 'Alimento' }}</span><small>{{ food.quantity ? `${food.quantity} g` : '' }} {{ food.observation || food.obs || '' }}</small>
            </li>
          </ul>
        </article>
      </section>
    </main>
  </div>
</template>
