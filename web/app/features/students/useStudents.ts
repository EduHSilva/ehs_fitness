import { errorMessage } from '~/types/api'
import { fetchStudents, inviteStudent, resendInvite, revokeInvite, endStudentLink, updateStudentGoal } from './student.api'
import type { InviteCard, ManagedModule, StudentCard, StudentDomain } from './types'

export function useStudents() {
  const { request } = useFitnessApi()
  const { isProfessional, domains: availableDomains } = useSession()
  const students = ref<StudentCard[]>([])
  const invites = ref<InviteCard[]>([])
  const loading = ref(false)
  const error = ref('')
  const actionError = ref('')
  const saving = ref(false)
  const busyLinkId = ref<number | null>(null)
  const busyInviteId = ref<number | null>(null)
  const feedback = ref('')

  function canManage(domain: StudentDomain) {
    return availableDomains.value.includes(domain)
  }

  async function load() {
    loading.value = true
    error.value = ''
    try {
      if (!isProfessional.value) {
        students.value = []
        invites.value = []
        return
      }
      const domain = availableDomains.value.length === 1 ? availableDomains.value[0] : undefined
      const response = (await fetchStudents(request, domain)).data
      students.value = response?.students ?? []
      invites.value = (response?.invites ?? []).filter(invite => availableDomains.value.includes(invite.domain))
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel carregar os alunos.')
    } finally {
      loading.value = false
    }
  }

  async function invite(input: { email: string, domain: StudentDomain, goal: string }) {
    if (saving.value) return false
    actionError.value = ''
    feedback.value = ''
    saving.value = true
    try {
      const created = (await inviteStudent(request, input)).data
      if (created) invites.value.unshift(created)
      feedback.value = `Convite enviado para ${input.email}.`
      return true
    } catch (cause) {
      actionError.value = errorMessage(cause, 'Nao foi possivel enviar o convite.')
      return false
    } finally {
      saving.value = false
    }
  }

  async function resend(id: number) {
    if (busyInviteId.value) return
    actionError.value = ''
    feedback.value = ''
    busyInviteId.value = id
    try {
      const updated = (await resendInvite(request, id)).data
      if (updated) invites.value = invites.value.map(item => item.id === id ? updated : item)
      feedback.value = 'Convite reenviado.'
    } catch (cause) {
      actionError.value = errorMessage(cause, 'Nao foi possivel reenviar o convite.')
    } finally {
      busyInviteId.value = null
    }
  }

  async function revoke(id: number) {
    if (busyInviteId.value) return
    actionError.value = ''
    feedback.value = ''
    busyInviteId.value = id
    try {
      await revokeInvite(request, id)
      invites.value = invites.value.filter(item => item.id !== id)
      feedback.value = 'Convite revogado.'
    } catch (cause) {
      actionError.value = errorMessage(cause, 'Nao foi possivel revogar o convite.')
    } finally {
      busyInviteId.value = null
    }
  }

  async function saveGoal(linkId: number, goal: string) {
    if (busyLinkId.value) return
    actionError.value = ''
    feedback.value = ''
    busyLinkId.value = linkId
    try {
      const updated = (await updateStudentGoal(request, linkId, goal)).data
      if (updated) students.value = students.value.map(item => item.link_id === linkId ? updated : item)
      feedback.value = 'Objetivo atualizado.'
    } catch (cause) {
      actionError.value = errorMessage(cause, 'Nao foi possivel atualizar o objetivo.')
    } finally {
      busyLinkId.value = null
    }
  }

  async function endLink(linkId: number) {
    if (busyLinkId.value) return
    actionError.value = ''
    feedback.value = ''
    busyLinkId.value = linkId
    try {
      await endStudentLink(request, linkId)
      students.value = students.value.filter(item => item.link_id !== linkId)
      feedback.value = 'Vinculo encerrado. O aluno recupera a gestao do plano.'
    } catch (cause) {
      actionError.value = errorMessage(cause, 'Nao foi possivel encerrar o vinculo.')
    } finally {
      busyLinkId.value = null
    }
  }

  function manage(student: StudentCard, destination: ManagedModule) {
    if (!student.student_id) {
      actionError.value = 'O aluno ainda nao aceitou o convite.'
      return
    }
    return navigateTo({
      path: `/${destination}`,
      query: {
        studentId: student.student_id,
        domain: student.domain,
        linkId: String(student.link_id),
        studentName: student.name
      }
    })
  }

  return {
    students,
    invites,
    loading,
    error,
    actionError,
    saving,
    busyLinkId,
    busyInviteId,
    feedback,
    isProfessional,
    availableDomains,
    canManage,
    load,
    invite,
    resend,
    revoke,
    saveGoal,
    endLink,
    manage
  }
}
