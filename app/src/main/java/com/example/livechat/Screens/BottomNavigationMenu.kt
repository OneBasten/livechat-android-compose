package com.example.livechat.Screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.livechat.DestinationScreen
import com.example.livechat.navigateTo
import com.example.livechat.ui.theme.GreyMessage

enum class BottomNavigationItem(val icon: ImageVector, val title: String, val navDestination: DestinationScreen) {
    DEPARTMENT(Icons.Filled.Store, "Филиал", DestinationScreen.Department),
    CITY(Icons.Filled.LocationCity, "Город", DestinationScreen.City),
    CHATLIST(Icons.Filled.Chat, "Чаты", DestinationScreen.ChatList),
    PROFILE(Icons.Filled.Person, "Профиль", DestinationScreen.Profile)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationMenu(
    selectedItem: BottomNavigationItem,
    navController: NavController,
    newMessageCount: Boolean,
    badgeVisible: Boolean,
    msgBadgeVisible: Boolean,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        for (item in BottomNavigationItem.entries) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable { navigateTo(navController, item.navDestination.route) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box() {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = if (item == selectedItem) {
                                if (isDarkTheme) Color.White else Color.Black
                            } else {
                                if (isDarkTheme) GreyMessage else Color.Gray
                            }
                        )

                        if ((item == BottomNavigationItem.CITY && newMessageCount) ||
                            (item == BottomNavigationItem.DEPARTMENT && badgeVisible) ||
                            (item == BottomNavigationItem.CHATLIST && msgBadgeVisible)
                            )  {
                            val infiniteTransition = rememberInfiniteTransition()
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )

                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(8.dp)
                                    .graphicsLayer(alpha = alpha),
                                containerColor = Color.Red
                            )
                        }
                    }

                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        color = if (item == selectedItem) {
                            if (isDarkTheme) Color.White else Color.Black
                        } else {
                            if (isDarkTheme) GreyMessage else Color.Gray
                        }
                    )
                }
            }
        }
    }
}