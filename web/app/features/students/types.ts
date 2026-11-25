export type StudentDomain = 'PERSONAL' | 'NUTRITIONIST'

export type StudentCard = {
  link_id: number
  student_id?: string
  name: string
  email: string
  goal: string
  domain: StudentDomain
  since?: string
}

export type InviteCard = {
  id: number
  email: string
  domain: StudentDomain
  goal: string
  status: string
  expires_at: string
  created_at: string
}

export type IncomingInviteCard = {
  id: number
  professional_id: string
  professional_name: string
  domain: StudentDomain
  goal: string
  expires_at: string
}

export type StudentsResponse = {
  students: StudentCard[]
  invites: InviteCard[]
}

/** Vínculo ativo visto pelo aluno. */
export type MyLinkCard = {
  link_id: number
  domain: StudentDomain
  professional_id: string
  professional_name: string
  goal: string
  since?: string
}

export type ManagedModule = 'workouts' | 'diet' | 'tracking' | 'history'

export function domainLabel(domain: StudentDomain): string {
  return domain === 'PERSONAL' ? 'Personal trainer' : 'Nutricionista'
}

export function domainShortLabel(domain: StudentDomain): string {
  return domain === 'PERSONAL' ? 'Treinos e agenda' : 'Dieta'
}
