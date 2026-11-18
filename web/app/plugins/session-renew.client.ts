import { readSessionToken, readSessionUser, saveSession } from '~/composables/useSession'

type SessionPayload = {
  token?: string
}

/**
 * Renova o token ao abrir o site (expiração deslizante). Falha de rede ou 401 não
 * fazem nada aqui: a próxima chamada da API trata a sessão.
 */
export default defineNuxtPlugin(() => {
  const token = readSessionToken()
  if (!token) return

  void (async () => {
    try {
      const response = await $fetch<{ data?: SessionPayload }>('/api/users/auth/me', {
        headers: { Authorization: `Bearer ${token}` }
      })
      const fresh = response.data?.token
      if (!fresh) return
      const keepConnected = sessionStorage.getItem('session_only') !== 'true'
      saveSession(fresh, readSessionUser() ?? {}, keepConnected)
    } catch {
      /* mantém a sessão salva até a API responder 401 */
    }
  })()
})
