<script setup lang="ts">
import { readStoredInviteToken, storeInviteToken } from '~/features/students/invite'
import type { SessionUser } from '~/composables/useSession'

const route = useRoute()
const email = ref('')
const password = ref('')
const showPassword = ref(false)
const keepConnected = ref(true)
const loading = ref(false)
const errorMessage = ref('')
const config = useRuntimeConfig()

type GoogleTokenResponse = { credential?: string }
type GoogleWindow = Window & {
  google?: {
    accounts?: {
      id?: {
        initialize: (options: {
          client_id: string
          callback: (response: GoogleTokenResponse) => void
        }) => void
        renderButton: (parent: HTMLElement, options: Record<string, unknown>) => void
      }
    }
  }
}

const googleSlot = ref<HTMLElement | null>(null)
const googleEnabled = computed(() => Boolean(config.public.googleClientId))
const googleFailed = ref(false)

let googleRenderId: { renderButton: (parent: HTMLElement, options: Record<string, unknown>) => void } | null = null
let googleRenderedWidth = 0
let googleResizeObserver: ResizeObserver | null = null

function renderGoogleButton() {
  const el = googleSlot.value
  if (!el || !googleRenderId) return

  // O GSI não aceita largura percentual: usa a largura real do slot para o
  // botão acompanhar os campos e não estourar o card em telas estreitas.
  const width = Math.max(200, Math.min(400, Math.floor(el.getBoundingClientRect().width) || 320))
  if (width === googleRenderedWidth) return
  googleRenderedWidth = width
  el.replaceChildren()

  googleRenderId.renderButton(el, {
    type: 'standard',
    theme: 'outline',
    size: 'large',
    text: 'continue_with',
    shape: 'rectangular',
    logo_alignment: 'left',
    width
  })
}

async function loadGoogleIdentity() {
  const w = window as GoogleWindow
  if (w.google?.accounts?.id) return w.google
  await new Promise<void>((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.defer = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Falha ao carregar o Google'))
    document.head.appendChild(script)
  })
  if (!w.google?.accounts?.id) throw new Error('Google indisponível')
  return w.google
}

type SessionPayload = {
  token?: string
  id?: string
  username?: string
  email?: string
  roles?: string[]
  apps?: string[]
  user?: Record<string, unknown>
}

function applySession(data?: SessionPayload) {
  if (!data?.token) throw new Error('Não foi possível entrar com estas credenciais.')
  // O login devolve o usuário "achatado" (id/username/email/roles); guardamos nesse
  // formato para o restante do app ler `user.id` e `user.roles`.
  const loggedUser = (data.user ?? {
    id: data.id,
    username: data.username,
    email: data.email,
    roles: data.roles ?? [],
    apps: data.apps ?? []
  }) as SessionUser
  saveSession(data.token, loggedUser, keepConnected.value)
  storeInviteToken(route.query.invite)
}

function afterLogin() {
  const invite = typeof route.query.invite === 'string' ? route.query.invite : readStoredInviteToken()
  return navigateTo(invite ? { path: '/home', query: { invite } } : '/home')
}

function reportError(error: unknown) {
  const fetchError = error as { data?: { message?: string }, message?: string }
  errorMessage.value = fetchError.data?.message || fetchError.message || 'Não foi possível entrar. Tente novamente.'
}

async function submit() {
  errorMessage.value = ''
  if (!email.value.trim() || !password.value) {
    errorMessage.value = 'Preencha seu e-mail e senha para continuar.'
    return
  }
  if (!config.public.apiUrl) {
    errorMessage.value = 'A URL da API não foi configurada.'
    return
  }

  loading.value = true
  try {
    const response = await $fetch<{ data?: SessionPayload, message?: string }>('/api/users/auth/login', {
      method: 'POST',
      body: { email: email.value.trim(), password: password.value, app: 'FITNESS' }
    })
    if (!response.data?.token) throw new Error(response.message || 'Não foi possível entrar com estas credenciais.')
    applySession(response.data)
    await afterLogin()
  } catch (error: unknown) {
    reportError(error)
  } finally {
    loading.value = false
  }
}

