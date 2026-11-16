// https://nuxt.com/docs/api/configuration/nuxt-config

type Env = Record<string, string | undefined>
const env = (globalThis as unknown as { process?: { env?: Env } }).process?.env ?? {}

const appBaseURL = env.NUXT_APP_BASE_URL || '/'
const googleClientId = env.NUXT_PUBLIC_GOOGLE_CLIENT_ID

export default defineNuxtConfig({
  modules: [
    '@nuxt/eslint',
    '@nuxt/ui'
  ],

  ssr: false,

  devtools: {
    enabled: true
  },

  app: {
    baseURL: appBaseURL
  },

  css: ['~/assets/css/main.css'],

  runtimeConfig: {
    apiBase: env.URL_API || 'http://localhost/',
    public: {
      apiUrl: env.URL_API || 'http://localhost/',
      googleClientId
    }
  },
  routeRules: {
    '/': { prerender: true }
  },

  compatibilityDate: '2026-06-30',

  eslint: {
    config: {
      stylistic: {
        commaDangle: 'never',
        braceStyle: '1tbs'
      }
    }
  }
})
