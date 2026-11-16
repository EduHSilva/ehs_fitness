export type SessionRole = 'HEALTH_USER' | 'HEALTH_PERSONAL' | 'HEALTH_NUTRITIONIST' | 'ADMIN'
export type ProfessionalDomain = 'PERSONAL' | 'NUTRITIONIST'

export type SessionUser = {
  id?: string
  name?: string
  username?: string
  email?: string
  photo?: string
  roles?: string[]
  apps?: string[]
}

const TOKEN_KEY = 'user_token'
const USER_KEY = 'user'

export function readSessionToken(): string {
  if (!import.meta.client) return ''
  return localStorage.getItem(TOKEN_KEY) ?? ''
}

export function readSessionUser(): SessionUser | null {
  if (!import.meta.client) return null
  const stored = localStorage.getItem(USER_KEY)
  if (!stored) return null
  try {
    return JSON.parse(stored) as SessionUser
  } catch {
    return null
  }
}

export function saveSession(token: string, user: SessionUser, keepConnected = true) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
  if (keepConnected) sessionStorage.removeItem('session_only')
  else sessionStorage.setItem('session_only', 'true')
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  sessionStorage.removeItem('session_only')
}

export function sessionName(user: SessionUser | null): string {
  return user?.name || user?.username || ''
}

export function sessionRoles(user: SessionUser | null): string[] {
  return Array.isArray(user?.roles) ? user.roles.map(String) : []
}

export function hasRole(user: SessionUser | null, role: SessionRole): boolean {
  return sessionRoles(user).includes(role)
}

export function isProfessional(user: SessionUser | null): boolean {
  return hasRole(user, 'HEALTH_PERSONAL') || hasRole(user, 'HEALTH_NUTRITIONIST')
}

export function professionalDomains(user: SessionUser | null): ProfessionalDomain[] {
  const domains: ProfessionalDomain[] = []
  if (hasRole(user, 'HEALTH_PERSONAL')) domains.push('PERSONAL')
  if (hasRole(user, 'HEALTH_NUTRITIONIST')) domains.push('NUTRITIONIST')
  return domains
}

export function canWriteDomain(user: SessionUser | null, domain: ProfessionalDomain): boolean {
  return domain === 'PERSONAL' ? hasRole(user, 'HEALTH_PERSONAL') : hasRole(user, 'HEALTH_NUTRITIONIST')
}

export function useSession() {
  const user = ref<SessionUser | null>(readSessionUser())
  const token = ref(readSessionToken())
  const professional = computed(() => isProfessional(user.value))
  const domains = computed(() => professionalDomains(user.value))

  function refresh() {
    user.value = readSessionUser()
    token.value = readSessionToken()
  }

  function write(nextToken: string, nextUser: SessionUser, keepConnected = true) {
    saveSession(nextToken, nextUser, keepConnected)
    refresh()
  }

  function clear() {
    clearSession()
    refresh()
  }

  return { user, token, isProfessional: professional, domains, refresh, write, clear }
}
