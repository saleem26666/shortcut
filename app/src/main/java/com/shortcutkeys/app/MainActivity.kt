package com.shortcutkeys.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shortcutkeys.app.data.AppLang
import com.shortcutkeys.app.ui.add.AddShortcutScreen
import com.shortcutkeys.app.ui.add.AddSoftwareScreen
import com.shortcutkeys.app.ui.detail.SoftwareDetailScreen
import com.shortcutkeys.app.ui.favorites.FavoritesScreen
import com.shortcutkeys.app.ui.home.HomeScreen
import com.shortcutkeys.app.ui.quiz.QuizScreen
import com.shortcutkeys.app.ui.theme.ShortcutTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val direction = if (state.lang == AppLang.UR) LayoutDirection.Rtl else LayoutDirection.Ltr
            ShortcutTheme {
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val nav = rememberNavController()
                        NavHost(navController = nav, startDestination = "home") {
                            composable("home") {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onOpen = { nav.navigate("detail/$it") },
                                    onFavorites = { nav.navigate("favorites") },
                                    onQuiz = { nav.navigate("quiz") },
                                    onAdd = { nav.navigate("addSoftware") }
                                )
                            }
                            composable(
                                route = "detail/{id}",
                                arguments = listOf(navArgument("id") { type = NavType.StringType })
                            ) { entry ->
                                val id = entry.arguments?.getString("id").orEmpty()
                                SoftwareDetailScreen(
                                    softwareId = id,
                                    viewModel = viewModel,
                                    onBack = { nav.popBackStack() },
                                    onAddShortcut = { nav.navigate("addShortcut/$id") }
                                )
                            }
                            composable("addSoftware") {
                                AddSoftwareScreen(viewModel = viewModel, onBack = { nav.popBackStack() })
                            }
                            composable(
                                route = "addShortcut/{id}",
                                arguments = listOf(navArgument("id") { type = NavType.StringType })
                            ) { entry ->
                                val id = entry.arguments?.getString("id").orEmpty()
                                AddShortcutScreen(
                                    softwareId = id,
                                    viewModel = viewModel,
                                    onBack = { nav.popBackStack() }
                                )
                            }
                            composable("favorites") {
                                FavoritesScreen(viewModel = viewModel, onBack = { nav.popBackStack() })
                            }
                            composable("quiz") {
                                QuizScreen(viewModel = viewModel, onBack = { nav.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}
