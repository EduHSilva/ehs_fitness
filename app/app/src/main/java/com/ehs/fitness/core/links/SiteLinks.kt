package com.ehs.fitness.core.links

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ehs.fitness.BuildConfig

// As páginas institucionais são servidas pelo mesmo host da API (nginx), por isso
// derivam do baseUrl configurado no build.
fun siteUrl(path: String): String =
    BuildConfig.API_BASE_URL.trimEnd('/') + "/" + path.trimStart('/')

val privacyPolicyUrl: String get() = siteUrl("app/privacidade/")
val resetPasswordUrl: String get() = siteUrl("user/reset-password")

fun openExternal(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
