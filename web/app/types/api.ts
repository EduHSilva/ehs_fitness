export type ApiResponse<T> = {
  data?: T
  message?: string
}

export type ApiRequest = <T>(path: string, options?: Parameters<typeof $fetch>[1]) => Promise<T>

export function errorMessage(error: unknown, fallback: string) {
  const apiError = error as { data?: { message?: string }, message?: string }
  return apiError.data?.message ?? apiError.message ?? fallback
}
