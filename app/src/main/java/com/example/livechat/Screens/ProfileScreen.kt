package com.example.livechat.Screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.livechat.CommonImage
import com.example.livechat.CommonProgressBar
import com.example.livechat.DestinationScreen
import com.example.livechat.FullScreenImage
import com.example.livechat.LCViewModel
import com.example.livechat.R
import com.example.livechat.ui.theme.MainBlue

@Composable
fun ProfileScreen(
    navController: NavController,
    vm: LCViewModel,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val inProgress = vm.inProcess.value
    if (inProgress) {
        CommonProgressBar()
    } else {
        val context = LocalContext.current
        val userData = vm.userData.value
        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var city by rememberSaveable { mutableStateOf(userData?.city ?: "") }
        var birthday by rememberSaveable { mutableStateOf(userData?.birthday ?: "") }
        var gender by rememberSaveable { mutableStateOf(userData?.gender ?: "") }

        var showFullScreenImage by remember { mutableStateOf(false) }
        var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

        Column {
            ProfileContent(
                navController = navController,
                context = context,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange,
                vm = vm,
                name = name,
                city = city,
                birthday = birthday,
                gender = gender,
                onSave = {
                    vm.createOrUpdateProfile(
                        city = city,
                        birthday = birthday,
                        gender = gender,
                        context = context
                    )
                    Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
                },
                onLogout = {
                    vm.logout(context)
                    if (userData != null) {
                        userData.userId?.let { vm.resetNewMessageChats(it, false) }
                    }
                    navController.navigate(DestinationScreen.SignUp.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                showFullScreenImage = showFullScreenImage,
                onShowFullScreenImage = { url ->
                    fullScreenImageUrl = url
                    showFullScreenImage = true
                },
                onDismissFullScreenImage = {
                    showFullScreenImage = false
                    fullScreenImageUrl = null
                }
            )

            if (userData != null) {
                BottomNavigationMenu(
                    selectedItem = BottomNavigationItem.PROFILE,
                    navController = navController,
                    newMessageCount = userData.newMessageCity,
                    badgeVisible = userData.newMessageDepts,
                    msgBadgeVisible = userData.chats?.values?.any { it is Boolean && it } ?: false,
                    isDarkTheme
                )
            }
        }

        if (showFullScreenImage && fullScreenImageUrl != null) {
            FullScreenImage(
                imageUrl = fullScreenImageUrl!!,
                onClose = { showFullScreenImage = false }
            )
        }
    }
}


@Composable
fun ProfileContent(
    navController: NavController,
    modifier: Modifier,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    vm: LCViewModel,
    name: String,
    city: String,
    birthday: String,
    gender: String,
    onSave: () -> Unit,
    onLogout: () -> Unit,
    context: Context,
    showFullScreenImage: Boolean,
    onShowFullScreenImage: (String) -> Unit,
    onDismissFullScreenImage: () -> Unit
) {
    val imageUrl = vm.userData?.value?.imageUrl
    val userData = vm.userData.value
    val userId = userData?.userId
    var isNewMessage by remember { mutableStateOf(userData?.newMessageChats) }
    Column(modifier = modifier) {

        ProfileImage(
            imageUrl = imageUrl,
            vm = vm,
            context = context,
            userId = userId,
            onShowFullScreenImage = onShowFullScreenImage
        )

        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            ),
            modifier = Modifier.padding(5.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 5.dp),
                horizontalAlignment = Alignment.Start
            ) {
                val showDialog = remember {
                    mutableStateOf(false)
                }
                val onEditClick: () -> Unit = { showDialog.value = true }
                val onDismiss: () -> Unit = { showDialog.value = false }
                Row (
                    modifier = Modifier.padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Личная информация",
                        color = MainBlue
                    )
                }
                Row(
                    modifier = Modifier.padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Person",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 5.dp),
                        tint = if (isDarkTheme) Color.White else Color.Black
                        )
                    Text(text = "Имя:")
                    Text(text = name, modifier = Modifier.padding(start = 10.dp))
                    IconButton(onClick = { }) {
                        DialogName(
                            showDialog = showDialog.value,
                            onEditClick = onEditClick,
                            onDismiss = onDismiss,
                            vm = vm,
                            city = city,
                            birthday = birthday,
                            gender = gender,
                            context = context
                        )
                    }
                }
                Divider()
                val showDialog2 = remember {
                    mutableStateOf(false)
                }
                val onEditClick2: () -> Unit = { showDialog2.value = true }
                val onDismiss2: () -> Unit = { showDialog2.value = false }
                Row(
                    modifier = Modifier.padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationCity,
                        contentDescription = "City",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 5.dp),
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(text = "Город:")
                    Text(text = city, modifier = Modifier.padding(start = 10.dp))
                    IconButton(onClick = { }) {
                        DialogCity(
                            vm = vm,
                            showDialog = showDialog2.value,
                            onEditClick = onEditClick2,
                            onDismiss = onDismiss2,
                            context = context,
                            name = name,
                            birthday = birthday,
                            gender = gender
                        )
                    }
                }
                Divider()
                Row(
                    modifier = Modifier.padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Date",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 5.dp),
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(text = "Дата рождения:")
                    Text(text = birthday, modifier = Modifier.padding(start = 10.dp))
                    IconButton(onClick = { }) {
                    }
                }
                Divider()
                Row(
                    modifier = Modifier.padding(top = 13.dp, bottom = 13.dp, start = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = "Gender",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 5.dp),
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(text = "Пол:")
                    Text(text = gender, modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            ),
            modifier = Modifier.padding(5.dp),

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 5.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row (
                    modifier = Modifier.padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Настройки",
                        color = MainBlue
                    )
                }
                Row(
                    modifier = Modifier.padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DarkMode,
                        contentDescription = "Dark",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 5.dp),
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(text = "Тёмная тема", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onThemeChange(it) }
                    )
                }
                Divider()
                Row(
                    modifier = Modifier.padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notification",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 5.dp),
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(text = "Уведомления", modifier = Modifier.weight(1f))
                    isNewMessage?.let {
                        Switch(
                            checked = it,
                            onCheckedChange = { checked ->
                                isNewMessage = checked
                                if (userId != null) {
                                    vm.resetNewMessageChats(userId, checked)
                                }
                            }
                        )
                    }
                }
            }
        }
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            ),
            modifier = Modifier.padding(5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 5.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row (
                    modifier = Modifier.padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Мой аккаунт",
                        color = MainBlue
                    )
                }
                Row(
                    modifier = Modifier
                        .clickable { navController.navigate(DestinationScreen.ChangeEmail.route) }
                        .padding(start = 5.dp, end = 15.dp, top = 15.dp, bottom = 15.dp)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Email",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 5.dp),
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(text = "Сменить Email", modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Arrow",
                        tint = if (isDarkTheme) Color.White else Color.Black)
                }
                Divider()
                DeleteAccountButton(
                    isDarkTheme = isDarkTheme,
                    onDeleteAccount = { password ->
                        vm.deleteAccount(
                            context = context,
                            currentPassword = password,
                            onSuccess = {
                                // После успешного удаления выполняем выход
                                onLogout.invoke()
                                navController.navigate(DestinationScreen.SignUp.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onFailure = { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onLogout = onLogout
                )
                Divider()
                    LogoutButton (isDarkTheme, vm){
                        onLogout.invoke()
                    }
                }
            }
        }
    }


@Composable
fun LogoutButton(isDarkTheme: Boolean, vm: LCViewModel, onLogout: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .clickable { showDialog = true }
            .padding(start = 5.dp, end = 15.dp, top = 15.dp, bottom = 15.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.ExitToApp,
            contentDescription = "Exit",
            modifier = Modifier
                .size(30.dp)
                .padding(end = 5.dp),
            tint = if (isDarkTheme) Color.White else Color.Black
        )
        Text(
            text = "Выйти",
            modifier = Modifier
                .weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Arrow",
            tint = if (isDarkTheme) Color.White else Color.Black)
    }


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Вы действительно хотите выйти?") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.setUserOnlineStatus(false)
                        onLogout.invoke()
                        showDialog = false
                    }
                ) {
                    Text("Ок")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun DeleteAccountButton(
    isDarkTheme: Boolean,
    onDeleteAccount: (String) -> Unit, // Принимает пароль
    onLogout: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Row(
        modifier = Modifier
            .clickable { showDialog = true }
            .padding(start = 5.dp, end = 15.dp, top = 15.dp, bottom = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.DeleteForever,
            contentDescription = "Delete account",
            modifier = Modifier
                .size(30.dp)
                .padding(end = 5.dp),
            tint = if (isDarkTheme) Color.White else Color.Black
        )
        Text(
            text = "Удалить аккаунт",
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Arrow",
            tint = if (isDarkTheme) Color.White else Color.Black
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                password = ""
                error = null
            },
            title = { Text(text = "Подтвердите удаление аккаунта") },
            text = {
                Column {
                    Text("Для удаления аккаунта введите ваш текущий пароль:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        label = { Text("Пароль") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = error != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (password.isBlank()) {
                            error = "Введите пароль"
                        } else {
                            onDeleteAccount(password)
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Text("Удалить", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                        password = ""
                        error = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun DialogName(
    vm: LCViewModel,
    showDialog: Boolean,
    onEditClick: () -> Unit,
    onDismiss: () -> Unit,
    context: Context,
    city: String,
    birthday: String,
    gender: String
) {

    val userData = vm.userData.value
    var name by rememberSaveable {
        mutableStateOf(userData?.name ?: "")
    }
    if (showDialog)
        AlertDialog(onDismissRequest = {
            if (name.isEmpty()
            ) {
                Toast.makeText(context, "Пожалуйста, введите новое имя", Toast.LENGTH_SHORT).show()
            } else {
                onDismiss.invoke()
            }
        },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isEmpty()
                    ) {
                        Toast.makeText(context, "Пожалуйста, введите новое имя", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        vm.createOrUpdateProfile(
                            name = name,
                            city = city,
                            birthday = birthday,
                            gender = gender,
                            context = context
                        )
                        onDismiss.invoke()
                    }
                }) {
                    Text(text = "Ок")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                        onDismiss.invoke()
                }) {
                    Text(text = "Отмена")
                }
            },
            title = { Text(text = "Введите имя:") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    )
                )
            }
        )
    IconButton(
        onClick = onEditClick,
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = null
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogCity(
    vm: LCViewModel,
    showDialog: Boolean,
    onEditClick: () -> Unit,
    onDismiss: () -> Unit,
    context: Context,
    name: String,
    birthday: String,
    gender: String
) {
    val citiesName = stringArrayResource(id = R.array.cities)
    var isExpanded by remember {
        mutableStateOf(false)
    }
    val userData = vm.userData.value
    var city by rememberSaveable {
        mutableStateOf(userData?.city ?: "")
    }
    if (showDialog)
        AlertDialog(
            onDismissRequest = {
                if (city.isEmpty()
                ) {
                    Toast.makeText(context, "Пожалуйста, введите новое имя", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    onDismiss.invoke()
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (city.isEmpty()
                    ) {
                        Toast.makeText(context, "Пожалуйста, введите новое имя", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        vm.createOrUpdateProfile(
                            name = name,
                            city = city,
                            birthday = birthday,
                            gender = gender,
                            context = context
                        )

                        onDismiss.invoke()
                    }
                }) {
                    Text(text = "Ок")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (city.isEmpty()
                    ) {
                        Toast.makeText(context, "Поле не должно быть пустым", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        onDismiss.invoke()
                    }
                }) {
                    Text(text = "Отмена")
                }
            },
            title = {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = {
                        isExpanded = !isExpanded
                    }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        value = city,
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
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(text = it) },
                                onClick = {
                                    city = it
                                    isExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                        }
                    }
                }

            },

            )
    IconButton(
        onClick = onEditClick,
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = null
        )
    }
}


