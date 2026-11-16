package com.ehs.fitness.core.network

import java.io.IOException

/** Mensagem amigável para falhas de rede/API exibida nas telas. */
fun Throwable.userMessage(fallback: String): String = when (this) {
    is ApiException -> message.takeIf { it.isNotBlank() && it != "ok" } ?: fallback
    is IOException -> "Não foi possível conectar. Verifique sua internet e tente novamente."
    else -> fallback
}
