package com.ehs.fitness.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ehs.fitness.core.session.AuthUser
import com.ehs.fitness.core.ui.components.InitialAvatar
import com.ehs.fitness.core.ui.components.ThemeToggleButton
import com.ehs.fitness.core.ui.theme.FitnessTheme
import com.ehs.fitness.feature.auth.AuthScreen
import com.ehs.fitness.feature.auth.BrandMark
import com.ehs.fitness.feature.auth.SessionViewModel
import com.ehs.fitness.feature.diet.DietScreen
import com.ehs.fitness.feature.home.HomeScreen
import com.ehs.fitness.feature.profile.ProfileScreen
import com.ehs.fitness.feature.workouts.WeekPlanScreen
import com.ehs.fitness.feature.workouts.WorkoutsScreen
import com.ehs.fitness.ui.appViewModel
import kotlinx.coroutines.launch

private data class Destination(
    val route: String,
    val icon: ImageVector,
    val label: String,
)

private val drawerDestinations = listOf(
    Destination(Routes.HOME, Icons.Filled.Home, "Início"),
    Destination(Routes.WORKOUTS, Icons.Filled.FitnessCenter, "Treinos"),
    Destination(Routes.WEEK, Icons.Filled.CalendarMonth, "Agenda semanal"),
    Destination(Routes.DIET, Icons.Filled.Restaurant, "Alimentação"),
    Destination(Routes.PROFILE, Icons.Filled.Person, "Perfil"),
)

private val bottomDestinations = drawerDestinations.filter {
    it.route in setOf(Routes.HOME, Routes.WORKOUTS, Routes.WEEK, Routes.DIET)
}

@Composable
fun FitnessApp() {
    val sessionVm: SessionViewModel = appViewModel { SessionViewModel(it.authRepository) }
    val session by sessionVm.session.collectAsStateWithLifecycle()
    val user = session?.user

    if (user == null) {
        AuthScreen()
    } else {
        MainScaffold(user = user, onLogout = sessionVm::logout)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(user: AuthUser, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val go: (String) -> Unit = { destination ->
        navController.navigate(destination) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val title = when (route) {
        Routes.WORKOUTS -> "Treinos"
        Routes.WEEK -> "Agenda semanal"
        Routes.DIET -> "Alimentação"
        Routes.PROFILE -> "Perfil"
        else -> "Início"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FitnessDrawer(
                user = user,
                currentRoute = route,
                onNavigate = { destination ->
                    scope.launch { drawerState.close() }
                    go(destination)
                },
                onLogout = onLogout,
            )
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menu")
                        }
                    },
                    actions = {
                        ThemeToggleButton()
                        IconButton(onClick = { go(Routes.PROFILE) }) {
                            InitialAvatar(name = user.name, size = 30.dp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            },
            bottomBar = {
                if (bottomDestinations.any { it.route == route }) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        bottomDestinations.forEach { destination ->
                            NavigationBarItem(
                                selected = route == destination.route,
                                onClick = { go(destination.route) },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) },
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = FitnessTheme.colors.primarySoft,
                                ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            user = user,
                            onOpenWorkouts = { go(Routes.WORKOUTS) },
                            onOpenDiet = { go(Routes.DIET) },
                            onOpenWeek = { go(Routes.WEEK) },
                        )
                    }
                    composable(Routes.WORKOUTS) {
                        WorkoutsScreen()
                    }
                    composable(Routes.WEEK) {
                        WeekPlanScreen()
                    }
                    composable(Routes.DIET) {
                        DietScreen()
                    }
                    composable(Routes.PROFILE) {
                        ProfileScreen(onLogout = onLogout)
                    }
                }
            }
        }
    }
}

@Composable
private fun FitnessDrawer(
    user: AuthUser,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val colors = FitnessTheme.colors

    ModalDrawerSheet(
        drawerContainerColor = colors.sidebar,
        drawerContentColor = Color.White,
        modifier = Modifier.width(300.dp),
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(20.dp),
        ) {
            BrandMark(dark = true)
            Spacer(Modifier.height(28.dp))
            drawerDestinations.forEach { destination ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            if (currentRoute == destination.route) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(8.dp),
                        )
                        .clickable { onNavigate(destination.route) }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        destination.icon,
                        contentDescription = null,
                        tint = if (currentRoute == destination.route) Color.White else colors.sidebarText,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        destination.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (currentRoute == destination.route) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (currentRoute == destination.route) Color.White else colors.sidebarText,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.weight(1f))
            HorizontalDivider(color = Color.White.copy(alpha = 0.13f))
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InitialAvatar(name = user.name, size = 38.dp)
                Column {
                    Text(
                        user.name.ifBlank { "Atleta" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                    Text(
                        user.roleLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.sidebarText,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = colors.sidebarText),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sair", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