@Composable
fun ProfileImage(
    imageUrl: String?,
    userId: String?,
    vm: LCViewModel,
    context: Context,
    onShowFullScreenImage: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            vm.uploadProfileImage(uri, context)
        }
    }

    Box(
        modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .clickable {
                    expanded = true
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(120.dp)
            ) {
                if (imageUrl != null) {
                    CommonImage(data = imageUrl)
                } else {
                    Image(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .size(120.dp)
                            .border(
                                BorderStroke(width = 1.dp, Color.Black),
                                CircleShape
                            )
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (imageUrl != null){
                        DropdownMenuItem(
                            text = { Text(text = "Открыть фото") },
                            trailingIcon = { Icon(imageVector = Icons.Outlined.Photo, contentDescription = null)},
                            onClick = {
                                expanded = false
                                imageUrl?.let { onShowFullScreenImage(it) }
                            })
                        DropdownMenuItem(
                            text = { Text(text = "Изменить фото") },
                            trailingIcon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)},
                            onClick = {
                                expanded = false
                                launcher.launch("image/*")
                            })
                        DropdownMenuItem(
                            text = { Text(text = "Удалить фото", color = Color.Red) },
                            trailingIcon = { Icon(imageVector = Icons.Outlined.DeleteForever, contentDescription = null, tint = Color.Red)},
                            onClick = {
                                expanded = false
                                showDeleteDialog = true
                            })
                    }else{
                        DropdownMenuItem(
                            text = { Text(text = "Выбрать фото") },
                            trailingIcon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)},
                            onClick = {
                                expanded = false
                                launcher.launch("image/*")
                            })
                    }

                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = { Text(text = "Удалить фото") },
            text = { Text(text = "Вы уверены, что хотите удалить эту фотографию?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        imageUrl?.let {
                            if (userId != null) {
                                vm.updateUserImageUrl(userId, context)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Text(text = "Удалить", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text(text = "Отмена")
                }
            }
        )
    }
}
