package com.example.livechat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.example.livechat.Screens.ChangeEmailScreen
import com.example.livechat.Screens.DepartmentScreen
import com.example.livechat.Screens.ChatListScreen
import com.example.livechat.Screens.LoginScreen
import com.example.livechat.Screens.ProfileScreen
import com.example.livechat.Screens.SignupScreen
import com.example.livechat.Screens.SingleChatScreen
import com.example.livechat.Screens.CityScreen
import com.example.livechat.Screens.DepartmentChat
import com.example.livechat.Screens.UserProfile
import com.example.livechat.Screens.UserProfileChats
import com.example.livechat.ui.theme.LiveChatTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

import kotlinx.serialization.Serializable


@Serializable
sealed class DestinationScreen(var route: String){
    object SignUp: DestinationScreen("signup")
    object Login: DestinationScreen("login")
    object Profile: DestinationScreen("profile")
    object ChatList: DestinationScreen("chatlist")
    object SingleChat: DestinationScreen("singlechat/{chatId}") {
        fun createRout(id: String) = "singlechat/$id"
    }
    
    object City: DestinationScreen("City")
    object Department: DestinationScreen("Department")
    object ChangeEmail: DestinationScreen("ChangeEmail")
}
@Serializable
data class UserProfileInfo(
    val name: String? = null,
    val imageUrl: String? = null,
    val city: String? = null,
    val birthday: String? = null,
    val gender: String? = null,
    val chatId: String?,
    val userId: String? = null,
    val banUser: Boolean
)

@Serializable
data class UserProfInfoChats(
    val senderId: String? = null,
    val name: String? = null,
    val imageUrl: String? = null,
    val city: String? = null,
    val birthday: String? = null,
    val gender: String? = null,
    val deviceToken: String? = null,
    val newMessageChats: Boolean = false
)

@Serializable
data class DepartmentChat(val itemName: String?, val cityName: String?)


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: LCViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.setUserOnlineStatus(true)
        handleDeepLink(intent)
        requestNotificationPermission()
        setContent {
            var isDarkTheme by remember { mutableStateOf(loadThemePreference()) }

            LiveChatTheme(
                darkTheme = isDarkTheme,
                content = {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ChatRootNavigation(
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { isDarkTheme = it; saveThemePreference(it) }
                        )
                    }
                }
            )
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.setUserOnlineStatus(false)
    }

    override fun onStop() {
        super.onStop()
        vm.setUserOnlineStatus(false)
    }

    override fun onResume() {
        super.onResume()
        vm.setUserOnlineStatus(true)
    }

    private fun saveThemePreference(isDarkTheme: Boolean) {
        getSharedPreferences("app_preferences", MODE_PRIVATE).edit {
            putBoolean("isDarkTheme", isDarkTheme)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(intent: Intent?) {
        val chatId = intent?.extras?.getString("chatId")
            ?: intent?.data?.getQueryParameter("chatId")

        chatId?.let { id ->
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit().apply {
                putString("pending_chat_id", id)
                putBoolean("should_open_chat_directly", true)
            }.apply()
        }
    }

    private fun loadThemePreference(): Boolean {
        return getSharedPreferences("app_preferences", MODE_PRIVATE)
            .getBoolean("isDarkTheme", false)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    }
}

@Composable
fun ChatRootNavigation(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val vm = hiltViewModel<LCViewModel>()
    val signInState = vm.signIn.value
    val context = LocalContext.current
    val navController = rememberNavController()

    // Получаем информацию о pending chatId
    val sharedPref = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val hasPendingChat = remember { derivedStateOf {
        sharedPref.getBoolean("should_open_chat_directly", false) &&
                sharedPref.getString("pending_chat_id", null) != null
    } }

    // Определяем стартовый экран
    val startDestination = remember(signInState, hasPendingChat.value) {
        when {
            !signInState -> DestinationScreen.SignUp.route
            hasPendingChat.value -> DestinationScreen.ChatList.route // или другой подходящий экран
            else -> DestinationScreen.Profile.route
        }
    }

    // Обработка deep link
    LaunchedEffect(signInState, hasPendingChat.value) {
        if (signInState && hasPendingChat.value) {
            val chatId = sharedPref.getString("pending_chat_id", null)

            sharedPref.edit {  
                remove("pending_chat_id")
                remove("should_open_chat_directly")
            }
            delay(300)

            if (chatId != null) {
                navController.navigate(DestinationScreen.SingleChat.createRout(chatId)) {
                    // Очищаем back stack до корня
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    // Восстанавливаем состояние
                    restoreState = true
                    // Запускаем как single top
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (signInState) DestinationScreen.Profile.route else DestinationScreen.SignUp.route
    ) {
        // Auth screens
        composable(DestinationScreen.SignUp.route) {
            SignupScreen(navController, vm)
        }
        composable(DestinationScreen.Login.route) {
            LoginScreen(vm, navController)
        }

        // Main app screens (only accessible when signed in)
        if (signInState) {
            composable(DestinationScreen.Profile.route) {
                ProfileScreen(navController, vm, isDarkTheme, onThemeChange)
            }
            composable(DestinationScreen.ChatList.route) {
                ChatListScreen(navController, vm, isDarkTheme)
            }
            composable(
                route = DestinationScreen.SingleChat.route,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                SingleChatScreen(navController, vm, chatId, isDarkTheme)
            }
            composable(DestinationScreen.City.route) {
                CityScreen(navController, vm, isDarkTheme)
            }
            composable(DestinationScreen.Department.route) {
                DepartmentScreen(navController, vm, isDarkTheme)
            }
            composable(DestinationScreen.ChangeEmail.route) {
                ChangeEmailScreen(navController, vm, isDarkTheme)
            }
            // Serializable destinations
            composable<UserProfileInfo> { backStackEntry ->
                val args = backStackEntry.toRoute<UserProfileInfo>()
                UserProfile(
                    navController = navController,
                    vm = vm,
                    name = args.name,
                    imageUrl = args.imageUrl,
                    city = args.city,
                    birthday = args.birthday,
                    gender = args.gender,
                    chatId = args.chatId,
                    userId = args.userId,
                    banUser = args.banUser,
                    isDarkTheme = isDarkTheme
                )
            }

            composable<UserProfInfoChats> { backStackEntry ->
                val args = backStackEntry.toRoute<UserProfInfoChats>()
                UserProfileChats(
                    vm = vm,
                    navController = navController,
                    senderId = args.senderId,
                    name = args.name,
                    imageUrl = args.imageUrl,
                    city = args.city,
                    birthday = args.birthday,
                    gender = args.gender,
                    deviceToken = args.deviceToken,
                    newMessageChats = args.newMessageChats,
                    isDarkTheme = isDarkTheme
                )
            }

            composable<DepartmentChat> { backStackEntry ->
                val args = backStackEntry.toRoute<DepartmentChat>()
                DepartmentChat(
                    navController = navController,
                    vm = vm,
                    itemName = args.itemName,
                    cityName = args.cityName,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}