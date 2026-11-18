import { describe, expect, it } from 'vitest'
import { canWriteDomain, isProfessional, professionalDomains } from '~/composables/useSession'
import type { SessionUser } from '~/composables/useSession'

const student: SessionUser = { id: '1', roles: ['HEALTH_USER'] }
const personal: SessionUser = { id: '2', roles: ['HEALTH_USER', 'HEALTH_PERSONAL'] }
const nutritionist: SessionUser = { id: '3', roles: ['HEALTH_NUTRITIONIST'] }

describe('permissões da sessão', () => {
  it('reconhece contas profissionais', () => {
    expect(isProfessional(student)).toBe(false)
    expect(isProfessional(personal)).toBe(true)
    expect(isProfessional(nutritionist)).toBe(true)
  })

  it('lista os domínios das roles', () => {
    expect(professionalDomains(personal)).toEqual(['PERSONAL'])
    expect(professionalDomains(nutritionist)).toEqual(['NUTRITIONIST'])
    expect(professionalDomains({ id: '4', roles: ['HEALTH_PERSONAL', 'HEALTH_NUTRITIONIST'] })).toEqual(['PERSONAL', 'NUTRITIONIST'])
  })

  it('só escreve no domínio da role', () => {
    expect(canWriteDomain(personal, 'PERSONAL')).toBe(true)
    expect(canWriteDomain(personal, 'NUTRITIONIST')).toBe(false)
    expect(canWriteDomain(nutritionist, 'NUTRITIONIST')).toBe(true)
    expect(canWriteDomain(student, 'PERSONAL')).toBe(false)
  })
})
