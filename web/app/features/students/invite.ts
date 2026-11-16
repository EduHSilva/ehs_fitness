const INVITE_KEY = 'fitness_invite_token'

export function storeInviteToken(value: unknown) {
  if (!import.meta.client || typeof value !== 'string') return
  const token = value.trim()
  if (token) sessionStorage.setItem(INVITE_KEY, token)
}

export function readStoredInviteToken(): string {
  if (!import.meta.client) return ''
  return sessionStorage.getItem(INVITE_KEY) ?? ''
}

export function clearStoredInviteToken() {
  if (import.meta.client) sessionStorage.removeItem(INVITE_KEY)
}

/** Mantém o token do convite entre o link do e-mail, login e a home. */
export function inviteQuery(value: unknown): Record<string, string> {
  return typeof value === 'string' && value.trim() ? { invite: value.trim() } : {}
}
