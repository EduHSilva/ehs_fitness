import { errorMessage } from '~/types/api'
import { fetchTrackingConfig, saveTrackingConfig } from './tracking.api'
import type { TrackingConfig } from './types'

export function useTracking(studentId: Ref<string>, canManage: Ref<boolean>) {
  const { request } = useFitnessApi()
  const loading = ref(true)
  const saving = ref(false)
  const error = ref('')
  const actionError = ref('')
  const feedback = ref('')
  const showCreatine = ref(true)
  const dailyCardio = ref(0)

  const readOnly = computed(() => !canManage.value)
  const cardioEnabled = computed({
    get: () => dailyCardio.value > 0,
    set: (enabled: boolean) => {
      if (!enabled) dailyCardio.value = 0
      else if (!(dailyCardio.value > 0)) dailyCardio.value = 30
    }
  })

  function apply(config: TrackingConfig) {
    showCreatine.value = config.showCreatine
    dailyCardio.value = config.dailyCardio || 0
  }

  async function load() {
    loading.value = true
    error.value = ''
    try {
      const config = (await fetchTrackingConfig(request, studentId.value || undefined)).data
      if (config) apply(config)
    } catch (cause) {
      error.value = errorMessage(cause, 'Nao foi possivel carregar o acompanhamento.')
    } finally {
      loading.value = false
    }
  }

  async function save() {
    if (readOnly.value || saving.value) return
    actionError.value = ''
    feedback.value = ''
    if (dailyCardio.value < 0 || dailyCardio.value > 600) {
      actionError.value = 'Informe uma meta de cardio entre 0 e 600 minutos.'
      return
    }
    saving.value = true
    try {
      const config = (await saveTrackingConfig(request, {
        showCreatine: showCreatine.value,
        dailyCardio: dailyCardio.value
      }, studentId.value || undefined)).data
      if (config) apply(config)
      feedback.value = 'Acompanhamento atualizado.'
    } catch (cause) {
      actionError.value = errorMessage(cause, 'Nao foi possivel salvar o acompanhamento.')
    } finally {
      saving.value = false
    }
  }

  return {
    loading,
    saving,
    error,
    actionError,
    feedback,
    showCreatine,
    dailyCardio,
    readOnly,
    cardioEnabled,
    load,
    save
  }
}
