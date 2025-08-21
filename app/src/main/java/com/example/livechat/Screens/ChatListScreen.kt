package com.example.livechat.Screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.livechat.CommonProgressBar
import com.example.livechat.CommonRow
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.TitleText
import com.example.livechat.navigateTo
import kotlinx.coroutines.delay

@Composable
fun ChatListScreen(
    navController: NavController,
    vm: LCViewModel,
    isDarkTheme: Boolean
) {
    val inProgress = vm.inProcessChats
    if (inProgress.value) {
        CommonProgressBar()
    } else {
        val chats = vm.chats.value
        val userData = vm.userData.value
        val msgBadgeVisible = userData?.chats?.values?.any { it is Boolean && it } ?: false

        val sortedChats = chats.sortedByDescending { it.timeLastMessage }

        val scrollState = rememberLazyListState()

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

        val totalItems = sortedChats.size
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

        Scaffold(
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    TitleText(txt = "Чаты")
                    if (sortedChats.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Чаты не найдены")
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                state = scrollState
                            ) {
                                items(sortedChats) { chat ->
                                    val chatUser = if (chat.user1.userId == userData?.userId) {
                                        chat.user2
                                    } else {
                                        chat.user1
                                    }
                                    val localTime = vm.convertMillisToLocalTime(chat.timeLastMessage)
                                    val boldValue = userData?.chats?.get(chat.chatId)
                                    val boldtext = when (boldValue) {
                                        is Boolean -> boldValue
                                        else -> false
                                    }
                                    val displayMessage = if (chat.idLastMessage == userData?.userId) {
                                        val lastMessage = if (chat.lastMessage.isNullOrEmpty()) "Изображение" else chat.lastMessage
                                        "Вы: $lastMessage"
                                    } else {
                                        if (chat.lastMessage.isNullOrEmpty()) "Изображение" else chat.lastMessage
                                    }
                                    CommonRow(
                                        boldText = boldtext,
                                        isDarkTheme = isDarkTheme,
                                        imageUrl = chatUser.imageUrl,
                                        name = chatUser.name,
                                        message = displayMessage,
                                        currentTime = localTime ?: ""
                                    ) {
                                        chat.chatId?.let {
                                            navigateTo(
                                                navController,
                                                DestinationScreen.SingleChat.createRout(id = it)
                                            )
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
                    if (userData != null) {
                        BottomNavigationMenu(
                            selectedItem = BottomNavigationItem.CHATLIST,
                            navController = navController,
                            newMessageCount = userData.newMessageCity,
                            badgeVisible = userData.newMessageDepts,
                            msgBadgeVisible = msgBadgeVisible,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
        )
    }
}
