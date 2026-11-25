import type { AccountRole } from './auth.api'
import { registerAccount } from './auth.api'

export function useRegistration() {
  const name = ref('')
  const email = ref('')
  const password = ref('')
  const role = ref<AccountRole>('HEALTH_USER')
  const errorMessage = ref('')
  const loading = ref(false)
  const confirmationOpen = ref(false)
  /** Mensagem do back: avisa que, se o e-mail já tem conta, é preciso entrar. */
  const confirmationMessage = ref('')

  async function register() {
    errorMessage.value = ''

    if (!name.value.trim() || !email.value.trim() || !password.value) {
      errorMessage.value = 'Preencha seu nome, e-mail e senha para continuar.'
      return
    }

    loading.value = true
    try {
      const response = await registerAccount({
        username: name.value.trim(),
        email: email.value.trim(),
        password: password.value,
        role: role.value
      })
      confirmationMessage.value = response.message || ''
      confirmationOpen.value = true
    } catch (cause: unknown) {
      const error = cause as { data?: { message?: string }, message?: string }
      errorMessage.value = error.data?.message ?? error.message ?? 'Não foi possível criar sua conta.'
    } finally {
      loading.value = false
    }
  }

  async function goToLogin() {
    confirmationOpen.value = false
    await navigateTo('/login')
  }

  return {
    name,
    email,
    password,
    role,
    errorMessage,
    loading,
    confirmationOpen,
    confirmationMessage,
    register,
    goToLogin
  }
}
