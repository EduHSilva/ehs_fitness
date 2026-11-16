import { errorMessage } from '~/types/api'
import { acceptInvite, declineInvite, fetchIncomingInvites } from './student.api'
import { clearStoredInviteToken } from './invite'
import type { IncomingInviteCard, InviteCard } from './types'

export function useIncomingInvites() {
  const { request } = useFitnessApi()
  const invites = ref<IncomingInviteCard[]>([])
  const loading = ref(false)
  const error = ref('')
  const pending = ref(false)
  const feedback = ref('')

  async function load() {
    loading.value = true
    error.value = ''
    try {
      invites.value = (await fetchIncomingInvites(request)).data ?? []
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel carregar seus convites.')
    } finally {
      loading.value = false
    }
  }

  async function accept(token: string): Promise<InviteCard | null> {
    if (pending.value) return null
    pending.value = true
    error.value = ''
    feedback.value = ''
    try {
      const card = (await acceptInvite(request, token)).data ?? null
      clearStoredInviteToken()
      feedback.value = 'Convite aceito. Seu plano agora e acompanhado pelo profissional.'
      await load()
      return card
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel aceitar o convite.')
      return null
    } finally {
      pending.value = false
    }
  }

  async function decline(token: string): Promise<InviteCard | null> {
    if (pending.value) return null
    pending.value = true
    error.value = ''
    feedback.value = ''
    try {
      const card = (await declineInvite(request, token)).data ?? null
      clearStoredInviteToken()
      feedback.value = 'Convite recusado.'
      await load()
      return card
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel recusar o convite.')
      return null
    } finally {
      pending.value = false
    }
  }

  return { invites, loading, error, pending, feedback, load, accept, decline }
}
