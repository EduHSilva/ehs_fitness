import { fetchMyLinks } from './student.api'
import type { MyLinkCard, StudentDomain } from './types'

/** Vínculos ativos do próprio usuário, usados para o gate somente leitura. */
export function useManagedLinks() {
  const { request } = useFitnessApi()
  const links = ref<MyLinkCard[]>([])
  let loaded = false
  let pending: Promise<void> | null = null

  async function load() {
    if (loaded) return
    if (pending) return pending
    pending = (async () => {
      try {
        links.value = (await fetchMyLinks(request)).data ?? []
      } catch {
        // Sem o endpoint o backend ainda bloqueia a escrita com 403.
        links.value = []
      } finally {
        loaded = true
        pending = null
      }
    })()
    return pending
  }

  function linkFor(domain: StudentDomain): MyLinkCard | null {
    return links.value.find(link => link.domain === domain) ?? null
  }

  function isManaged(domain: StudentDomain): boolean {
    return Boolean(linkFor(domain))
  }

  function managerFor(domain: StudentDomain): string {
    return linkFor(domain)?.professional_name ?? ''
  }

  return { links, load, linkFor, isManaged, managerFor }
}
