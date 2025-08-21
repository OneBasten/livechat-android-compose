package com.example.livechat.Screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.livechat.CommonImage
import com.example.livechat.FullScreenImage
import com.example.livechat.LCViewModel

@Composable
fun UserProfile(
    navController: NavController,
    vm: LCViewModel,
    name: String?,
    imageUrl: String?,
    city: String?,
    birthday: String?,
    gender: String?,
    chatId: String?,
    userId: String?,
    banUser: Boolean,
    isDarkTheme: Boolean
) {
    // Проверяем, удален ли пользователь
    val isUserDeleted = name == "Удаленный пользователь" || name.isNullOrEmpty()

    var localBanUser by remember { mutableStateOf(banUser) }
    var showBanDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.padding(start = 8.dp, top = 21.dp)) {
        UserProfileInfoHeader {
            navController.popBackStack()
        }
    }

    var isFullScreen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isUserDeleted) {
            // Отображение для удаленного пользователя
            Text(
                text = "Пользователь удален",
                fontSize = 25.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Filled.PersonOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
        } else {
            // Обычное отображение профиля
            if (isFullScreen) {
                if (imageUrl != null) {
                    FullScreenImage(
                        imageUrl = imageUrl,
                        onClose = { isFullScreen = false }
                    )
                }
            } else {
                if (imageUrl != null) {
                    if (imageUrl.isNotEmpty()) {
                        CommonImage(
                            data = imageUrl,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(150.dp)
                                .clip(CircleShape)
                                .clickable { isFullScreen = true }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(150.dp)
                                .clip(CircleShape)
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        if (isDarkTheme) Color.White else Color.Black
                                    ),
                                    CircleShape
                                ),
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(7.dp))

            if (name != null) {
                Text(
                    text = "Имя: $name",
                    fontSize = 25.sp
                )
            }

            Divider(modifier = Modifier.padding(start = 110.dp, end = 110.dp, top = 7.dp, bottom = 7.dp))

            if (city != null) {
                Text(
                    text = "Город: $city",
                    fontSize = 25.sp
                )
            }

            Divider(modifier = Modifier.padding(start = 110.dp, end = 110.dp, top = 7.dp, bottom = 7.dp))

            val age = birthday?.let { vm.calculateAge(it) }
            if (birthday != null) {
                Text(
                    text = "Возраст: $age",
                    fontSize = 25.sp
                )
            }

            Divider(modifier = Modifier.padding(start = 110.dp, end = 110.dp, top = 7.dp, bottom = 7.dp))

            if (gender != null) {
                Text(
                    text = "Пол: $gender",
                    fontSize = 25.sp
                )
            }

            Spacer(modifier = Modifier.padding(15.dp))

            // Кнопка блокировки/разблокировки (только если пользователь не удален)
            if (!isUserDeleted && chatId != null && userId != null) {
                Text(
                    text = if (localBanUser) "Разблокировать пользователя" else "Заблокировать пользователя",
                    color = Color.Red,
                    modifier = Modifier.clickable { showBanDialog = true }
                )
            }
        }
    }

    // Диалог подтверждения блокировки/разблокировки
    if (showBanDialog) {
        AlertDialog(
            onDismissRequest = { showBanDialog = false },
            title = {
                Text(text = if (localBanUser)
                    "Вы действительно хотите разблокировать пользователя?"
                else
                    "Вы действительно хотите заблокировать пользователя?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chatId != null && userId != null) {
                            vm.banUserInChat(chatId, userId, !localBanUser)
                            localBanUser = !localBanUser
                            Toast.makeText(
                                context,
                                if (localBanUser) "Пользователь заблокирован"
                                else "Пользователь разблокирован",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        showBanDialog = false
                    }
                ) {
                    Text(text = if (localBanUser) "Разблокировать" else "Заблокировать")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showBanDialog = false }
                ) {
                    Text(text = "Отмена")
                }
            }
        )
    }
}
@Composable
fun UserProfileInfoHeader(onBackClicked: () -> Unit){
    Row(
        modifier = Modifier
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Rounded.ArrowBackIosNew, contentDescription = null,
            modifier = Modifier
                .clickable { onBackClicked.invoke() }

        )
    }
}