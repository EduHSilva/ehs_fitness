export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const path = getRouterParam(event, 'path')
  if (!path) {
    throw createError({ statusCode: 404, statusMessage: 'API route not found' })
  }

  const requestUrl = getRequestURL(event)
  const upstreamUrl = `${String(config.apiBase).replace(/\/$/, '')}/${path}${requestUrl.search}`
  const authorization = getHeader(event, 'authorization')
  const contentType = getHeader(event, 'content-type')
  const acceptLanguage = getHeader(event, 'accept-language')
  const method = getMethod(event)
  const body = ['GET', 'HEAD'].includes(method)
    ? undefined
    : await readRawBody(event, false)

  const response = await fetch(upstreamUrl, {
    method,
    headers: {
      ...(authorization ? { authorization } : {}),
      ...(contentType ? { 'content-type': contentType } : {}),
      ...(acceptLanguage ? { 'accept-language': acceptLanguage } : {})
    },
    body
  })

  setResponseStatus(event, response.status)
  const responseContentType = response.headers.get('content-type')
  if (responseContentType) {
    setResponseHeader(event, 'content-type', responseContentType)
  }

  const responseBody = await response.text()
  if (!responseBody) return null
  try {
    return JSON.parse(responseBody)
  } catch {
    return responseBody
  }
})
