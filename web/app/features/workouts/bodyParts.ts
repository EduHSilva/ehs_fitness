const BODY_PART_LABELS: Record<string, string> = {
  'upper arms': 'Braços',
  'upper legs': 'Pernas',
  'lower arms': 'Antebraços',
  'lower legs': 'Panturrilhas',
  'back': 'Costas',
  'waist': 'Abdômen',
  'chest': 'Peito',
  'shoulders': 'Ombros',
  'cardio': 'Cardio',
  'neck': 'Pescoço'
}

const BODY_PART_ORDER = ['chest', 'back', 'upper legs', 'lower legs', 'shoulders', 'upper arms', 'lower arms', 'waist', 'cardio', 'neck']

export const BODY_PART_OPTIONS = BODY_PART_ORDER.map(value => ({ value, label: BODY_PART_LABELS[value] }))

export function bodyPartLabel(bodyPart?: string) {
  const value = bodyPart?.trim()
  if (!value) return ''
  return BODY_PART_LABELS[value.toLocaleLowerCase('pt-BR')] ?? value
}
