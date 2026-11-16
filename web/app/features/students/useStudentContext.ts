import { fetchStudent } from './student.api'
import type { StudentDomain } from './types'

export function useStudentContext() {
  const route = useRoute()
  const { request } = useFitnessApi()
  const { user } = useSession()
  const studentId = computed(() => typeof route.query.studentId === 'string' ? route.query.studentId : '')
  const linkId = computed(() => {
    const value = Number(route.query.linkId)
    return Number.isFinite(value) && value > 0 ? value : 0
  })
  const domain = computed<StudentDomain | ''>(() => {
    const value = route.query.domain
    return value === 'PERSONAL' || value === 'NUTRITIONIST' ? value : ''
  })
  const studentName = ref(typeof route.query.studentName === 'string' ? route.query.studentName : '')

  // Sem vínculo na URL o plano é o do próprio usuário; a API ainda bloqueia a
  // escrita quando existe profissional ativo no domínio (403).
  const canManage = computed(() => {
    if (!studentId.value) return true
    if (!domain.value) return true
    return canWriteDomain(user.value, domain.value)
  })

  async function loadContext() {
    if (!linkId.value) return
    try {
      const card = (await fetchStudent(request, linkId.value)).data
      if (card?.name) studentName.value = card.name
    } catch {
      // O nome na query já permite exibir o cabeçalho; o backend valida o vínculo.
    }
  }

  return { studentId, linkId, domain, studentName, canManage, loadContext }
}