async function submitGoogle(idToken: string) {
  errorMessage.value = ''
  loading.value = true
  try {
    const response = await $fetch<{ data?: SessionPayload, message?: string }>('/api/users/auth/login', {
      method: 'POST',
      body: { tokenGmail: idToken, app: 'FITNESS' }
    })
    if (!response.data?.token) throw new Error(response.message || 'Não foi possível entrar com estas credenciais.')
    applySession(response.data)
    await afterLogin()
  } catch (error: unknown) {
    reportError(error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const el = googleSlot.value
  if (!googleEnabled.value || !el) return
  try {
    const google = await loadGoogleIdentity()
    const id = google.accounts?.id
    if (!id) throw new Error('Google indisponível')
    id.initialize({
      client_id: config.public.googleClientId as string,
      callback: (response) => {
        if (response.credential) void submitGoogle(response.credential)
      }
    })
    googleRenderId = id
    renderGoogleButton()
    googleResizeObserver = new ResizeObserver(renderGoogleButton)
    googleResizeObserver.observe(el)
  } catch {
    googleFailed.value = true
  }
})

onBeforeUnmount(() => {
  googleResizeObserver?.disconnect()
})

useSeoMeta({ title: 'Entrar' })
</script>

<template>
  <main class="login-page">
    <section class="login-aside">
      <NuxtLink
        to="/"
        class="brand inverse"
        aria-label="Voltar para a página inicial"
      ><span class="brand-mark"><UIcon name="i-lucide-activity" /></span><span>EHS <b>Fitness</b></span></NuxtLink>
      <div class="aside-copy">
        <p class="eyebrow light">
          <span /> sua jornada começa aqui
        </p><h1>Cuide de você<br>todos os dias.</h1><p>Tenha seus treinos, refeições e conquistas sempre por perto.</p>
      </div>
      <div class="aside-stat">
        <span class="quote">“</span><p>Não se trata de perfeição. Se trata de aparecer por você.</p><span class="line" />
      </div>
      <div class="aside-orb orb-a" /><div class="aside-orb orb-b" />
    </section>
    <section class="login-panel">
      <div class="mobile-brand">
        <NuxtLink
          to="/"
          class="brand"
        ><span class="brand-mark"><UIcon name="i-lucide-activity" /></span><span>EHS <b>Fitness</b></span></NuxtLink>
      </div>
      <div class="login-content">
        <NuxtLink
          to="/"
          class="back-link"
        ><UIcon name="i-lucide-arrow-left" /> Voltar para o início</NuxtLink>
        <div class="login-heading">
          <h2>Que bom ter você de volta.</h2><p>Entre para continuar construindo sua melhor versão.</p>
        </div>
        <form
          novalidate
          @submit.prevent="submit"
        >
          <label for="email">E-mail</label><div class="input-wrap">
            <UIcon name="i-lucide-mail" /><input
              id="email"
              v-model="email"
              type="email"
              placeholder="seu@email.com"
              autocomplete="email"
              required
            >
          </div>
          <div class="password-label">
            <label for="password">Senha</label><a href="/user/reset-password">Esqueci minha senha</a>
          </div>
          <div class="input-wrap">
            <UIcon name="i-lucide-lock-keyhole" /><input
              id="password"
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="Sua senha"
              autocomplete="current-password"
              required
            ><button
              type="button"
              :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'"
              @click="showPassword = !showPassword"
            >
              <UIcon :name="showPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'" />
            </button>
          </div>
          <label class="checkbox-label"><input
            v-model="keepConnected"
            type="checkbox"
          ><span /> Manter-me conectado</label>
          <button
            class="primary-button submit-button"
            type="submit"
            :disabled="loading"
          >
            {{ loading ? 'Entrando...' : 'Entrar na minha conta' }} <UIcon name="i-lucide-arrow-right" />
          </button>
          <template v-if="googleEnabled">
            <div
              v-if="!googleFailed"
              class="google-divider"
            >
              <span>ou</span>
            </div>
            <div
              v-if="!googleFailed"
              ref="googleSlot"
              class="google-slot"
            />
          </template>
          <p
            v-if="googleFailed"
            class="form-error"
            role="alert"
          >
            Não foi possível carregar o login com Google.
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
          Ainda não tem uma conta? <NuxtLink to="/register">Crie gratuitamente</NuxtLink>
        </p>
      </div>
    </section>
  </main>
</template>
