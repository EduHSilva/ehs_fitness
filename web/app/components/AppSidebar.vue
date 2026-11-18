<script setup lang="ts">
defineProps<{ active: 'home' | 'diet' | 'workouts' | 'agenda' | 'tracking' | 'students' }>()

const router = useRouter()
const { isProfessional, clear } = useSession()

function logout() {
  clear()
  router.push('/login')
}
</script>

<template>
  <aside class="app-sidebar">
    <NuxtLink
      to="/home"
      class="brand app-brand"
    ><span class="brand-mark"><UIcon name="i-lucide-activity" /></span><span><b>Fitness</b></span></NuxtLink>
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
        to="/agenda"
        :class="{ active: active === 'agenda' }"
      ><UIcon name="i-lucide-calendar-days" /> Agenda</NuxtLink>
      <NuxtLink
        to="/tracking"
        :class="{ active: active === 'tracking' }"
      ><UIcon name="i-lucide-heart-pulse" /> Acompanhamento</NuxtLink>
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
      <p class="sidebar-credit">
        Desenvolvido por EHS Solutions
      </p>
    </div>
  </aside>
</template>
