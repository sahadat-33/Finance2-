package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.AddTransactionDialog
import com.example.ui.DashboardScreen
import com.example.ui.SettingsScreen
import com.example.ui.OthersScreen
import com.example.ui.UpdateScreen
import com.example.ui.TimelineScreen
import com.example.ui.theme.FinanceTrackerTheme
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.example.ui.PinScreen

import com.google.firebase.FirebaseApp

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            val viewModel: FinanceViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val appTheme by viewModel.appTheme.collectAsState()
            val isAppLocked by viewModel.isAppLocked.collectAsState()
            val isOfflineGuest by viewModel.isOfflineGuest.collectAsState()
            val isUserSignedIn by viewModel.isUserSignedInFlow.collectAsState()
            val isEmailVerified by viewModel.isEmailVerifiedFlow.collectAsState()
            val isOnboardingComplete by viewModel.isOnboardingComplete.collectAsState()

            val showAuthScreen = !isUserSignedIn && !isOfflineGuest
            
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        viewModel.lockApp()
                    } else if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.triggerFetchFromCloud()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            FinanceTrackerTheme(darkTheme = isDarkMode, themeName = appTheme) {
                val rootNavController = rememberNavController()

                val startDest = remember(showAuthScreen, isUserSignedIn, isEmailVerified, isOnboardingComplete) {
                    if (showAuthScreen) "welcome_auth"
                    else if (isUserSignedIn && !isEmailVerified) "verification"
                    else if (!isOnboardingComplete) "onboarding_balance"
                    else "main"
                }

                LaunchedEffect(showAuthScreen) {
                    if (showAuthScreen) {
                        rootNavController.navigate("welcome_auth") {
                            popUpTo(rootNavController.graph.id) { inclusive = true }
                        }
                    }
                }

                NavHost(navController = rootNavController, startDestination = startDest) {
                    composable("welcome_auth") {
                        com.example.ui.WelcomeAuthScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { 
                                if (!viewModel.isEmailVerifiedFlow.value) {
                                    rootNavController.navigate("verification") { popUpTo("welcome_auth") { inclusive = true } }
                                } else if (!viewModel.isOnboardingComplete.value) {
                                    rootNavController.navigate("onboarding_balance") { popUpTo("welcome_auth") { inclusive = true } }
                                } else {
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                }
                            },
                            onBypass = { 
                                if (!viewModel.isOnboardingComplete.value) {
                                    rootNavController.navigate("onboarding_balance") { popUpTo("welcome_auth") { inclusive = true } }
                                } else {
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                }
                            }
                        )
                    }
                    composable("verification") {
                        com.example.ui.EmailVerificationScreen(viewModel = viewModel, navController = rootNavController)
                    }
                    composable("onboarding_balance") {
                        com.example.ui.OnboardingBalanceScreen(
                            viewModel = viewModel,
                            onComplete = { 
                                viewModel.completeOnboarding()
                                rootNavController.navigate("main") { popUpTo("onboarding_balance") { inclusive = true } }
                            }
                        )
                    }
                    composable("main") {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val navController = rememberNavController()
                    
                    // Track modal quick add state
                    var showAddDialog by remember { mutableStateOf(false) }

                // Retrieve active navigation route
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

                Scaffold(
                    modifier = Modifier.fillMaxSize().testTag("app_scaffold"),
                    topBar = {
                        if (currentRoute == "dashboard" || currentRoute == "timeline") {
                            val selectedCalendar by viewModel.selectedCalendar.collectAsState()
                            val monthNameFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
                            val formattedMonth = monthNameFormatter.format(selectedCalendar.time)
                            
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.wrapContentSize().testTag("unified_month_selector")
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.previousMonth() },
                                            modifier = Modifier.testTag("prev_month_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Previous Month",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Text(
                                            text = formattedMonth.uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .testTag("selected_month_label")
                                        )

                                        IconButton(
                                            onClick = { viewModel.nextMonth() },
                                            modifier = Modifier.testTag("next_month_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Next Month",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { viewModel.toggleDarkMode(!isDarkMode) },
                                        modifier = Modifier.testTag("theme_toggle_btn")
                                    ) {
                                        Icon(imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.ModeNight, contentDescription = "Toggle Theme", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    },
                    bottomBar = {
                        val navItemColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary
                        )
                        NavigationBar(
                            modifier = Modifier.testTag("app_navigation_bar"),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == "dashboard",
                                onClick = {
                                    if (currentRoute != "dashboard") {
                                        navController.navigate("dashboard") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard overview") },
                                label = { Text("Dashboard") },
                                colors = navItemColors,
                                modifier = Modifier.testTag("nav_tab_dashboard")
                            )
                            NavigationBarItem(
                                selected = currentRoute == "timeline",
                                onClick = {
                                    if (currentRoute != "timeline") {
                                        navController.navigate("timeline") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.History, contentDescription = "Daily ledger timeline") },
                                label = { Text("Timeline") },
                                colors = navItemColors,
                                modifier = Modifier.testTag("nav_tab_timeline")
                            )
                            NavigationBarItem(
                                selected = currentRoute == "yearly",
                                onClick = {
                                    if (currentRoute != "yearly") {
                                        navController.navigate("yearly") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Analytics, contentDescription = "Summary") },
                                label = { Text("Analytics") },
                                colors = navItemColors,
                                modifier = Modifier.testTag("nav_tab_yearly")
                            )
                            NavigationBarItem(
                                selected = currentRoute == "settings",
                                onClick = {
                                    if (currentRoute != "settings") {
                                        navController.navigate("settings") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Menu, contentDescription = "Menu page") },
                                label = { Text("Menu") },
                                colors = navItemColors,
                                modifier = Modifier.testTag("nav_tab_settings")
                            )
                        }
                    },
                    floatingActionButton = {
                        // Present FAB only on Home Dashboard and Timeline Daily Log (Screen 1 & 2 as requested)
                        if (currentRoute == "dashboard" || currentRoute == "timeline") {
                            FloatingActionButton(
                                onClick = { showAddDialog = true },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("quick_add_fab")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Quick add transaction FAB")
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = { 
                            androidx.compose.animation.slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(150)) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150))
                        },
                        exitTransition = { 
                            androidx.compose.animation.slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(150)) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
                        },
                        popEnterTransition = { 
                            androidx.compose.animation.slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(150)) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150))
                        },
                        popExitTransition = { 
                            androidx.compose.animation.slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(150)) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
                        }
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = {
                                    navController.navigate("settings") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable("timeline") {
                            TimelineScreen(viewModel = viewModel)
                        }
                        composable("yearly") {
                            com.example.ui.YearlySummaryScreen(viewModel = viewModel)
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToAuth = { rootNavController.navigate("welcome_auth") },
                                onNavigateToOthers = { navController.navigate("others") }
                            )
                        }
                        composable("others") {
                            OthersScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onNavigateToUpdate = { navController.navigate("update") })
                        }
                        composable("update") {
                            UpdateScreen(onBack = { navController.popBackStack() })
                        }
                        composable("profile") {
                            AccountSettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                    }

                        // Render quick-add transaction dialogue
                        if (showAddDialog) {
                            AddTransactionDialog(
                                onDismiss = { showAddDialog = false },
                                viewModel = viewModel
                            )
                        }
                    }
                        if (isAppLocked) {
                            PinScreen(onVerify = { pin -> viewModel.verifyPin(pin) })
                        }
                    } // End of Box block
                    } // End of main composable block
                } // End of root NavHost
            } // End of Theme
        }
    }
}
