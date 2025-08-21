package com.example.livechat.Screens

import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.text.ClipboardManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.livechat.CommonDivider
import com.example.livechat.CommonImage
import com.example.livechat.LCViewModel
import com.example.livechat.UserProfileInfo
import com.example.livechat.data.Message
import com.example.livechat.ui.theme.GreyMessage
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.livechat.DestinationScreen
import com.example.livechat.FullScreenImage
import com.example.livechat.data.USER_NODE
import com.example.livechat.ui.theme.MainBlue
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SingleChatScreen(navController: NavController, vm: LCViewModel, chatId: String, isDarkTheme: Boolean) {
    var reply by rememberSaveable { mutableStateOf("") }
    var replyToMessage by remember { mutableStateOf<Message?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    val deviceToken = remember { mutableStateOf("") }
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            deviceToken.value = task.result ?: ""
        } else {
            Log.e("TokenError", "Failed to get device token")
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val chatMessage = vm.chatMessages
    val myUser = vm.userData.value
    val currentChat = vm.chats.value.first { it.chatId == chatId }
    val chatUser = if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1
    val deviceTokenUser = if (deviceToken.value == currentChat.user1.deviceToken) currentChat.user2 else currentChat.user1
    Log.d("deviceTokenUser", "${deviceTokenUser.deviceToken}")

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }
    val onSendReply = {
        if (selectedImageUri != null) {
            vm.onSendImage(chatId, selectedImageUri!!, reply, replyToMessage?.timestamp, replyToMessage?.message, replyToMessage?.sendBy)
            selectedImageUri = null
            if (myUser != null && chatUser.newMessageChats == true) {
                deviceTokenUser.deviceToken?.let { token ->
                    myUser.name?.let { name ->
                        vm.sendMessage(
                            "Изображение",
                            token,
                            name,
                            chatId
                        )
                    }
                }
            }
        } else {
            vm.onSendReply(chatId, reply, replyToMessage?.timestamp, replyToMessage?.message, replyToMessage?.sendBy)
            if (myUser != null && chatUser.newMessageChats == true) {
                deviceTokenUser.deviceToken?.let { token ->
                    myUser.name?.let { name ->
                        vm.sendMessage(
                            reply,
                            token,
                            name,
                            chatId
                        )
                    }
                }
            }
        }
        reply = ""
        replyToMessage = null
        if (myUser != null) {
            myUser.userId?.let { vm.updateChatStatus(chatId, it) }
        }
    }

    LaunchedEffect(key1 = Unit) {
        vm.populateMessages(chatId)
    }
    LaunchedEffect(key1 = Unit) {
        if (myUser != null) {
            myUser.userId?.let { vm.userChats(chatId, it, false) }
        }
    }

    val maxLength = 9
    val localFocusManager = LocalFocusManager.current
    BackHandler {
        vm.depopulateMessage()
        navController.navigate(DestinationScreen.ChatList.route)
        if (myUser != null) {
            myUser.userId?.let { vm.userChats(chatId, it, false) }
        }
    }

    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = chatMessage.value.size)
    var highlightedMessageId by remember { mutableStateOf<Long?>(null) }

    fun scrollToMessageAndHighlight(messageId: Long) {
        val index = chatMessage.value.indexOfFirst { it.timestamp?.toLong() == messageId }
        if (index != -1) {
            val reversedIndex = chatMessage.value.size - 1 - index
            coroutineScope.launch {
                scrollState.scrollToItem(reversedIndex)
                highlightedMessageId = messageId
                delay(1000)
                highlightedMessageId = null
            }
        }
    }

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

    val totalItems = chatMessage.value.size
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
                (1f - scrollProgress) * (scrollState.layoutInfo.viewportSize.height - scrollbarHeight)
            }
        }
    }

    var isScrollToBottomButtonVisible by remember { mutableStateOf(false) }
    LaunchedEffect(scrollState.firstVisibleItemIndex) {
        isScrollToBottomButtonVisible = scrollState.firstVisibleItemIndex > 0
    }
    val chatUserStatus = remember { mutableStateOf<Pair<Boolean, Long?>?>(null) }

    // Слушаем изменения статуса пользователя в чате
    LaunchedEffect(key1 = chatId) {
        val currentChat = vm.chats.value.firstOrNull { it.chatId == chatId }
        val otherUserId = if (currentChat?.user1?.userId == vm.userData.value?.userId) {
            currentChat?.user2?.userId
        } else {
            currentChat?.user1?.userId
        }

        otherUserId?.let { userId ->
            vm.db.collection(USER_NODE).document(userId)
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.let {
                        val isOnline = it.getBoolean("isOnline") ?: false
                        val lastSeen = it.getLong("lastSeen")
                        chatUserStatus.value = Pair(isOnline, lastSeen)
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .imePadding()
                .fillMaxSize()
                .focusable()
                .wrapContentHeight()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        localFocusManager.clearFocus()
                    })
                }
        ) {
            ChatHeader(
                navController,
                isDarkTheme,
                city = chatUser.city ?: "",
                birthday = chatUser.birthday ?: "",
                gender = chatUser.gender ?: "",
                chatId = chatId,
                userId = chatUser.userId ?: "",
                banUser = chatUser.banUser ?: false,
                name = chatUser.name ?: "",
                imageUrl = chatUser.imageUrl ?: "",
                isOnline = chatUserStatus.value?.first ?: false,
                lastSeen = chatUserStatus.value?.second,
                vm = vm
            ) {
                vm.depopulateMessage()
                navController.navigate(DestinationScreen.ChatList.route)
                if (myUser != null) {
                    myUser.userId?.let { vm.userChats(chatId, it, false) }
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = scrollState,
                    reverseLayout = true
                ) {
                    items(chatMessage.value.reversed()) { msg ->
                        val localTime = vm.convertMillisToLocalTime(msg.normtime)
                        val isHighlighted = msg.timestamp?.toLong() == highlightedMessageId

                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (msg.sendBy == myUser?.userId ?: "") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    var showMenu by remember { mutableStateOf(false) }

                                    val offsetX = remember { Animatable(0f) }
                                    val density = LocalDensity.current
                                    val maxOffset = with(density) { -50.dp.toPx() }

                                    Box(
                                        modifier = Modifier
                                            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onTap = {
                                                        localFocusManager.clearFocus()
                                                    },
                                                    onLongPress = { showMenu = true }
                                                )
                                            }
                                            .pointerInput(Unit) {
                                                detectHorizontalDragGestures { change, dragAmount ->
                                                    val newOffset = (offsetX.value + dragAmount).coerceIn(maxOffset, 0f)
                                                    coroutineScope.launch {
                                                        offsetX.snapTo(newOffset)

                                                        if (newOffset <= maxOffset) {
                                                            replyToMessage = msg
                                                        }

                                                        if (newOffset < 0) {
                                                            offsetX.animateTo(0f, animationSpec = tween(durationMillis = 500))
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        ElevatedCard(
                                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                                            modifier = Modifier
                                                .padding(end = 5.dp, start = 30.dp)
                                                .zIndex(1f),
                                            colors = CardDefaults.cardColors(MainBlue)
                                        ) {
                                            Column {
                                                if (msg.replyToMessageId != null) {
                                                    val repliedMessage = chatMessage.value.find {
                                                        it.timestamp?.toLong() == msg.replyToMessageId.toLongOrNull()
                                                    }
                                                    if (repliedMessage != null) {
                                                        Row(
                                                            modifier = Modifier
                                                                .height(IntrinsicSize.Min)
                                                                .padding(8.dp)
                                                                .clickable {
                                                                    repliedMessage.timestamp?.let {
                                                                        scrollToMessageAndHighlight(it.toLong())
                                                                    }
                                                                },
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .width(4.dp)
                                                                    .fillMaxHeight()
                                                                    .background(MaterialTheme.colorScheme.onSurface)
                                                            )

                                                            Spacer(modifier = Modifier.width(8.dp))

                                                            Column(modifier = Modifier.weight(1f)) {
                                                                val repliedUserName = if (repliedMessage.sendBy == myUser?.userId) {
                                                                    myUser?.name ?: "Вы"
                                                                } else {
                                                                    chatUser.name ?: "Неизвестный пользователь"
                                                                }

                                                                Text(
                                                                    text = repliedUserName,
                                                                    style = MaterialTheme.typography.bodyMedium
                                                                )

                                                                if (repliedMessage.message?.isNotEmpty() == true) {
                                                                    val truncatedText = if (repliedMessage.message.length > maxLength) {
                                                                        repliedMessage.message.take(maxLength) + "..."
                                                                    } else {
                                                                        repliedMessage.message
                                                                    }
                                                                    Text(
                                                                        text = truncatedText,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                        color = Color.White // Белый текст на синем фоне
                                                                    )
                                                                } else if (repliedMessage.imageUrl?.isNotEmpty() == true) {
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Image,
                                                                            contentDescription = "Изображение",
                                                                            modifier = Modifier.size(16.dp),
                                                                            tint = Color.White
                                                                        )
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text(
                                                                            text = "Изображение",
                                                                            maxLines = 1,
                                                                            overflow = TextOverflow.Ellipsis,
                                                                            fontStyle = FontStyle.Italic,
                                                                            color = Color.White
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = msg.message ?: "",
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                                                )

                                                msg.imageUrl?.let { imageUrl ->
                                                    AsyncImage(
                                                        model = imageUrl,
                                                        contentDescription = "Изображение",
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .padding(8.dp)
                                                            .clickable {
                                                                fullScreenImageUrl = imageUrl
                                                            },
                                                        contentScale = ContentScale.FillWidth
                                                    )
                                                }

                                                Text(
                                                    fontSize = 12.sp,
                                                    text = localTime ?: "",
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }

                                        if (isHighlighted) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(end = 5.dp, start = 30.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.Gray.copy(alpha = 0.3f))
                                                    .matchParentSize()
                                                    .zIndex(2f)
                                            )
                                        }
                                    }

                                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                        DropdownMenuItem(
                                            text = { Text(text = "Ответить") },
                                            onClick = {
                                                replyToMessage = msg
                                                showMenu = false
                                            })
                                        DropdownMenuItem(
                                            text = { Text(text = "Копировать") },
                                            onClick = {
                                                msg.message?.let { message ->
                                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                    val clipData = ClipData.newPlainText("message", message)
                                                    clipboardManager.setPrimaryClip(clipData)
                                                    Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                                                }
                                                showMenu = false
                                            })
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    var showMenu by remember { mutableStateOf(false) }

                                    val offsetX = remember { Animatable(0f) }
                                    val density = LocalDensity.current
                                    val maxOffset = with(density) { -50.dp.toPx() }

                                    Box(
                                        modifier = Modifier
                                            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onTap = {
                                                        localFocusManager.clearFocus()
                                                    },
                                                    onLongPress = { showMenu = true }
                                                )
                                            }
                                            .pointerInput(Unit) {
                                                detectHorizontalDragGestures { change, dragAmount ->
                                                    val newOffset = (offsetX.value + dragAmount).coerceIn(maxOffset, 0f)
                                                    coroutineScope.launch {
                                                        offsetX.snapTo(newOffset)

                                                        if (newOffset <= maxOffset) {
                                                            replyToMessage = msg
                                                        }

                                                        if (newOffset < 0) {
                                                            offsetX.animateTo(0f, animationSpec = tween(durationMillis = 500))
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        ElevatedCard(
                                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                                            modifier = Modifier
                                                .padding(end = 5.dp, start = 10.dp)
                                                .zIndex(1f)
                                        ) {
                                            Column {
                                                if (msg.replyToMessageId != null) {
                                                    val repliedMessage = chatMessage.value.find {
                                                        it.timestamp?.toLong() == msg.replyToMessageId.toLongOrNull()
                                                    }
                                                    if (repliedMessage != null) {
                                                        Row(
                                                            modifier = Modifier
                                                                .height(IntrinsicSize.Min)
                                                                .padding(8.dp)
                                                                .clickable {
                                                                    repliedMessage.timestamp?.let {
                                                                        scrollToMessageAndHighlight(it.toLong())
                                                                    }
                                                                },
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .width(4.dp)
                                                                    .fillMaxHeight()
                                                                    .background(MaterialTheme.colorScheme.onSurface) // Серый/черный цвет для сообщений собеседника
                                                            )

                                                            Spacer(modifier = Modifier.width(8.dp))

                                                            Column(modifier = Modifier.weight(1f)) {
                                                                val repliedUserName = if (repliedMessage.sendBy == myUser?.userId) {
                                                                    myUser?.name ?: "Вы"
                                                                } else {
                                                                    chatUser.name ?: "Неизвестный пользователь"
                                                                }

                                                                Text(
                                                                    text = repliedUserName,
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                    color = MaterialTheme.colorScheme.onSurface // Основной цвет темы
                                                                )

                                                                if (repliedMessage.message?.isNotEmpty() == true) {
                                                                    val truncatedText = if (repliedMessage.message.length > maxLength) {
                                                                        repliedMessage.message.take(maxLength) + "..."
                                                                    } else {
                                                                        repliedMessage.message
                                                                    }
                                                                    Text(
                                                                        text = truncatedText,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                        color = GreyMessage // Серый текст
                                                                    )
                                                                } else if (repliedMessage.imageUrl?.isNotEmpty() == true) {
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Image,
                                                                            contentDescription = "Изображение",
                                                                            modifier = Modifier.size(16.dp),
                                                                            tint = GreyMessage
                                                                        )
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text(
                                                                            text = "Изображение",
                                                                            maxLines = 1,
                                                                            overflow = TextOverflow.Ellipsis,
                                                                            fontStyle = FontStyle.Italic,
                                                                            color = GreyMessage
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = msg.message ?: "",
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                                                )

                                                msg.imageUrl?.let { imageUrl ->
                                                    AsyncImage(
                                                        model = imageUrl,
                                                        contentDescription = "Изображение",
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .padding(8.dp)
                                                            .clickable {
                                                                fullScreenImageUrl = imageUrl
                                                            },
                                                        contentScale = ContentScale.FillWidth
                                                    )
                                                }

                                                Text(
                                                    fontSize = 12.sp,
                                                    text = localTime ?: "",
                                                    color = GreyMessage,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }

                                        if (isHighlighted) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(end = 5.dp, start = 10.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.Gray.copy(alpha = 0.3f))
                                                    .matchParentSize()
                                                    .zIndex(2f)
                                            )
                                        }
                                    }

                                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                        DropdownMenuItem(
                                            text = { Text(text = "Ответить") },
                                            onClick = {
                                                replyToMessage = msg
                                                showMenu = false
                                            })
                                        DropdownMenuItem(
                                            text = { Text(text = "Копировать") },
                                            onClick = {
                                                msg.message?.let { message ->
                                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                    val clipData = ClipData.newPlainText("message", message)
                                                    clipboardManager.setPrimaryClip(clipData)
                                                    Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                                                }
                                                showMenu = false
                                            })
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

                if (isScrollToBottomButtonVisible) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    scrollState.scrollToItem(0)
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = "Scroll to bottom"
                            )
                        }
                    }
                }
            }

            if (replyToMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(8.dp)
                        .clickable {
                            replyToMessage!!.timestamp?.let {
                                scrollToMessageAndHighlight(it.toLong())
                            }
                        }
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                if (replyToMessage?.sendBy == myUser?.userId) MaterialTheme.colorScheme.onSurface
                                else Color.Blue
                            )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val userName = if (replyToMessage?.sendBy == myUser?.userId) {
                            myUser?.name ?: "Вы"
                        } else {
                            chatUser.name ?: "Неизвестный пользователь"
                        }

                        Text(
                            text = userName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (replyToMessage?.sendBy == myUser?.userId) MaterialTheme.colorScheme.onSurface
                            else Color.Blue
                        )
                        if (replyToMessage?.message?.isNotEmpty() == true) {
                            Text(
                                text = replyToMessage?.message ?: "",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (replyToMessage?.imageUrl?.isNotEmpty() == true) {
                            Text(
                                text = "Изображение",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontStyle = FontStyle.Italic,
                                color = Color.Gray
                            )
                    }
                    }
                    IconButton(
                        onClick = { replyToMessage = null }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Отмена ответа")
                    }
                }
            }

            CommonDivider()

            val currentUserInChat = if (currentChat.user1.userId == myUser?.userId) currentChat.user1 else currentChat.user2
            val otherUserInChat = if (currentChat.user1.userId == myUser?.userId) currentChat.user2 else currentChat.user1

            if (otherUserInChat.banUser == true) {
                Text(
                    text = "Пользователь заблокирован",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            } else if (currentUserInChat.banUser == true) {
                Text(
                    text = "Пользователь ограничил круг лиц, которые могут ему написать",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    if (selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .size(80.dp) // Размер квадрата
                                .padding(8.dp)
                                .clickable {
                                    fullScreenImageUrl = selectedImageUri.toString()
                                    localFocusManager.clearFocus()
                                }

                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Выбранное изображение",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(20.dp)
                                    .background(Color.Gray.copy(alpha = 0.7f), CircleShape)
                                    .clickable { selectedImageUri = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Удалить изображение",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { launcher.launch("image/*") }) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = "Выбрать изображение")
                        }

                        OutlinedTextField(
                            value = reply,
                            onValueChange = { reply = it },
                            maxLines = 3,
                            placeholder = {
                                Text(text = "Введите сообщение:")
                            },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            shape = RoundedCornerShape(35.dp),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )

                        if (reply.isEmpty() && selectedImageUri == null) {
                            IconButton(onClick = { }) {
                                Icon(imageVector = Icons.Filled.Send, contentDescription = null)
                            }
                        } else {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    scrollState.scrollToItem(0)
                                }
                                onSendReply()
                            }) {
                                Icon(imageVector = Icons.Filled.Send, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        if (fullScreenImageUrl != null) {
            FullScreenImage(
                imageUrl = fullScreenImageUrl!!,
                onClose = { fullScreenImageUrl = null }
            )
        }
    }
}

@Composable
fun ChatHeader(
    navController: NavController,
    isDarkTheme: Boolean,
    name: String,
    imageUrl: String?,
    city: String,
    birthday: String,
    gender: String,
    chatId: String,
    userId: String,
    banUser: Boolean,
    isOnline: Boolean, // Новый параметр
    lastSeen: Long?, // Новый параметр
    vm: LCViewModel,
    onBackClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.ArrowBackIosNew,
            contentDescription = null,
            modifier = Modifier
                .clickable { onBackClicked() }
                .padding(8.dp)
        )

        Box(modifier = Modifier.clickable {
            navController.navigate(UserProfileInfo(
                name = name,
                imageUrl = imageUrl,
                city = city,
                birthday = birthday,
                gender = gender,
                chatId,
                userId,
                banUser
            ))
        }) {
            Row(
                modifier = Modifier.wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Аватар пользователя
                if (imageUrl != null && imageUrl.isNotEmpty()) {
                    CommonImage(
                        data = imageUrl,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(50.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(
                                BorderStroke(width = 1.dp, if (isDarkTheme) Color.White else Color.Black),
                                CircleShape
                            ),
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                }

                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(text = name, fontWeight = FontWeight.Bold)

                    // Отображение статуса
                    if (isOnline) {
                        Text(
                            text = "online",
                            fontSize = 12.sp
                        )
                    } else {
                        lastSeen?.let {
                            val lastSeenText = vm.convertMillisToLocalTime(it) ?: "был(а) недавно"
                            Text(
                                text = "был(а) $lastSeenText",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}