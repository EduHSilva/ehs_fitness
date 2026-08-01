import type { ApiRequest, ApiResponse } from '~/types/api'
import type { Student } from './types'

// TODO: confirmar os endpoints e o contrato de alunos com o backend.
export function fetchStudents(request: ApiRequest) {
  return request<ApiResponse<Student[]>>('/fitness/students')
}

// TODO: confirmar se o aluno deve ser associado ao profissional no payload ou pelo token.
export function createStudent(request: ApiRequest, student: Omit<Student, 'id'>) {
  return request<ApiResponse<Student>>('/fitness/students', { method: 'POST', body: student })
}
