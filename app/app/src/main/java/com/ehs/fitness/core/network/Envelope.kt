package com.ehs.fitness.core.network

import kotlinx.serialization.Serializable

/** Resposta padrão da API: todo payload chega embrulhado em `DefaultResponse`. */
@Serializable
data class Envelope<T>(
    val message: String? = null,
    val data: T? = null,
    val links: Map<String, String>? = null,
)

class ApiException(
    override val message: String,
    val status: Int? = null,
) : Exception(message)

/**
 * `login` responde 202 (envelope sem `data`) quando a conta existe mas ainda
 * não tem acesso ao app: o e-mail de confirmação foi enviado, não é erro.
 */
class EmailConfirmationRequiredException(message: String) : Exception(message)

fun <T> Envelope<T>.unwrap(): T = data ?: throw ApiException(message ?: "Resposta inesperada da API")

/**
 * Listagens chegam com `data: null` quando não há registros (o back usa `nil` em vez de
 * lista vazia). Tratar como ausência de itens evita transformar "nada cadastrado" em erro.
 */
fun <T> Envelope<List<T>>.unwrapList(): List<T> = data.orEmpty()
