package com.example.sos.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sos.data.SosRepository
import com.example.sos.data.model.TaskItem
import com.example.sos.feature.chat.CancellationSentScreen
import com.example.sos.feature.chat.ChatListScreen
import com.example.sos.feature.chat.ChatRoomScreen
import com.example.sos.feature.chat.ContractAgreementScreen
import com.example.sos.feature.chat.ReviewScreen
import com.example.sos.feature.explore.ExploreScreen
import com.example.sos.feature.explore.HelperInfoScreen
import com.example.sos.feature.history.HistoryScreen
import com.example.sos.feature.history.RequestDetailsScreen
import com.example.sos.feature.home.HomeScreen
import com.example.sos.feature.profile.EditProfileScreen
import com.example.sos.feature.profile.ProfileScreen
import com.example.sos.feature.request.CreateRequestScreen
import com.example.sos.feature.request.DirectRequestScreen
import com.example.sos.feature.request.EditRequestScreen
import com.example.sos.feature.request.RequestInfoScreen
import com.example.sos.feature.wallet.TopUpScreen
import com.example.sos.feature.wallet.TransactionHistoryScreen
import com.example.sos.feature.wallet.WithdrawDialog
import com.example.sos.ui.components.SosBlue
import com.example.sos.ui.components.SosGray

/** Route di dalam area client (setelah login). */
object MainRoute {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val CHAT = "chat"
    const val PROFILE = "profile"
    const val CREATE_REQUEST = "create_request"
    const val HISTORY = "history"
    const val REQUEST = "request/{requestId}"
    const val EDIT_REQUEST = "edit_request/{requestId}"
    const val HELPER = "helper/{helperId}"
    const val DIRECT_REQUEST = "direct_request/{helperId}"
    const val DETAILS = "details/{assignmentId}"
    const val CHAT_ROOM = "chat/{conversationId}"
    const val CONTRACT = "contract/{requestId}/{helperId}"
    const val REVIEW = "review/{assignmentId}"
    const val CANCELLATION_SENT = "cancellation_sent"
    const val EDIT_PROFILE = "edit_profile"
    const val TOP_UP = "top_up"
    const val TRANSACTIONS = "transactions"

    fun request(id: String) = "request/$id"
    fun editRequest(id: String) = "edit_request/$id"
    fun helper(id: String) = "helper/$id"
    fun directRequest(helperId: String) = "direct_request/$helperId"
    fun details(assignmentId: String) = "details/$assignmentId"
    fun chatRoom(conversationId: String) = "chat/$conversationId"
    fun contract(requestId: String, helperId: String) = "contract/$requestId/$helperId"
    fun review(assignmentId: String) = "review/$assignmentId"
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(MainRoute.HOME, "Home", Icons.Outlined.Home),
    BottomTab(MainRoute.EXPLORE, "Explore", Icons.Outlined.Explore),
    BottomTab(MainRoute.CHAT, "Chat", Icons.AutoMirrored.Outlined.Chat),
    BottomTab(MainRoute.PROFILE, "Profile", Icons.Outlined.Person)
)

