<script setup lang="ts">
defineProps<{ active: 'home' | 'diet' | 'workouts' | 'habits' | 'students' }>()

const router = useRouter()
const isProfessional = computed(() => {
  if (!import.meta.client) return false
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const roles = Array.isArray(user.roles) ? user.roles.map(String) : []
    return roles.includes('HEALTH_PERSONAL') || roles.includes('HEALTH_NUTRITIONIST')
  } catch {
    return false
  }
})

function logout() {
  localStorage.removeItem('user_token')
  localStorage.removeItem('user')
  sessionStorage.removeItem('session_only')
  router.push('/login')
}
</script>

<template>
  <aside class="app-sidebar">
    <NuxtLink
      to="/home"
      class="brand app-brand"
    ><span class="brand-mark"><UIcon name="i-lucide-activity" /></span><span>EHS <b>Fitness</b></span></NuxtLink>
    <nav
      class="app-nav"
      aria-label="Navegacao do aplicativo"
    >
      <NuxtLink
        to="/home"
        :class="{ active: active === 'home' }"
      ><UIcon name="i-lucide-house" /> Inicio</NuxtLink>
      <NuxtLink
        to="/diet"
        :class="{ active: active === 'diet' }"
      ><UIcon name="i-lucide-utensils" /> Alimentacao</NuxtLink>
      <NuxtLink
        to="/workouts"
        :class="{ active: active === 'workouts' }"
      ><UIcon name="i-lucide-dumbbell" /> Treinos</NuxtLink>
      <NuxtLink
        to="/habits"
        :class="{ active: active === 'habits' }"
      ><UIcon name="i-lucide-list-checks" /> Habitos</NuxtLink>
      <NuxtLink
        v-if="isProfessional"
        to="/students"
        :class="{ active: active === 'students' }"
      ><UIcon name="i-lucide-users" /> Alunos</NuxtLink>
    </nav>
    <div class="sidebar-bottom">
      <button type="button">
        <UIcon name="i-lucide-settings-2" /> Configuracoes
      </button>
      <button
        type="button"
        @click="logout"
      >
        <UIcon name="i-lucide-log-out" /> Sair
      </button>
    </div>
  </aside>
</template>
