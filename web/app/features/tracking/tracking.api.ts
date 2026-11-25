import type { ApiRequest, ApiResponse } from '~/types/api'
import type { TrackingConfig, TrackingDay, TrackingInput } from './types'

function studentQuery(studentId?: string) {
  return studentId ? `?student_id=${encodeURIComponent(studentId)}` : ''
}

export function fetchTrackingConfig(request: ApiRequest, studentId?: string) {
  return request<ApiResponse<TrackingConfig>>(`/fitness/config/user${studentQuery(studentId)}`)
}

export function fetchTrackingHistory(
  request: ApiRequest,
  input: { from: string, to: string, studentId?: string }
) {
  const params = new URLSearchParams({ from: input.from, to: input.to })
  if (input.studentId) params.set('student_id', input.studentId)
  return request<ApiResponse<TrackingDay[]>>(`/fitness/config/history?${params.toString()}`)
}

export function saveTrackingConfig(request: ApiRequest, input: TrackingInput, studentId?: string) {
  return request<ApiResponse<TrackingConfig>>(`/fitness/config/user${studentQuery(studentId)}`, {
    method: 'PUT',
    body: {
      showCreatine: input.showCreatine,
      dailyCardio: input.dailyCardio,
      showWorkout: input.showWorkout,
      ...(studentId ? { student_id: studentId } : {})
    }
  })
}
