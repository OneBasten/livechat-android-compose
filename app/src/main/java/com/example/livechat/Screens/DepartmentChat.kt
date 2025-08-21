package com.example.livechat.Screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.livechat.CommonImageGroupChat
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.UserProfInfoChats
import com.example.livechat.ui.theme.GreyCard
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentChat(
    navController: NavController,
    vm: LCViewModel,
    itemName: String?,
    cityName: String?,
    isDarkTheme: Boolean
) {
    val escapedItemName = itemName?.let { escapeDepartmenttId(it) }
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val onEditClick: () -> Unit = { showDialog.value = true }
    val onDismiss: () -> Unit = { showDialog.value = false }
    val userData = vm.userData.value
    val userId = userData?.userId

    val addNewFieldToUser = {
        if (escapedItemName != null) {
            if (userData != null) {
                userData.userId?.let { vm.addNewFieldToUser(escapedItemName, it) }
            }
        }
    }

    var departmentChatMessage = vm.messageForDepartmentChats
    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    val coroutineScope = rememberCoroutineScope()
    val msgBadgeVisible = userData?.chats?.values?.any { it is Boolean && it } ?: false

    var isScrollbarVisible by remember { mutableStateOf(false) }

    val scrollbarAlpha by animateFloatAsState(
        targetValue = if (isScrollbarVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            isScrollbarVisible = true
        } else {
            delay(1000)
            isScrollbarVisible = false
        }
    }

    LaunchedEffect(Unit) {
        isScrollbarVisible = true
        delay(1000)
        isScrollbarVisible = false
    }

    val totalItems = departmentChatMessage.value.size
    val visibleItems = scrollState.layoutInfo.visibleItemsInfo.size
    val firstVisibleItemIndex = scrollState.firstVisibleItemIndex
    val scrollOffset = scrollState.firstVisibleItemScrollOffset

    val scrollbarHeight by remember(totalItems, visibleItems) {
        derivedStateOf {
            if (totalItems == 0) 0f
            else (visibleItems.toFloat() / totalItems.toFloat()) * scrollState.layoutInfo.viewportSize.height
        }
    }

    val scrollbarPosition by remember(firstVisibleItemIndex, scrollOffset, totalItems, visibleItems) {
        derivedStateOf {
            if (totalItems == 0) 0f
            else {
                val scrollableHeight = (totalItems - visibleItems).toFloat()
                val firstItemSize = scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 1f
                val scrollProgress = (firstVisibleItemIndex.toFloat() + scrollOffset.toFloat() / firstItemSize.toFloat()) / scrollableHeight
                scrollProgress * (scrollState.layoutInfo.viewportSize.height - scrollbarHeight)
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (itemName != null) {
            if (escapedItemName != null) {
                vm.populateMessagesDepartment(escapedItemName)
            }
            if (escapedItemName != null) {
                if (userId != null) {
                    vm.resetFieldForAllUsersExcept(escapedItemName, userId, false)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (itemName != null) {
                        Text(
                            text = itemName,
                            fontSize = 17.sp,
                            maxLines = 2,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(DestinationScreen.Department.route) }) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                if (departmentChatMessage.value.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Сообщения не найдены")
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(),
                            state = scrollState
                        ) {
                            items(departmentChatMessage.value.reversed()) { msg ->
                                val localTime = vm.convertMillisToLocalTime(msg.normtime)
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(15.dp),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 5.dp
                                    )
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .padding(2.dp)
                                                .clickable {
                                                    if (userData != null) {
                                                        if (msg.senderId == userData.userId) {
                                                            navController.navigate(DestinationScreen.Profile.route)
                                                        } else {
                                                            navController.navigate(
                                                                UserProfInfoChats(
                                                                    senderId = msg.senderId ?: "",
                                                                    name = msg.name ?: "",
                                                                    imageUrl = msg.imageUrl ?: "",
                                                                    city = msg.city ?: "",
                                                                    birthday = msg.birthday ?: "",
                                                                    gender = msg.gender ?: "",
                                                                    msg.deviceToken,
                                                                    newMessageChats = msg.newMessageChats ?: false
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                        ) {
                                            if (msg.imageUrl != null) {
                                                CommonImageGroupChat(data = msg.imageUrl ?: "")
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .padding(3.dp)
                                                        .size(40.dp)
                                                        .clip(CircleShape),
                                                    tint = if (isDarkTheme) Color.White else Color.Black
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = msg.name ?: "",
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    fontSize = 11.sp,
                                                    text = localTime ?: ""
                                                )
                                            }
                                        }
                                        var expanded by remember { mutableStateOf(msg.isExpanded) }
                                        if (expanded) {
                                            Text(
                                                text = msg.message ?: "",
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .padding(start = 10.dp, end = 5.dp, bottom = 10.dp)
                                            )
                                        } else {
                                            val message = msg.message
                                            if (message != null) {
                                                val displayMessage = if (message.length > 100) {
                                                    message.take(100) + "..."
                                                } else {
                                                    message
                                                }

                                                val annotatedString = buildAnnotatedString {
                                                    append(displayMessage)
                                                    if (message.length > 100) {
                                                        append(" ")
                                                        pushStringAnnotation("show_more", "click")
                                                        withStyle(style = SpanStyle(color = Color.Blue)) {
                                                            append("показать еще")
                                                        }
                                                        pop()
                                                    }
                                                }

                                                Text(
                                                    text = annotatedString,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .padding(
                                                            start = 10.dp,
                                                            end = 5.dp,
                                                            bottom = 10.dp
                                                        )
                                                        .clickable {
                                                            if (message.length > 100) {
                                                                expanded = true
                                                            }
                                                        }
                                                )
                                            }
                                            LaunchedEffect(expanded) {
                                                msg.isExpanded = expanded
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isScrollbarVisible) {
                            Canvas(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight()
                                    .width(8.dp)
                                    .graphicsLayer(alpha = scrollbarAlpha)
                            ) {
                                drawRect(
                                    color = Color.Gray.copy(alpha = 0.5f),
                                    topLeft = Offset(size.width - 4.dp.toPx(), scrollbarPosition),
                                    size = Size(8.dp.toPx(), scrollbarHeight)
                                )
                            }
                        }
                    }
                }

                if (userId != null) {
                    if (escapedItemName != null) {
                        if (cityName != null) {
                            AlertDialogDepartment(
                                addNewFieldToUser = addNewFieldToUser,
                                showDialog = showDialog.value,
                                onEditClick = onEditClick,
                                onDismiss = onDismiss,
                                context = context,
                                scrollState,
                                coroutineScope,
                                userId,
                                escapedItemName,
                                vm,
                                cityName
                            )
                        }
                    }
                }

                if (userData != null) {
                    BottomNavigationMenu(
                        selectedItem = BottomNavigationItem.DEPARTMENT,
                        navController = navController,
                        userData.newMessageCity,
                        badgeVisible = userData.newMessageDepts,
                        msgBadgeVisible = msgBadgeVisible,
                        isDarkTheme
                    )
                }
            }
        }
    )
}

@Composable
fun AlertDialogDepartment(
    addNewFieldToUser: () -> Unit,
    showDialog: Boolean,
    onEditClick: () -> Unit,
    onDismiss: () -> Unit,
    context: Context,
    scrollState: LazyListState,
    coroutineScope: CoroutineScope,
    userId: String,
    escapedItemName: String,
    vm: LCViewModel,
    cityName: String
) {
    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val deviceToken = remember { mutableStateOf("") }
    val onSendReply = {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                deviceToken.value = task.result ?: ""
                vm.onSendReplyDepartment(
                    departmentId = escapedItemName,
                    message = reply,
                    cityId = cityName,
                    deviceToken = deviceToken.value
                )
                reply = ""
            } else {
                Log.e("TokenError", "Failed to get device token")
            }
        }

    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss.invoke()
            },
            confirmButton = {
                TextButton(onClick = {
                    if (reply.isEmpty()) {
                        Toast.makeText(context, "Пожалуйста, введите сообщение", Toast.LENGTH_SHORT).show()
                    } else {
                            coroutineScope.launch {
                                scrollState.scrollToItem(0)
                            }
                            onSendReply()
                            onDismiss.invoke()
                    }
                }) {
                    Text(
                        text = "Ок",
                        fontSize = 18.sp
                    )
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.width(370.dp),
            dismissButton = {
                TextButton(onClick = {
                    onDismiss.invoke()
                }) {
                    Text(
                        text = "Отмена",
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                OutlinedTextField(
                    value = reply,
                    onValueChange = {reply = it},
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
        )
    }

    val firstInteractionSource = remember { MutableInteractionSource() }
    val secondInteractionSource = remember { MutableInteractionSource() }

    val isFirstPressed by firstInteractionSource.collectIsPressedAsState()
    val isSecondPressed by secondInteractionSource.collectIsPressedAsState()

    val firstButtonColor = if (isFirstPressed) GreyCard else Color.White
    val secondButtonColor = if (isSecondPressed) GreyCard else Color.White

    var isSubscribed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    LaunchedEffect(userId) {
        vm.checkIfFieldExists(escapedItemName, userId) { exists ->
            isSubscribed = exists
        }
    }
    Row {
        Button(
            onClick = {
                onEditClick()
            },
            interactionSource = firstInteractionSource,
            colors = ButtonDefaults.buttonColors(firstButtonColor),
            border = BorderStroke(1.dp, Color.Black),
            modifier = Modifier
                .weight(1f)
                .padding(start = 5.dp)
        ) {
            Text(
                text = "Введите Ваше сообщение",
                color = Color.Black
            )
        }
        if (!isSubscribed) {
            Button(
                onClick = {
                    isSubscribed = true
                    Toast.makeText(context, "Вы подписались на обновления", Toast.LENGTH_SHORT).show()
                    addNewFieldToUser()
                },
                interactionSource = secondInteractionSource,
                colors = ButtonDefaults.buttonColors(secondButtonColor),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier
                    .padding(start = 5.dp, end = 5.dp)
            ) {
                Text(
                    text = "+ подписаться",
                    color = Color.Black
                )
            }
        } else {
            Text(
                text = "✔ Подписаны",
                fontSize = 15.sp,
                modifier = Modifier
                    .clickable {
                        showDialog = true
                    }
                    .padding(start = 5.dp, end = 5.dp, top = 9.dp)
            )
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.removeFieldFromUser(escapedItemName, userId)
                    isSubscribed = false
                    showDialog = false
                },
                    modifier = Modifier.padding(end = 7.dp)
                    ) {
                    Text(
                        "Отписаться",
                        fontSize = 17.sp
                        )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text(
                        "Отмена",
                        fontSize = 17.sp)
                }
            }
        )
    }
}

fun escapeDepartmenttId(departmentId: String): String {
    return departmentId
        .replace(":", "_")
        .replace(".", "_")
        .replace("/", "_")
        .replace("[", "_")
        .replace("]", "_")
        .replace("#", "_")
        .replace("?", "_")
        .replace("\"", "'")
}