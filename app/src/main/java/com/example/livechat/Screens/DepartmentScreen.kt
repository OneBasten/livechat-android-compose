package com.example.livechat.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.livechat.LCViewModel
import com.example.livechat.TitleText
import com.example.livechat.DepartmentChat
import com.example.livechat.data.FavoritesManager

import com.example.livechat.data.cityDepartmentList
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentScreen(navController: NavController, vm: LCViewModel, isDarkTheme: Boolean) {
    val context = LocalContext.current
    val userData = vm.userData.value
    val city by rememberSaveable { mutableStateOf(userData?.city ?: "") }

    var selectedDepartment by remember { mutableStateOf(vm.selectedDepartment) }

    val departments = listOf("Администрация", "IT-департамент", "Финансы", "HR", "Маркетинг")
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFieldVisible by remember { mutableStateOf(false) }
    val favoritesManager = remember { FavoritesManager(context) }
    val favoriteItems = remember { mutableStateListOf<String>() }
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        favoriteItems.addAll(favoritesManager.loadFavorites())
        if (userData != null) {
        }
    }

    var showFavorites by remember { mutableStateOf(false) }
    val msgBadgeVisible = userData?.chats?.values?.any { it is Boolean && it } ?: false

    val departmentList = when (selectedDepartment) {
        "Администрация" -> cityDepartmentList.find { it.cityName == city }?.administration ?: emptyList()
        "IT-департамент" -> cityDepartmentList.find { it.cityName == city }?.itDepartment ?: emptyList()
        "Финансы" -> cityDepartmentList.find { it.cityName == city }?.finance ?: emptyList()
        "HR" -> cityDepartmentList.find { it.cityName == city }?.hr ?: emptyList()
        "Маркетинг" -> cityDepartmentList.find { it.cityName == city }?.marketing ?: emptyList()
        else -> emptyList()
    }

    val filteredDepartmentList = if (showFavorites) {
        departmentList.filter { favoriteItems.contains(it) }
    } else {
        departmentList.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    val listState = rememberLazyListState()

    var isScrollbarVisible by remember { mutableStateOf(false) }

    val scrollbarAlpha by animateFloatAsState(
        targetValue = if (isScrollbarVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
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

    val totalItems = filteredDepartmentList.size
    val visibleItems = listState.layoutInfo.visibleItemsInfo.size
    val firstVisibleItemIndex = listState.firstVisibleItemIndex
    val scrollOffset = listState.firstVisibleItemScrollOffset

    val scrollbarHeight by remember(totalItems, visibleItems) {
        derivedStateOf {
            if (totalItems == 0) 0f
            else (visibleItems.toFloat() / totalItems.toFloat()) * listState.layoutInfo.viewportSize.height
        }
    }

    val scrollbarPosition by remember(firstVisibleItemIndex, scrollOffset, totalItems, visibleItems) {
        derivedStateOf {
            if (totalItems == 0) 0f
            else {
                val scrollableHeight = (totalItems - visibleItems).toFloat()
                val firstItemSize = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 1f
                val scrollProgress = (firstVisibleItemIndex.toFloat() + scrollOffset.toFloat() / firstItemSize.toFloat()) / scrollableHeight
                scrollProgress * (listState.layoutInfo.viewportSize.height - scrollbarHeight)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TitleText(txt = city) },
                actions = {
                    AnimatedVisibility(visible = !isSearchFieldVisible) {
                        IconButton(onClick = { isSearchFieldVisible = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск")
                        }
                    }
                    AnimatedVisibility(visible = isSearchFieldVisible) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Поиск подотделов") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(35.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    isSearchFieldVisible = false
                                    searchQuery = ""
                                }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть поиск")
                                }
                            }
                        )
                    }
                    IconButton(onClick = { showFavorites = !showFavorites }) {
                        Icon(
                            imageVector = if (showFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (showFavorites) "Показать все" else "Показать избранные"
                        )
                    }
                }
            )
        },
        content = { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    modifier = Modifier.padding(start = 10.dp),
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        value = selectedDepartment,
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        label = { Text("Выберите отдел") }
                        // Убрать interactionSource и LaunchedEffect
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        departments.forEach { department ->
                            DropdownMenuItem(
                                text = { Text(text = department) },
                                onClick = {
                                    selectedDepartment = department
                                    vm.updateSelectedDepartment(department)
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        items(filteredDepartmentList.size) { index ->
                            val itemName = filteredDepartmentList[index]
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth().padding(5.dp),
                                shape = RoundedCornerShape(15.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                            ) {
                                val combinedValue = "$city: $selectedDepartment - $itemName"
                                val escapedItemName = escapeDepartmenttId(combinedValue)
                                val badgeVisible = userData?.additionalFields?.get(escapedItemName) as? Boolean ?: false
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        navController.navigate(DepartmentChat(itemName = combinedValue, cityName = city))
                                    },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = itemName, modifier = Modifier.weight(1f).padding(15.dp).wrapContentWidth(Alignment.Start))
                                    if (badgeVisible) {
                                        Badge(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .graphicsLayer(alpha = alpha),
                                            containerColor = Color.Red
                                        )
                                    }
                                    IconButton(onClick = {
                                        if (favoriteItems.contains(itemName)) {
                                            favoriteItems.remove(itemName)
                                        } else {
                                            favoriteItems.add(itemName)
                                        }
                                        favoritesManager.saveFavorites(favoriteItems)
                                    }) {
                                        Icon(
                                            imageVector = if (favoriteItems.contains(itemName)) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Избранное"
                                        )
                                    }
                                }
                            }
                        }
                        if (filteredDepartmentList.isEmpty()) {
                            item {
                                Text(
                                    text = "Подотделы не найдены",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.Gray
                                )
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

                if (userData != null) {
                    BottomNavigationMenu(
                        selectedItem = BottomNavigationItem.DEPARTMENT,
                        navController = navController,
                        newMessageCount = userData.newMessageCity,
                        badgeVisible = userData.newMessageDepts,
                        msgBadgeVisible = msgBadgeVisible,
                        isDarkTheme
                    )
                }
            }
        }
    )
}