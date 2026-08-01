export function useFitnessApi() {
  async function request<T>(path: string, options: Parameters<typeof $fetch>[1] = {}): Promise<T> {
    const token = localStorage.getItem('user_token')
    if (!token) {
      await navigateTo('/login')
      throw new Error('Sessao expirada.')
    }

    try {
      return await $fetch<T>(`/api${path}`, {
        ...options,
        headers: { ...options.headers, Authorization: `Bearer ${token}` }
      })
    } catch (error: unknown) {
      const status = (error as { statusCode?: number, response?: { status?: number } }).statusCode
        ?? (error as { response?: { status?: number } }).response?.status
      if (status === 401) {
        localStorage.removeItem('user_token')
        localStorage.removeItem('user')
        await navigateTo('/login')
      }
      throw error
    }
  }

  return { request }
}
