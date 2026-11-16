<script setup lang="ts">
const appBase = useRuntimeConfig().app.baseURL

const description = 'Planeje treinos, organize sua alimentação e acompanhe sua evolução em um só lugar.'

useHead({
  titleTemplate: title => (title ? `${title} · Fitness` : 'Fitness — Sua rotina em movimento'),
  meta: [
    { name: 'viewport', content: 'width=device-width, initial-scale=1' },
    { name: 'theme-color', content: '#4F46E5' },
    { name: 'apple-mobile-web-app-title', content: 'Fitness' },
    { name: 'description', content: description }
  ],
  link: [
    { rel: 'icon', type: 'image/svg+xml', href: `${appBase}favicon.svg` },
    { rel: 'icon', href: `${appBase}favicon.ico`, sizes: '32x32' },
    { rel: 'apple-touch-icon', href: `${appBase}apple-touch-icon.png` }
  ],
  htmlAttrs: { lang: 'pt-BR' }
})

useSeoMeta({
  ogTitle: 'Fitness — Sua rotina em movimento',
  ogDescription: description,
  ogLocale: 'pt_BR'
})
</script>

<template>
  <UApp>
    <NuxtPage />
  </UApp>
</template>
