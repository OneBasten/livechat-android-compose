package com.example.livechat.Screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.livechat.CommonImage
import com.example.livechat.DestinationScreen
import com.example.livechat.FullScreenImage
import com.example.livechat.LCViewModel
import com.example.livechat.ui.theme.GreyCard
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

@Composable
fun UserProfileChats(
    vm: LCViewModel,
    navController: NavController,
    senderId: String?,
    name: String?,
    imageUrl: String?,
    city: String?,
    birthday: String?,
    gender: String?,
    deviceToken: String?,
    newMessageChats: Boolean,
    isDarkTheme: Boolean
) {
    val userData = vm.userData.value
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val onEditClick: () -> Unit = { showDialog.value = true }
    val onDismiss: () -> Unit = { showDialog.value = false }

    // Проверяем, удален ли пользователь
    val isUserDeleted = name == "Удаленный пользователь" || name.isNullOrEmpty()

    val chats = vm.chats.value
    val userExists = remember { mutableStateOf(false) }

    val navigateTo = {
        chats.forEach { chat ->
            if (userData != null) {
                if ((chat.user1.userId == userData.userId && chat.user2.userId == senderId) ||
                    (chat.user1.userId == senderId && chat.user2.userId == userData.userId)) {
                    chat.chatId?.let {
                        navController.navigate(DestinationScreen.SingleChat.createRout(id = it))
                    }
                }
            }
        }
    }

    LaunchedEffect(senderId) {
        if (userData != null) {
            userExists.value = chats.any { chat ->
                (chat.user1.userId == userData.userId && chat.user2.userId == senderId) ||
                        (chat.user1.userId == senderId && chat.user2.userId == userData.userId)
            }
        }
    }

    val onButtonClick = {
        if (userExists.value) {
            navigateTo()
        } else {
            onEditClick()
        }
    }

    val buttonText = if (userExists.value) "Перейти в чат" else "Написать сообщение"
    var isFullScreen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(start = 8.dp, top = 21.dp)) {
        UserProfileChatsHeader() {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isUserDeleted) {
            // Показываем сообщение об удаленном пользователе
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
                                    BorderStroke(width = 1.dp, if (isDarkTheme) Color.White else Color.Black),
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

            if (birthday != null) {
                val age = birthday.let { vm.calculateAge(it) }
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

            Spacer(modifier = Modifier.padding(7.dp))

            if (senderId != null && deviceToken != null && !isUserDeleted) {
                AlertDialogUser(
                    vm,
                    navigateTo,
                    showDialog = showDialog.value,
                    onEditClick = onEditClick,
                    onDismiss = onDismiss,
                    context = context,
                    onButtonClick,
                    buttonText,
                    senderId,
                    deviceToken,
                    userData?.imageUrl,
                    userData?.name,
                    newMessageChats = newMessageChats
                )
            }
        }
    }
}
@Composable
fun UserProfileChatsHeader(onBackClicked: () -> Unit){
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

@Composable
fun AlertDialogUser(
    vm: LCViewModel,
    navigateTo: () -> Unit,
    showDialog: Boolean,
    onEditClick: () -> Unit,
    onDismiss: () -> Unit,
    context: Context,
    onButtonClick: () -> Unit,
    buttonText: String,
    senderId: String,
    deviceToken: String,
    imageUrl: String?,
    name: String?,
    newMessageChats: Boolean
) {
    var reply by rememberSaveable {
        mutableStateOf("")
    }


    val userToken = remember { mutableStateOf("") }
    var isReplySent by remember { mutableStateOf(false) }
    var showCancelButton by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val onAddChat = {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userToken.value = task.result ?: ""
            }
            if (senderId != null && deviceToken != null) {
                vm.onAddChats(
                    senderId = senderId,
                    reply = reply,
                    userDeviceToken = userToken.value,
                    chatPartnerDeviceToken = deviceToken
                ) { chatId -> // Получаем chatId через callback
                    if (name != null && newMessageChats) {
                        vm.sendMessage(
                            reply,
                            deviceToken,
                            name,
                            chatId // Теперь передаем реальный chatId
                        )
                    }
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss.invoke()
            },
            confirmButton = {
                if (!isReplySent) {
                    TextButton(
                        onClick = {
                            if (reply.isEmpty()) {
                                Toast.makeText(context, "Пожалуйста, введите сообщение", Toast.LENGTH_SHORT).show()
                            } else {
                                coroutineScope.launch {
                                    onAddChat()
                                    isReplySent = true
                                    showCancelButton = false
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "Ок",
                            fontSize = 18.sp
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(bottom = 10.dp)
                        ,
                        contentAlignment = Alignment.Center
                    ){
                        OutlinedButton(
                            onClick = {
                                navigateTo()
                            },
                        ) {
                            Text(
                                text = "Перейти в чат",
                                fontSize = 18.sp
                            )
                        }
                    }

                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier.width(370.dp),
            dismissButton = {
                if (showCancelButton) {
                    TextButton(onClick = {
                        onDismiss.invoke()
                    }) {
                        Text(
                            text = "Отмена",
                            fontSize = 18.sp
                        )
                    }
                }
            },
            text = {
                if (!isReplySent) {
                    OutlinedTextField(
                        value = reply,
                        onValueChange = { reply = it },
                        maxLines = 3,
                        placeholder = {
                            Text(text = "Введите сообщение:")
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    )
                }
            }
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val color = if (isPressed) GreyCard else Color.White

    Button(
        onClick = onButtonClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(color),
        border = BorderStroke(1.dp, Color.Black),
    ) {
        Text(
            text = buttonText,
            color = Color.Black
        )
    }
}



