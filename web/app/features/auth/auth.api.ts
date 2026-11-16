import type { ApiResponse } from '~/types/api'

export type AccountRole = 'HEALTH_USER' | 'HEALTH_PERSONAL' | 'HEALTH_NUTRITIONIST'
export type RegisterInput = { username: string, email: string, password: string, role: AccountRole }
export type AuthSession = { token: string, user: Record<string, unknown> }

export function registerAccount(input: RegisterInput) {
  return $fetch<ApiResponse<AuthSession>>('/api/users/auth/register', {
    method: 'POST',
    body: input
  })
}
