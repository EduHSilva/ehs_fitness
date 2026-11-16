package com.ehs.fitness.feature.auth

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehs.fitness.core.links.openExternal
import com.ehs.fitness.core.links.privacyPolicyUrl
import com.ehs.fitness.core.links.resetPasswordUrl
import com.ehs.fitness.core.ui.components.ContentCard
import com.ehs.fitness.core.ui.components.ErrorBanner
import com.ehs.fitness.core.ui.components.PrimaryButton
import com.ehs.fitness.core.ui.components.ThemeToggleButton
import com.ehs.fitness.core.ui.theme.FitnessTheme
import com.ehs.fitness.ui.LocalContainer
import com.ehs.fitness.ui.appViewModel
import kotlinx.coroutines.launch

private data class RoleOption(val value: String, val label: String, val description: String, val icon: ImageVector)

private val roles = listOf(
    RoleOption("HEALTH_USER", "Aluno / praticante", "Organize seu próprio bem-estar.", Icons.Filled.FitnessCenter),
    RoleOption("HEALTH_PERSONAL", "Personal trainer", "Gerencie seus alunos e sua rotina.", Icons.Filled.Groups),
    RoleOption("HEALTH_NUTRITIONIST", "Nutricionista", "Crie planos para alunos e para você.", Icons.Filled.Restaurant),
)

@Composable
fun AuthScreen() {
    val container = LocalContainer.current
    val vm: AuthViewModel = appViewModel { AuthViewModel(it.authRepository) }
    val state by vm.state.collectAsStateWithLifecycle()

    var isRegister by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(roles.first().value) }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val activity = context as? Activity

    val submit = {
        focusManager.clearFocus()
        if (isRegister) vm.register(name, email, password, role) else vm.login(email, password)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandMark()
                ThemeToggleButton()
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "SUA SAÚDE, NO SEU RITMO",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitnessTheme.colors.secondary,
                )
                Text(
                    "Uma rotina mais leve.",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "Um você mais forte.",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Treinos, alimentação e evolução reunidos em um só lugar para transformar pequenos hábitos em grandes resultados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                BenefitLine("Treinos que cabem na sua rotina")
                BenefitLine("Alimentação sem complicação")
                BenefitLine("Evolução que você enxerga")
            }

            if (isRegister && state.registerSuccess) {
                RegisterConfirmation(
                    email = state.registeredEmail,
                    onGoToLogin = {
                        vm.resetRegisterSuccess()
                        isRegister = false
                        vm.clearErrors()
                    },
                )
            } else {
                ContentCard {
                    AuthTabs(
                        isRegister = isRegister,
                        onChange = { next ->
                            isRegister = next
                            showPassword = false
                            vm.clearErrors()
                        },
                    )

                    Spacer(Modifier.height(18.dp))
                    Text(
                        if (isRegister) "Crie sua conta" else "Que bom ter você de volta.",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        if (isRegister) "Leva menos de um minuto para começar."
                        else "Entre para continuar construindo sua melhor versão.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (isRegister) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Como podemos chamar você?") },
                            placeholder = { Text("Seu nome") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        placeholder = { Text("seu@email.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha") },
                        placeholder = { Text("Sua senha") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPassword) "Ocultar senha" else "Mostrar senha",
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (!isRegister) {
                        TextButton(
                            onClick = { openExternal(context, resetPasswordUrl) },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Esqueci minha senha", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (isRegister) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Qual é o seu objetivo na plataforma?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            roles.forEach { option ->
                                RoleCard(
                                    option = option,
                                    selected = role == option.value,
                                    onClick = { role = option.value },
                                )
                            }
                        }
                    }

                    val error = if (isRegister) state.registerError else state.error
                    if (error != null) {
                        Spacer(Modifier.height(14.dp))
                        ErrorBanner(message = error)
                    }

                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(
                        text = when {
                            isRegister && state.registerPending -> "Criando conta..."
                            isRegister -> "Criar conta"
                            state.pending -> "Entrando..."
                            else -> "Entrar na minha conta"
                        },
                        onClick = submit,
                        loading = if (isRegister) state.registerPending else state.pending,
                    )

                    if (!isRegister && container.googleLoginEnabled && activity != null) {
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(Modifier.weight(1f))
                            Text(
                                "ou",
                                modifier = Modifier.padding(horizontal = 12.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = FitnessTheme.colors.subtle,
                            )
                            HorizontalDivider(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val token = runCatching {
                                        GoogleSignIn.requestIdToken(activity, container.googleWebClientId)
                                    }.getOrNull()
                                    if (token != null) vm.loginWithGoogle(token) else vm.reportGoogleFailure()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(9.dp),
                        ) {
                            Text("G", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text("Continuar com o Google")
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            if (isRegister) "Já tem uma conta?" else "Ainda não tem uma conta?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(
                            onClick = {
                                isRegister = !isRegister
                                showPassword = false
                                vm.clearErrors()
                            },
                        ) {
                            Text(
                                if (isRegister) "Entrar" else "Crie gratuitamente",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }

            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                TextButton(onClick = { openExternal(context, privacyPolicyUrl) }) {
                    Icon(Icons.Filled.PrivacyTip, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Política de Privacidade", style = MaterialTheme.typography.labelMedium)
                }
                Text(
                    "EHS Fitness © ${java.time.Year.now()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitnessTheme.colors.subtle,
                )
            }
        }
    }
}

@Composable
private fun BenefitLine(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(22.dp)
                .background(FitnessTheme.colors.successSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = FitnessTheme.colors.success,
                modifier = Modifier.size(13.dp),
            )
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AuthTabs(isRegister: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(false to "Entrar", true to "Criar conta").forEach { (register, label) ->
            val selected = register == isRegister
            Box(
                Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(
                        if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        RoundedCornerShape(8.dp),
                    )
                    .clickable { onChange(register) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RoleCard(option: RoleOption, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) FitnessTheme.colors.primarySoft else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else FitnessTheme.colors.border,
        ),
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else FitnessTheme.colors.primarySoft,
                        RoundedCornerShape(9.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    option.icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(option.label, style = MaterialTheme.typography.titleSmall)
                Text(
                    option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun RegisterConfirmation(email: String, onGoToLogin: () -> Unit) {
    ContentCard {
        Box(
            Modifier
                .size(46.dp)
                .background(FitnessTheme.colors.successSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = FitnessTheme.colors.success,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text("Confirme seu e-mail", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            "Enviamos uma mensagem de confirmação para $email. Confirme sua conta antes de entrar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onGoToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(9.dp),
        ) {
            Text("Ir para o login")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
