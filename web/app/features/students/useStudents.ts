import { errorMessage } from '~/types/api'
import { createStudent, fetchStudents } from './student.api'
import type { ManagedModule, Student } from './types'

export function useStudents() {
  const { request } = useFitnessApi()
  const students = ref<Student[]>([])
  const account = computed(() => {
    if (!import.meta.client) return {}
    try {
      return JSON.parse(localStorage.getItem('user') || '{}') as Record<string, unknown>
    } catch {
      return {}
    }
  })
  const isProfessional = computed(() => {
    const role = String(account.value.role || account.value.account_type || account.value.type || '').toLowerCase()
    return ['personal_trainer', 'nutritionist', 'professional', 'pj'].includes(role) || account.value.is_pj === true
  })
  const error = ref('')

  async function load() {
    error.value = ''
    try {
      students.value = (await fetchStudents(request)).data ?? []
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel carregar os alunos.')
    }
  }

  async function addStudent(student: Omit<Student, 'id'>) {
    error.value = ''
    try {
      const created = (await createStudent(request, student)).data
      if (created) students.value.push(created)
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel cadastrar o aluno.')
      throw cause
    }
  }

  function manage(studentId: string, destination: ManagedModule) {
    return navigateTo({ path: `/${destination}`, query: { studentId } })
  }

  return { students, isProfessional, error, load, addStudent, manage }
}
