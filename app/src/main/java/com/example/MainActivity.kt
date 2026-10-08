package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LanguageManager
import com.example.ui.screens.AddEditPropertyScreen
import com.example.ui.screens.AuthGateScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OwnerDashboardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PropertyDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeManager
import com.example.ui.viewmodel.RentNearViewModel

object RentNearRoutes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val PROPERTY_DETAILS = "property_details/{propertyId}"
    const val FAVORITES = "favorites"
    const val OWNER_DASHBOARD = "owner_dashboard"
    const val PROFILE = "profile"
    const val ADD_EDIT_PROPERTY = "add_edit_property?propertyId={propertyId}"

    fun propertyDetails(propertyId: String): String = "property_details/$propertyId"
    fun addProperty(): String = "add_edit_property"
    fun editProperty(propertyId: String): String = "add_edit_property?propertyId=$propertyId"
}

sealed interface Screen {
    data object Home : Screen
    data object Search : Screen
    data object Favorites : Screen
    data object OwnerDashboard : Screen
    data object Profile : Screen
    data class Details(val propertyId: String) : Screen
    data class AddEdit(val propertyId: String? = null) : Screen
}

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    HOME("Explore", Icons.Default.Home, Icons.Outlined.Home, RentNearRoutes.HOME),
    SEARCH("Search", Icons.Default.Search, Icons.Outlined.Search, RentNearRoutes.SEARCH),
    FAVORITES("Saved", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder, RentNearRoutes.FAVORITES),
    OWNER("Owner Hub", Icons.Default.Business, Icons.Outlined.Business, RentNearRoutes.OWNER_DASHBOARD),
    PROFILE("Profile", Icons.Default.Person, Icons.Outlined.Person, RentNearRoutes.PROFILE)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.init(this)
        ThemeManager.init(this)
        enableEdgeToEdge()
        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            val isDark = when (themeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = isDark) {
                RentNearApp()
            }
        }
    }
}

@Composable
fun RentNearApp(
    viewModel: RentNearViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    if (!isUserLoggedIn) {
        AuthGateScreen(viewModel = viewModel)
        return
    }

    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val isTopLevel = currentRoute in listOf(
        RentNearRoutes.HOME,
        RentNearRoutes.SEARCH,
        RentNearRoutes.FAVORITES,
        RentNearRoutes.OWNER_DASHBOARD,
        RentNearRoutes.PROFILE
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(
                    tonalElevation = 6.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = currentRoute == tab.route
                        val localizedTitle = when (tab) {
                            NavigationTab.HOME -> AppStrings.tabExplore(currentLanguage)
                            NavigationTab.SEARCH -> AppStrings.tabSearch(currentLanguage)
                            NavigationTab.FAVORITES -> AppStrings.tabSaved(currentLanguage)
                            NavigationTab.OWNER -> AppStrings.tabOwner(currentLanguage)
                            NavigationTab.PROFILE -> AppStrings.tabProfile(currentLanguage)
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = localizedTitle
                                )
                            },
                            label = { Text(localizedTitle) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = RentNearRoutes.HOME,
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. HOME SCREEN ROUTE
                composable(RentNearRoutes.HOME) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToSearch = {
                            navController.navigate(RentNearRoutes.SEARCH)
                        },
                        onNavigateToDetails = { propertyId ->
                            navController.navigate(RentNearRoutes.propertyDetails(propertyId))
                        },
                        onNavigateToOwnerDashboard = {
                            navController.navigate(RentNearRoutes.OWNER_DASHBOARD)
                        },
                        onNavigateToAddProperty = {
                            navController.navigate(RentNearRoutes.addProperty())
                        }
                    )
                }

                // 2. SEARCH SCREEN ROUTE
                composable(RentNearRoutes.SEARCH) {
                    SearchScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = { propertyId ->
                            navController.navigate(RentNearRoutes.propertyDetails(propertyId))
                        }
                    )
                }

                // 3. PROPERTY DETAILS SCREEN ROUTE
                composable(
                    route = RentNearRoutes.PROPERTY_DETAILS,
                    arguments = listOf(
                        navArgument("propertyId") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
                    PropertyDetailScreen(
                        propertyId = propertyId,
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // 4. FAVORITES SCREEN ROUTE
                composable(RentNearRoutes.FAVORITES) {
                    FavoritesScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = { propertyId ->
                            navController.navigate(RentNearRoutes.propertyDetails(propertyId))
                        },
                        onNavigateToExplore = {
                            navController.navigate(RentNearRoutes.HOME) {
                                popUpTo(RentNearRoutes.HOME) { inclusive = false }
                            }
                        }
                    )
                }

                // 5. OWNER DASHBOARD ROUTE
                composable(RentNearRoutes.OWNER_DASHBOARD) {
                    OwnerDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToAddProperty = {
                            navController.navigate(RentNearRoutes.addProperty())
                        },
                        onNavigateToEditProperty = { propertyId ->
                            navController.navigate(RentNearRoutes.editProperty(propertyId))
                        },
                        onNavigateToDetails = { propertyId ->
                            navController.navigate(RentNearRoutes.propertyDetails(propertyId))
                        }
                    )
                }

                // 6. PROFILE SCREEN ROUTE
                composable(RentNearRoutes.PROFILE) {
                    ProfileScreen(
                        viewModel = viewModel
                    )
                }

                // 7. ADD / EDIT PROPERTY SCREEN ROUTE
                composable(
                    route = RentNearRoutes.ADD_EDIT_PROPERTY,
                    arguments = listOf(
                        navArgument("propertyId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val propertyId = backStackEntry.arguments?.getString("propertyId")
                    AddEditPropertyScreen(
                        propertyId = propertyId,
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
