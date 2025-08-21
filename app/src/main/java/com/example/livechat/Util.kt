package com.example.livechat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.livechat.ui.theme.DarkGreyText
import com.example.livechat.ui.theme.GreyCard
import com.example.livechat.ui.theme.GreyMessage
import com.example.livechat.ui.theme.GreyText

fun navigateTo (navController: NavController, route: String) {
    navController.navigate(route){
        popUpTo(route)
        launchSingleTop = true
    }
}

@Composable
fun CommonProgressBar(){
    Row (
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) {}
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        CircularProgressIndicator()
    }
}


@Composable
fun CommonDivider(){
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun CommonImage(
    data: String?,
    modifier: Modifier = Modifier
        .clip(CircleShape)
        .background(Color.Gray)
        .size(120.dp)
        .border(
            width = 1.dp,
            color = Color.Black,
            shape = CircleShape
        ),
    contentScale: ContentScale = ContentScale.Crop
){
    val painter = rememberAsyncImagePainter(model = data)
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
        )
}

@Composable
fun FullScreenImage(
    imageUrl: String,
    onClose: () -> Unit,
) {
    val maxScale = 3f
    val minScale = 1f

    var scale by remember { mutableStateOf(minScale) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        val painter = rememberAsyncImagePainter(model = imageUrl)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { layoutCoordinates ->
                    containerSize = layoutCoordinates.size
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoomChange, _ ->

                        scale = (scale * zoomChange).coerceIn(minScale, maxScale)

                        offsetX += pan.x
                        offsetY += pan.y

                        offsetX = offsetX.coerceIn(
                            (containerSize.width / 2 * (1 - scale))..(containerSize.width / 2 * (scale - 1))
                        )
                        offsetY = offsetY.coerceIn(
                            (containerSize.height / 2 * (1 - scale))..(containerSize.height / 2 * (scale - 1))
                        )
                    }
                }
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .align(Alignment.Center)
                    .onGloballyPositioned { layoutCoordinates ->
                        imageSize = layoutCoordinates.size
                    }
            )
        }

        Button(
            onClick = {
                onClose()
                scale = minScale
                offsetX = 0f
                offsetY = 0f
            },
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Text("Закрыть", color = Color.White)
        }
    }

    LaunchedEffect(Unit) {
        scale = minScale
        offsetX = 0f
        offsetY = 0f
    }
}

@Composable
fun CommonImageGroupChat(
    data: String?,
    modifier: Modifier = Modifier
        .padding(3.dp)
        .size(40.dp)
        .clip(CircleShape),
    contentScale: ContentScale = ContentScale.Crop
){
    val painter = rememberAsyncImagePainter(model = data)
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )
}



@Composable
fun CheckSignedIn(vm: LCViewModel, navController: NavController){
    val alreadySignIn = remember { mutableStateOf(false) }
    val signIn = vm.signIn.value
    if (signIn && !alreadySignIn.value){
        alreadySignIn.value = true
        navController.navigate(DestinationScreen.Profile.route){
            popUpTo(0)
        }
    }
}

@Composable
fun TitleText(txt: String) {
    Text(
        text = txt,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        modifier = Modifier
            .padding(8.dp)
    )
}

@Composable
fun CommonRow(
    boldText: Boolean,
    isDarkTheme: Boolean,
    imageUrl: String?,
    name: String?,
    message: String?,
    currentTime: String?,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .clickable { onItemClick.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageUrl != null) {
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

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        ) {
            Text(
                text = name ?: "---",
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 10.dp)
                    .background(
                        if (boldText) {
                            if (isDarkTheme) DarkGreyText else GreyText
                        } else {
                            Color.Transparent
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                        text = "$message",
                    fontWeight = if (boldText) FontWeight.Bold else FontWeight.Normal,
                    color = if (boldText && isDarkTheme) Color.Black else Color.Unspecified,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = " · $currentTime",
                    fontWeight = if (boldText) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (boldText && isDarkTheme) Color.Black else Color.Unspecified,
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCity(cityState: MutableState<String>){
    val citiesName = stringArrayResource(id = R.array.cities)
    var isExpanded by remember {
        mutableStateOf(false)
    }
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            isExpanded = !isExpanded
        }) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            value = cityState.value,
            onValueChange = {
            },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            label = { Text(text = "Выберите город:")}
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false })
        {
            citiesName.forEach {
                DropdownMenuItem(
                    text = { Text(text = it) },
                    onClick = {
                        cityState.value = it
                        isExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectGender(genderState: MutableState<String>){
    val citiesName = stringArrayResource(id = R.array.gender)
    var isExpanded by remember {
        mutableStateOf(false)
    }
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            isExpanded = !isExpanded
        }) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            value = genderState.value,
            onValueChange = {
            },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            label = { Text(text = "Пол:")}
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false })
        {
            citiesName.forEach {
                DropdownMenuItem(
                    text = { Text(text = it) },
                    onClick = {
                        genderState.value = it
                        isExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}