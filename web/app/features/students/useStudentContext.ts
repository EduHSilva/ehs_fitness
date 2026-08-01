export function useStudentContext() {
  const route = useRoute()
  const studentId = computed(() => typeof route.query.studentId === 'string' ? route.query.studentId : '')
  // TODO: carregar o nome do aluno pelo endpoint de detalhe ao abrir um plano gerenciado.
  const studentName = ref('')

  return { studentId, studentName }
}
