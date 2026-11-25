import { fetchStudent } from './student.api'
import { useManagedLinks } from './useManagedLinks'
import type { StudentDomain } from './types'

export function useStudentContext() {
  const route = useRoute()
  const { request } = useFitnessApi()
  const { user } = useSession()
  const managed = useManagedLinks()
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

  // Acompanhamento configurado por qualquer profissional do vínculo.
  const canManage = computed(() => {
    if (!studentId.value) return true
    if (!domain.value) return true
    return canWriteDomain(user.value, domain.value)
  })

  // Sem vínculo na URL o plano é o do próprio usuário: o gate vem de
  // GET /students/links, que lista os profissionais ativos de cada domínio.
  // Com vínculo, cada domínio exige o papel correspondente: o personal consulta
  // a dieta e o nutricionista consulta treinos/agenda, mas só o dono do domínio edita.
  function canManageOwn(target: StudentDomain): boolean {
    if (!studentId.value) return !managed.isManaged(target)
    return canWriteDomain(user.value, target)
  }

  function managerName(target: StudentDomain): string {
    return studentId.value ? '' : managed.managerFor(target)
  }

  async function loadContext() {
    const tasks: Promise<unknown>[] = [managed.load()]
    if (linkId.value) {
      tasks.push(fetchStudent(request, linkId.value)
        .then((response) => {
          if (response.data?.name) studentName.value = response.data.name
        })
        .catch(() => {
          // O nome na query já permite exibir o cabeçalho; o backend valida o vínculo.
        }))
    }
    await Promise.all(tasks)
  }

  return {
    studentId,
    linkId,
    domain,
    studentName,
    canManage,
    canManageWorkouts: computed(() => canManageOwn('PERSONAL')),
    canManageDiet: computed(() => canManageOwn('NUTRITIONIST')),
    managerName,
    loadContext
  }
}
