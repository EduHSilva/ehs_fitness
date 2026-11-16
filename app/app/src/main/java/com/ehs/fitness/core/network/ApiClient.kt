package com.ehs.fitness.core.network

import com.ehs.fitness.BuildConfig
import com.ehs.fitness.core.session.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP único. Injeta o Bearer token, pede os textos em português e derruba a
 * sessão quando a API responde 401 (o gate de autenticação volta para o login).
 *
 * O serviço de fitness escolhe o idioma pelo valor exato do header: ele compara com
 * `pt_BR`, então "pt-BR" faria os exercícios voltarem em inglês.
 */
class ApiClient(
    private val sessionManager: SessionManager,
    baseUrl: String = BuildConfig.API_BASE_URL,
) {
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = false
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sessionManager))
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                else HttpLoggingInterceptor.Level.NONE
            },
        )
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(normalizeBaseUrl(baseUrl))
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val builder = chain.request().newBuilder()
            sessionManager.token?.let { builder.header("Authorization", "Bearer $it") }
            builder.header("Accept-Language", "pt_BR")
            val response = chain.proceed(builder.build())
            if (response.code == 401) sessionManager.expire()
            return response
        }
    }

    internal companion object {
        fun normalizeBaseUrl(value: String): String {
            val trimmed = value.trim().ifBlank { "http://10.0.2.2/" }
            return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        }
    }
}
