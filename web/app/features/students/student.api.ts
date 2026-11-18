import type { ApiRequest, ApiResponse } from '~/types/api'
import type {
  IncomingInviteCard,
  InviteCard,
  MyLinkCard,
  StudentCard,
  StudentDomain,
  StudentsResponse
} from './types'

export function fetchStudents(request: ApiRequest, domain?: StudentDomain) {
  const query = domain ? `?domain=${domain}` : ''
  return request<ApiResponse<StudentsResponse>>(`/fitness/students${query}`)
}

export function inviteStudent(request: ApiRequest, input: { email: string, domain: StudentDomain, goal: string }) {
  return request<ApiResponse<InviteCard>>('/fitness/students', { method: 'POST', body: input })
}

export function fetchStudent(request: ApiRequest, linkId: number) {
  return request<ApiResponse<StudentCard>>(`/fitness/students/${linkId}`)
}

export function updateStudentGoal(request: ApiRequest, linkId: number, goal: string) {
  return request<ApiResponse<StudentCard>>(`/fitness/students/links/${linkId}`, {
    method: 'PATCH',
    body: { goal }
  })
}

export function endStudentLink(request: ApiRequest, linkId: number) {
  return request<ApiResponse<StudentCard>>(`/fitness/students/links/${linkId}`, { method: 'DELETE' })
}

export function fetchSentInvites(request: ApiRequest) {
  return request<ApiResponse<InviteCard[]>>('/fitness/students/invites?direction=sent')
}

export function fetchIncomingInvites(request: ApiRequest) {
  return request<ApiResponse<IncomingInviteCard[]>>('/fitness/students/invites?direction=received')
}

export function fetchMyLinks(request: ApiRequest) {
  return request<ApiResponse<MyLinkCard[]>>('/fitness/students/links')
}

export function resendInvite(request: ApiRequest, id: number) {
  return request<ApiResponse<InviteCard>>(`/fitness/students/invites/${id}/resend`, { method: 'POST' })
}

export function revokeInvite(request: ApiRequest, id: number) {
  return request<ApiResponse<InviteCard>>(`/fitness/students/invites/${id}`, { method: 'DELETE' })
}

export function acceptInvite(request: ApiRequest, token: string) {
  return request<ApiResponse<InviteCard>>('/fitness/students/invites/accept', {
    method: 'POST',
    body: { token }
  })
}

export function declineInvite(request: ApiRequest, token: string) {
  return request<ApiResponse<InviteCard>>('/fitness/students/invites/decline', {
    method: 'POST',
    body: { token }
  })
}
