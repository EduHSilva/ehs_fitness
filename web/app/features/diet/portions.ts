import type { Portion } from './types'

export type PortionShortcut = {
  count: number
  grams: number
  label: string
}

export function hasPortion(food: Portion) {
  return Boolean(food.portion_label && typeof food.portion_grams === 'number' && food.portion_grams > 0)
}

function pluralLabel(label: string) {
  const parts = label.split(' de ')
  const head = parts[0] ?? label
  const pluralHead = /[rzs]$/.test(head) ? `${head}es` : `${head}s`
  return [pluralHead, ...parts.slice(1)].join(' de ')
}

export function portionHint(food: Portion) {
  const label = food.portion_label ?? ''
  const grams = food.portion_grams ?? 0
  if (!label || grams <= 0) return ''
  return `1 ${label} ≈ ${grams} g`
}

export function portionShortcuts(food: Portion, max = 3): PortionShortcut[] {
  if (!hasPortion(food)) return []
  const grams = food.portion_grams ?? 0
  const label = food.portion_label ?? ''
  return Array.from({ length: max }, (_, index) => {
    const count = index + 1
    return {
      count,
      grams: Math.round(count * grams),
      label: count === 1 ? `1 ${label}` : `${count} ${pluralLabel(label)}`
    }
  })
}
