package com.ehs.fitness.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehs.fitness.core.links.openExternal
import com.ehs.fitness.core.links.privacyPolicyUrl
import com.ehs.fitness.core.settings.AppTheme
import com.ehs.fitness.core.settings.LocalAppSettings
import com.ehs.fitness.core.ui.components.ContentCard
import com.ehs.fitness.core.ui.components.ErrorBanner
import com.ehs.fitness.core.ui.components.FieldLabel
import com.ehs.fitness.core.ui.components.InitialAvatar
import com.ehs.fitness.core.ui.components.IntroBanner
import com.ehs.fitness.core.ui.components.PrimaryButton
import com.ehs.fitness.core.ui.components.ScreenDivider
import com.ehs.fitness.core.ui.components.Tag
import com.ehs.fitness.core.ui.theme.FitnessTheme
import com.ehs.fitness.ui.LocalContainer
import com.ehs.fitness.ui.appViewModel

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val container = LocalContainer.current
    val vm: ProfileViewModel = appViewModel { ProfileViewModel(it.authRepository) }
    val state by vm.state.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val settings = LocalAppSettings.current
    val theme by settings.theme.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val user = session?.user
    var name by rememberSaveable(user?.name) { mutableStateOf(user?.name.orEmpty()) }
    val dark = theme == AppTheme.DARK

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            IntroBanner(
                eyebrow = "Sua conta",
                title = "Perfil",
                description = "Ajuste seus dados e as preferências do aplicativo.",
                icon = Icons.Filled.Person,
            )
        }

        item {
            ContentCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    InitialAvatar(name = user?.name.orEmpty(), size = 64.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(user?.name.orEmpty(), style = MaterialTheme.typography.titleLarge)
                        Text(
                            user?.email.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(2.dp))
                        Tag(text = user?.roleLabel.orEmpty())
                    }
                }
            }
        }

        item {
            ContentCard {
                FieldLabel("Nome")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (state.error != null || state.saved) vm.clearFeedback()
                    },
                    placeholder = { Text("Como podemos chamar você?") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(14.dp))
                FieldLabel("E-mail")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = user?.email.orEmpty(),
                    onValueChange = {},
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                state.error?.let { message ->
                    Spacer(Modifier.height(14.dp))
                    ErrorBanner(message = message)
                }
                if (state.saved) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Nome atualizado com sucesso.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FitnessTheme.colors.success,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FitnessTheme.colors.successSoft, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }

                Spacer(Modifier.height(18.dp))
                PrimaryButton(
                    text = if (state.pending) "Salvando..." else "Salvar alterações",
                    onClick = { vm.updateName(name) },
                    loading = state.pending,
                )
            }
        }

        item {
            ContentCard {
                Text("Preferências", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            if (dark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Column {
                            Text("Aparência", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (dark) "Tema escuro" else "Tema claro",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = settings::toggleTheme,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Alternar", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(Modifier.height(12.dp))
                ScreenDivider()
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { openExternal(context, privacyPolicyUrl) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.PrivacyTip, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Política de Privacidade", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(9.dp),
                border = BorderStroke(1.dp, FitnessTheme.colors.danger.copy(alpha = 0.4f)),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = FitnessTheme.colors.danger,
                ),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sair da conta", fontWeight = FontWeight.SemiBold)
            }
        }

        item {
            Text(
                "EHS Fitness v${container.appVersion}",
                style = MaterialTheme.typography.labelSmall,
                color = FitnessTheme.colors.subtle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }
    }
}