/**
 * Area client (user biasa): bottom navigation Home / Explore / Chat / Profile.
 * Halaman detail dibuka di atasnya tanpa bottom bar.
 */
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = bottomTabs.any { it.route == currentRoute }
    val context = LocalContext.current
    var showWithdraw by remember { mutableStateOf(false) }

    val toast: (String) -> Unit = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }

    // Buka (atau buat) percakapan Requester ↔ Helper untuk sebuah request.
    val openChat: (String, String) -> Unit = { requestId, helperId ->
        SosRepository.openConversation(requestId, helperId)?.let {
            navController.navigate(MainRoute.chatRoom(it))
        }
    }

    // Ongoing task / history: yang sudah punya assignment → Request Details,
    // yang masih Floating → halaman request (untuk melihat reply).
    val openTask: (TaskItem) -> Unit = { task ->
        val assignment = task.assignment
        if (assignment != null) navController.navigate(MainRoute.details(assignment.id))
        else navController.navigate(MainRoute.request(task.request.id))
    }

    val back: () -> Unit = { navController.popBackStack() }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (showBottomBar) SosBottomBar(navController, currentRoute)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.HOME,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            // ---------- Tab ----------
            composable(MainRoute.HOME) {
                HomeScreen(
                    onPostRequestClick = { navController.navigate(MainRoute.CREATE_REQUEST) },
                    onTaskClick = openTask
                )
            }

            composable(MainRoute.EXPLORE) {
                ExploreScreen(
                    onRequestClick = { navController.navigate(MainRoute.request(it)) },
                    onHelperClick = { navController.navigate(MainRoute.helper(it)) },
                    onCreateRequestClick = { navController.navigate(MainRoute.CREATE_REQUEST) }
                )
            }

            composable(MainRoute.CHAT) {
                ChatListScreen(onConversationClick = { navController.navigate(MainRoute.chatRoom(it)) })
            }

            composable(MainRoute.PROFILE) {
                ProfileScreen(
                    onEditProfile = { navController.navigate(MainRoute.EDIT_PROFILE) },
                    onHistoryClick = { navController.navigate(MainRoute.HISTORY) },
                    onTransactionsClick = { navController.navigate(MainRoute.TRANSACTIONS) },
                    onTopUpClick = { navController.navigate(MainRoute.TOP_UP) },
                    onWithdrawClick = { showWithdraw = true },
                    onLogout = onLogout
                )
            }

            // ---------- Request ----------
            composable(MainRoute.CREATE_REQUEST) {
                CreateRequestScreen(
                    onBack = back,
                    onPosted = { id ->
                        navController.navigate(MainRoute.request(id)) {
                            popUpTo(MainRoute.CREATE_REQUEST) { inclusive = true }
                        }
                    }
                )
            }

            composable(MainRoute.REQUEST) { entry ->
                val requestId = entry.arguments?.getString("requestId").orEmpty()
                RequestInfoScreen(
                    requestId = requestId,
                    onBack = back,
                    onHelperClick = { navController.navigate(MainRoute.helper(it)) },
                    onChatClick = { helperId -> openChat(requestId, helperId) },
                    onOpenAssignment = { navController.navigate(MainRoute.details(it)) },
                    onSelectHelper = { helperId -> navController.navigate(MainRoute.contract(requestId, helperId)) },
                    onEditRequest = { navController.navigate(MainRoute.editRequest(requestId)) }
                )
            }

            composable(MainRoute.EDIT_REQUEST) { entry ->
                EditRequestScreen(
                    requestId = entry.arguments?.getString("requestId").orEmpty(),
                    onBack = back,
                    onSaved = {
                        toast("Request updated")
                        navController.popBackStack()
                    }
                )
            }

            composable(MainRoute.HELPER) { entry ->
                val helperId = entry.arguments?.getString("helperId").orEmpty()
                HelperInfoScreen(
                    helperId = helperId,
                    onBack = back,
                    onChatHelperClick = { navController.navigate(MainRoute.directRequest(helperId)) }
                )
            }

            composable(MainRoute.DIRECT_REQUEST) { entry ->
                DirectRequestScreen(
                    helperId = entry.arguments?.getString("helperId").orEmpty(),
                    onBack = back,
                    onSent = { conversationId ->
                        // Ganti form dengan ruang chat yang baru dibuat.
                        navController.navigate(MainRoute.chatRoom(conversationId)) {
                            popUpTo(MainRoute.DIRECT_REQUEST) { inclusive = true }
                        }
                    }
                )
            }

            // ---------- Chat & assignment ----------
            composable(MainRoute.CHAT_ROOM) { entry ->
                ChatRoomScreen(
                    conversationId = entry.arguments?.getString("conversationId").orEmpty(),
                    onBack = back,
                    onProfileClick = { userId ->
                        // Profil publik hanya untuk Helper (yang punya skill).
                        if (SosRepository.user(userId)?.skills?.isNotEmpty() == true) {
                            navController.navigate(MainRoute.helper(userId))
                        }
                    },
                    onAcceptHelp = { requestId, helperId ->
                        navController.navigate(MainRoute.contract(requestId, helperId))
                    },
                    onEditRequest = { navController.navigate(MainRoute.editRequest(it)) },
                    onCancellationSent = { navController.navigate(MainRoute.CANCELLATION_SENT) },
                    onReview = { navController.navigate(MainRoute.review(it)) }
                )
            }

            composable(MainRoute.CONTRACT) { entry ->
                ContractAgreementScreen(
                    requestId = entry.arguments?.getString("requestId").orEmpty(),
                    helperId = entry.arguments?.getString("helperId").orEmpty(),
                    onBack = back,
                    onAgreed = { conversationId ->
                        toast("Agreement approved. The work has started!")
                        // Kalau datang dari chat itu, kembali ke sana; kalau tidak, buka chat-nya.
                        val previous = navController.previousBackStackEntry
                        val fromSameChat = previous?.destination?.route == MainRoute.CHAT_ROOM &&
                                previous.arguments?.getString("conversationId") == conversationId
                        if (fromSameChat) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(MainRoute.chatRoom(conversationId)) {
                                popUpTo(MainRoute.CONTRACT) { inclusive = true }
                            }
                        }
                    },
                    onTopUp = { navController.navigate(MainRoute.TOP_UP) }
                )
            }

            composable(MainRoute.REVIEW) { entry ->
                ReviewScreen(
                    assignmentId = entry.arguments?.getString("assignmentId").orEmpty(),
                    onBack = back,
                    onSent = {
                        toast("Thanks for your review!")
                        navController.popBackStack()
                    }
                )
            }

            composable(MainRoute.CANCELLATION_SENT) {
                CancellationSentScreen(onDone = back)
            }

            composable(MainRoute.HISTORY) {
                HistoryScreen(onBack = back, onItemClick = openTask)
            }

            composable(MainRoute.DETAILS) { entry ->
                RequestDetailsScreen(
                    assignmentId = entry.arguments?.getString("assignmentId").orEmpty(),
                    onBack = back,
                    onSeePost = { navController.navigate(MainRoute.request(it)) },
                    onChatClick = openChat,
                    onHelperClick = { navController.navigate(MainRoute.helper(it)) },
                    onOpenContract = { requestId, helperId ->
                        navController.navigate(MainRoute.contract(requestId, helperId))
                    },
                    onReview = { navController.navigate(MainRoute.review(it)) },
                    onCancellationSent = { navController.navigate(MainRoute.CANCELLATION_SENT) }
                )
            }

            // ---------- Profil & wallet ----------
            composable(MainRoute.EDIT_PROFILE) {
                EditProfileScreen(
                    onBack = back,
                    onSaved = {
                        toast("Profile updated")
                        navController.popBackStack()
                    }
                )
            }

            composable(MainRoute.TOP_UP) {
                TopUpScreen(
                    onBack = back,
                    onDone = {
                        toast("Top up successful")
                        navController.popBackStack()
                    }
                )
            }

            composable(MainRoute.TRANSACTIONS) {
                TransactionHistoryScreen(
                    onBack = back,
                    onTopUp = { navController.navigate(MainRoute.TOP_UP) }
                )
            }
        }
    }

    if (showWithdraw) {
        WithdrawDialog(
            onDismiss = { showWithdraw = false },
            onDone = {
                showWithdraw = false
                toast("Withdrawal requested")
            }
        )
    }
}

@Composable
private fun SosBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = Color.White) {
        bottomTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SosBlue,
                    selectedTextColor = SosBlue,
                    unselectedIconColor = SosGray,
                    unselectedTextColor = SosGray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
