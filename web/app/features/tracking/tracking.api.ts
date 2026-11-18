import type { ApiRequest, ApiResponse } from '~/types/api'
import type { TrackingConfig, TrackingInput } from './types'

function studentQuery(studentId?: string) {
  return studentId ? `?student_id=${encodeURIComponent(studentId)}` : ''
}

export function fetchTrackingConfig(request: ApiRequest, studentId?: string) {
  return request<ApiResponse<TrackingConfig>>(`/fitness/config/user${studentQuery(studentId)}`)
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
