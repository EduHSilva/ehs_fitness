import type { ApiResponse } from '~/types/api'

export type AccountRole = 'student' | 'personal_trainer' | 'nutritionist'
export type RegisterInput = { name: string, email: string, role: AccountRole }
export type AuthSession = { token: string, user: Record<string, unknown> }

// TODO: confirmar rota, payload e retorno do cadastro com o backend.
export function registerAccount(input: RegisterInput) {
  return $fetch<ApiResponse<AuthSession>>('/api/users/auth/register', {
    method: 'POST',
    body: input
  })
}
