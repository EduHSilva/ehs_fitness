<script setup lang="ts">
import { useStudents } from '~/features/students/useStudents'
import { domainShortLabel } from '~/features/students/types'

// Seletor do aluno consultado nas telas de plano e acompanhamento. O profissional
// alterna entre os alunos vinculados e o próprio plano ("Eu mesmo", padrão).
const route = useRoute()
const { isProfessional } = useSession()
const { students, load } = useStudents()
const loading = ref(false)
const selected = computed(() => typeof route.query.studentId === 'string' ? route.query.studentId : '')

onMounted(async () => {
  if (!isProfessional.value) return
  loading.value = true
  try {
    await load()
  } finally {
    loading.value = false
  }
})

function change(event: Event) {
  const value = (event.target as HTMLSelectElement).value
  const student = students.value.find(item => item.student_id === value)
  if (!student?.student_id) {
    void navigateTo({ path: route.path, query: {} })
    return
  }
  void navigateTo({
    path: route.path,
    query: {
      studentId: student.student_id,
      domain: student.domain,
      linkId: String(student.link_id),
      studentName: student.name
    }
  })
}
</script>

<template>
  <div
    v-if="isProfessional && (loading || students.length)"
    class="student-switcher"
  >
    <label>
      <UIcon name="i-lucide-users-round" />
      <span>Aluno</span>
      <select
        :value="selected"
        :disabled="loading"
        aria-label="Selecionar aluno"
        @change="change"
      >
        <option value="">
          Eu mesmo
        </option>
        <option
          v-for="student in students"
          :key="student.link_id"
          :value="student.student_id"
        >
          {{ student.name }} · {{ domainShortLabel(student.domain) }}
        </option>
      </select>
    </label>
  </div>
</template>
