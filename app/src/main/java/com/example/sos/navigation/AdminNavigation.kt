package com.example.sos.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sos.feature.admin.*

/** Area Admin (role ADMIN): dashboard + halaman penanganan. */
@Composable
fun AdminNavigation(onLogout: () -> Unit) {
    val navController = rememberNavController()
    var showNotifications by remember { mutableStateOf(false) }

    fun go(base: String, vararg params: Pair<String, String?>) {
        val query = params.filter { !it.second.isNullOrBlank() }
            .joinToString("&") { "${it.first}=${android.net.Uri.encode(it.second)}" }
        navController.navigate(if (query.isEmpty()) base else "$base?$query") { launchSingleTop = true }
    }

    val actions = AdminActions(
        back = { navController.popBackStack() },
        logout = onLogout,
        notifications = { showNotifications = true },
        verification = { go("admin_verification", "id" to it) },
        users = { id, q -> go("admin_users", "id" to id, "q" to q) },
        moderation = { go("admin_moderation", "id" to it) },
        cases = { go("admin_cases", "id" to it) },
        transactions = { go("admin_transactions", "id" to it) }
    )

    val optional = { name: String ->
        navArgument(name) { type = NavType.StringType; nullable = true; defaultValue = null }
    }

    NavHost(navController = navController, startDestination = "admin_dashboard") {
        composable("admin_dashboard") { AdminDashboardScreen(actions) }

        composable("admin_verification?id={id}", arguments = listOf(optional("id"))) {
            AdminVerificationScreen(it.arguments?.getString("id"), actions)
        }
        composable("admin_users?id={id}&q={q}", arguments = listOf(optional("id"), optional("q"))) {
            AdminUsersScreen(it.arguments?.getString("id"), it.arguments?.getString("q"), actions)
        }
        composable("admin_moderation?id={id}", arguments = listOf(optional("id"))) {
            AdminModerationScreen(it.arguments?.getString("id"), actions)
        }
        composable("admin_cases?id={id}", arguments = listOf(optional("id"))) {
            AdminCasesScreen(it.arguments?.getString("id"), actions)
        }
        composable("admin_transactions?id={id}", arguments = listOf(optional("id"))) {
            AdminTransactionsScreen(it.arguments?.getString("id"), actions)
        }
    }

    if (showNotifications) {
        AdminNotificationsDialog(actions, onDismiss = { showNotifications = false })
    }
}
