import type { ApiRequest, ApiResponse } from '~/types/api'

export type AccountProfile = {
  id: string
  username: string
  email: string
  roles?: string[]
  companyName?: string
}

export type AccountSession = AccountProfile & {
  token: string
  apps?: string[]
}

export function updateAccount(request: ApiRequest, id: string, username: string) {
  return request<ApiResponse<AccountProfile>>(`/users/${id}`, {
    method: 'PUT',
    body: { username }
  })
}

export function changePassword(request: ApiRequest, currentPassword: string, newPassword: string) {
  return request<ApiResponse<AccountSession>>('/users/auth/password', {
    method: 'PUT',
    body: { currentPassword, newPassword }
  })
}
