export type TrackingConfig = {
  id?: number
  showCreatine: boolean
  dailyCardio: number
  showWorkout: boolean
}

export type TrackingInput = {
  showCreatine: boolean
  dailyCardio: number
  showWorkout: boolean
}

/** Registro diário do aluno (`eventDate` no formato YYYY-MM-DD). */
export type TrackingDay = {
  id?: number
  eventDate: string
  creatine: boolean
  cardio: number
  trained: boolean
}
