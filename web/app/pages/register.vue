<script setup lang="ts">
import { registerAccount, type AccountRole } from '~/features/auth/auth.api'

const name = ref('')
const email = ref('')
const role = ref<AccountRole>('student')
const errorMessage = ref('')
const loading = ref(false)

async function register() {
  errorMessage.value = ''
  if (!name.value.trim() || !email.value.trim()) {
    errorMessage.value = 'Informe seu nome e e-mail para continuar.'
    return
  }
  loading.value = true
  try {
    const session = (await registerAccount({ name: name.value.trim(), email: email.value.trim(), role: role.value })).data
    if (!session?.token) throw new Error('Cadastro concluido sem uma sessao valida.')
    localStorage.setItem('user_token', session.token)
    localStorage.setItem('user', JSON.stringify(session.user))
    await navigateTo('/home')
  } catch (cause) {
    errorMessage.value = (cause as { data?: { message?: string }, message?: string }).data?.message ?? (cause as Error).message ?? 'Nao foi possivel criar sua conta.'
  } finally {
    loading.value = false
  }
}
useSeoMeta({ title: 'Criar conta — EHS Fitness' })
</script>

<template>
  <main class="register-page">
    <section class="register-aside">
      <NuxtLink
        to="/"
        class="brand inverse"
        aria-label="Voltar para a página inicial"
      ><span class="brand-mark"><UIcon name="i-lucide-activity" /></span><span>EHS <b>Fitness</b></span></NuxtLink>
      <div class="register-aside-copy">
        <p class="eyebrow light">
          <span /> comece no seu ritmo
        </p>
        <h1>Um espaço feito para a sua evolução.</h1>
        <p>Treinos, alimentação e acompanhamento em uma rotina que funciona para você.</p>
      </div>
      <ol class="register-benefits">
        <li><UIcon name="i-lucide-check" /> Cadastro gratuito e rápido</li>
        <li><UIcon name="i-lucide-check" /> Personalize sua experiência</li>
        <li><UIcon name="i-lucide-check" /> Acesse de onde estiver</li>
      </ol>
      <div class="register-orb register-orb-one" /><div class="register-orb register-orb-two" />
    </section>
    <section class="register-panel">
      <div class="register-mobile-brand">
        <NuxtLink
          to="/"
          class="brand"
        ><span class="brand-mark"><UIcon name="i-lucide-activity" /></span><span>EHS <b>Fitness</b></span></NuxtLink>
      </div>
      <div class="register-content">
        <NuxtLink
          to="/"
          class="back-link"
        ><UIcon name="i-lucide-arrow-left" /> Voltar para o início</NuxtLink>
        <div class="register-heading">
          <span class="register-step">Passo 1 de 1</span>
          <h2>Crie sua conta</h2><p>Leva menos de um minuto para começar.</p>
        </div>
        <form
          class="register-form"
          @submit.prevent="register"
        >
          <div class="register-field">
            <label for="name">Como podemos chamar você?</label><div class="register-input">
              <UIcon name="i-lucide-user-round" /><input
                id="name"
                v-model="name"
                placeholder="Seu nome"
                autocomplete="name"
                required
              >
            </div>
          </div>
          <div class="register-field">
            <label for="email">E-mail</label><div class="register-input">
              <UIcon name="i-lucide-mail" /><input
                id="email"
                v-model="email"
                type="email"
                placeholder="seu@email.com"
                autocomplete="email"
                required
              >
            </div>
          </div>
          <fieldset class="role-selector">
            <legend>Qual é o seu objetivo na plataforma?</legend>
            <label><input
              v-model="role"
              value="student"
              type="radio"
            ><span class="role-icon"><UIcon name="i-lucide-dumbbell" /></span><span><b>Aluno / praticante</b><small>Organize seu próprio bem-estar.</small></span><UIcon
              class="role-check"
              name="i-lucide-check-circle-2"
            /></label>
            <label><input
              v-model="role"
              value="personal_trainer"
              type="radio"
            ><span class="role-icon"><UIcon name="i-lucide-users-round" /></span><span><b>Personal trainer</b><small>Gerencie seus alunos e sua rotina.</small></span><UIcon
              class="role-check"
              name="i-lucide-check-circle-2"
            /></label>
            <label><input
              v-model="role"
              value="nutritionist"
              type="radio"
            ><span class="role-icon"><UIcon name="i-lucide-apple" /></span><span><b>Professor / nutricionista</b><small>Crie planos para alunos e para você.</small></span><UIcon
              class="role-check"
              name="i-lucide-check-circle-2"
            /></label>
          </fieldset>
          <button
            class="primary-button submit-button"
            type="submit"
            :disabled="loading"
          >
            {{ loading ? 'Criando conta...' : 'Continuar' }} <UIcon name="i-lucide-arrow-right" />
          </button>
          <p class="register-privacy">
            <UIcon name="i-lucide-shield-check" /> Seus dados são protegidos e usados apenas para personalizar sua experiência.
          </p>
          <p
            v-if="errorMessage"
            class="form-error"
            role="alert"
          >
            {{ errorMessage }}
          </p>
        </form>
        <p class="signup-text">
          Já tem uma conta? <NuxtLink to="/login">Entrar</NuxtLink>
        </p>
      </div>
    </section>
  </main>
</template>
