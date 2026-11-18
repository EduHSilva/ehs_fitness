<script setup lang="ts">
import { errorMessage } from '~/types/api'
import { changePassword, updateAccount } from '~/features/profile/profile.api'
import { domainLabel } from '~/features/students/types'

const router = useRouter()
const { request } = useFitnessApi()
const { user, token, domains, write, clear } = useSession()
const colorMode = useColorMode()

const name = ref('')
const savingName = ref(false)
const nameFeedback = ref('')
const nameError = ref('')

const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const savingPassword = ref(false)
const passwordFeedback = ref('')
const passwordError = ref('')

const themes = [
  { value: 'light', label: 'Claro', icon: 'i-lucide-sun' },
  { value: 'dark', label: 'Escuro', icon: 'i-lucide-moon' },
  { value: 'system', label: 'Sistema', icon: 'i-lucide-monitor' }
] as const

const displayName = computed(() => user.value?.name || user.value?.username || '')
const initials = computed(() => displayName.value.slice(0, 1).toUpperCase() || '?')
const rolesLabel = computed(() => (user.value?.roles ?? [])
  .map(role => role === 'HEALTH_USER' ? 'Aluno' : role === 'HEALTH_PERSONAL' ? 'Personal trainer' : role === 'HEALTH_NUTRITIONIST' ? 'Nutricionista' : role)
  .join(' · '))
const domainsLabel = computed(() => domains.value.map(domainLabel).join(' + '))

function setTheme(value: 'light' | 'dark' | 'system') {
  colorMode.preference = value
}

function logout() {
  clear()
  router.push('/login')
}

async function saveName() {
  nameError.value = ''
  nameFeedback.value = ''
  const username = name.value.trim()
  if (!username) {
    nameError.value = 'Informe seu nome.'
    return
  }
  if (!user.value?.id) {
    nameError.value = 'Sessão sem identificador da conta.'
    return
  }
  savingName.value = true
  try {
    const updated = (await updateAccount(request, user.value.id, username)).data
    if (!updated) throw new Error('Conta não retornada pela API.')
    write(token.value, { ...user.value, name: updated.username, username: updated.username, email: updated.email })
    name.value = updated.username
    nameFeedback.value = 'Nome atualizado.'
  } catch (cause) {
    nameError.value = errorMessage(cause, 'Não foi possível atualizar o nome.')
  } finally {
    savingName.value = false
  }
}

async function savePassword() {
  passwordError.value = ''
  passwordFeedback.value = ''
  if (!currentPassword.value) return void (passwordError.value = 'Informe a senha atual.')
  if (newPassword.value.length < 8) return void (passwordError.value = 'A nova senha deve ter ao menos 8 caracteres.')
  if (newPassword.value !== confirmPassword.value) return void (passwordError.value = 'A confirmação não confere.')
  savingPassword.value = true
  try {
    const account = (await changePassword(request, currentPassword.value, newPassword.value)).data
    if (account?.token && user.value) {
      write(account.token, {
        ...user.value,
        name: account.username,
        username: account.username,
        email: account.email
      })
    }
    currentPassword.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
    passwordFeedback.value = 'Senha alterada. As outras sessões foram encerradas.'
  } catch (cause) {
    passwordError.value = errorMessage(cause, 'Não foi possível alterar a senha.')
  } finally {
    savingPassword.value = false
  }
}

onMounted(() => {
  if (!user.value) return router.replace('/login')
  name.value = displayName.value
})

useSeoMeta({ title: 'Configurações' })
</script>

<template>
  <div class="app-shell">
    <AppSidebar active="profile" />
    <main class="app-main module-page">
      <header class="app-topbar">
        <div><p>SUA CONTA</p><h1>Configurações</h1></div>
        <div class="topbar-actions">
          <button
            class="secondary-button"
            type="button"
            @click="logout"
          >
            <UIcon name="i-lucide-log-out" /> Sair
          </button>
        </div>
      </header>

      <section class="module-intro">
        <div>
          <p class="eyebrow">
            <span /> conta e preferências
          </p>
          <h2>Seus dados e aparência</h2>
          <p>Atualize o nome, troque a senha e escolha o tema da interface.</p>
        </div>
        <UIcon name="i-lucide-settings-2" />
      </section>

      <section class="content-grid">
        <div>
          <article class="registration-card">
            <div class="registration-heading">
              <div>
                <p class="eyebrow">
                  <span /> perfil
                </p>
                <h2>Dados da conta</h2>
              </div>
            </div>
            <div class="student-title">
              <span class="student-avatar">{{ initials }}</span>
              <div>
                <h2>{{ displayName }}</h2>
                <p>{{ user?.email }}</p>
              </div>
            </div>
            <p
              v-if="rolesLabel || domainsLabel"
              class="skills-hint"
            >
              {{ rolesLabel }}<template v-if="domainsLabel">
                · {{ domainsLabel }}
              </template>
            </p>
            <form
              class="student-form"
              @submit.prevent="saveName"
            >
              <label>Nome<input
                v-model="name"
                maxlength="80"
                placeholder="Seu nome"
              ></label>
              <button
                class="primary-button"
                type="submit"
                :disabled="savingName"
              >
                {{ savingName ? 'Salvando...' : 'Salvar nome' }}
              </button>
            </form>
            <p
              v-if="nameFeedback"
              class="notice-banner"
              role="status"
            >
              <UIcon name="i-lucide-circle-check" /> {{ nameFeedback }}
            </p>
            <p
              v-if="nameError"
              class="form-error form-error-left"
              role="alert"
            >
              {{ nameError }}
            </p>
          </article>

          <article class="registration-card">
            <div class="registration-heading">
              <div>
                <p class="eyebrow">
                  <span /> segurança
                </p>
                <h2>Alterar senha</h2>
              </div>
            </div>
            <form
              class="student-form"
              @submit.prevent="savePassword"
            >
              <label>Senha atual<input
                v-model="currentPassword"
                type="password"
                autocomplete="current-password"
              ></label>
              <label>Nova senha<input
                v-model="newPassword"
                type="password"
                autocomplete="new-password"
              ></label>
              <label>Confirmar nova senha<input
                v-model="confirmPassword"
                type="password"
                autocomplete="new-password"
              ></label>
              <button
                class="primary-button"
                type="submit"
                :disabled="savingPassword"
              >
                {{ savingPassword ? 'Alterando...' : 'Alterar senha' }}
              </button>
            </form>
            <p
              v-if="passwordFeedback"
              class="notice-banner"
              role="status"
            >
              <UIcon name="i-lucide-circle-check" /> {{ passwordFeedback }}
            </p>
            <p
              v-if="passwordError"
              class="form-error form-error-left"
              role="alert"
            >
              {{ passwordError }}
            </p>
          </article>
        </div>

        <aside class="weekly-card">
          <div class="section-heading">
            <div>
              <p>APARÊNCIA</p>
              <h2>Tema</h2>
            </div>
          </div>
          <div class="theme-options">
            <button
              v-for="theme in themes"
              :key="theme.value"
              type="button"
              class="theme-option"
              :class="{ active: colorMode.preference === theme.value }"
              :aria-pressed="colorMode.preference === theme.value"
              @click="setTheme(theme.value)"
            >
              <UIcon :name="theme.icon" /> {{ theme.label }}
            </button>
          </div>
          <p class="skills-hint">
            O tema fica salvo neste navegador e também pode seguir a preferência do sistema.
          </p>
        </aside>
      </section>
    </main>
  </div>
</template>
