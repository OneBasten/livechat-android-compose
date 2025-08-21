package com.example.livechat.Screens

import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.example.livechat.CheckSignedIn
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.R
import com.example.livechat.SelectCity
import com.example.livechat.SelectGender
import com.example.livechat.navigateTo
import com.example.livechat.ui.theme.MainBlue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController, vm: LCViewModel) {

    val context = LocalContext.current

    CheckSignedIn(vm = vm, navController = navController)
    val localFocusManager = LocalFocusManager.current

    var selectedDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .focusable()
                .wrapContentHeight()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        localFocusManager.clearFocus()
                    })
                }
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nameState = remember {
                mutableStateOf(TextFieldValue())
            }
            val emailState = remember {
                mutableStateOf(TextFieldValue())
            }
            val cityState = remember {
                mutableStateOf("")
            }
            val genderState = remember {
                mutableStateOf("")
            }
            var passwordState by rememberSaveable {
                mutableStateOf("")
            }
            var passwordVisible by rememberSaveable { mutableStateOf(false) }
            var passwordError by remember { mutableStateOf<String?>(null) }


            Image(
                painter = painterResource(id = R.drawable.handshake),
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .padding(top = 16.dp)
                    .padding(8.dp)
            )
            Text(
                text = "Регистрация",
                fontSize = 30.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
            OutlinedTextField(value = nameState.value, onValueChange = {
                nameState.value = it
            },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                label = { Text(text = "Имя") },
                singleLine = true,
                modifier = Modifier.padding(8.dp)
            )
            SelectCity(cityState = cityState)

            OutlinedTextField(
                value = selectedDate,
                onValueChange = { },
                label = { Text("Дата рождения") },
                modifier = Modifier.padding(8.dp),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Выбрать дату")
                    }
                }
            )
            if (showDatePicker) {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val maxDateCalendar = Calendar.getInstance()
                maxDateCalendar.add(Calendar.YEAR, -18)

                val datePickerDialog = android.app.DatePickerDialog(
                    context,
                    { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                        selectedDate = String.format(
                            "%02d.%02d.%04d",
                            selectedDay,
                            selectedMonth + 1,
                            selectedYear
                        )
                        showDatePicker = false
                    },
                    year,
                    month,
                    day
                )

                datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis
                datePickerDialog.setOnDismissListener {
                    showDatePicker = false
                }
                datePickerDialog.show()
            }
            SelectGender(genderState = genderState)
            OutlinedTextField(value = emailState.value, onValueChange = {
                emailState.value = it
            },
                label = { Text(text = "Электронная почта") },
                singleLine = true,
                modifier = Modifier.padding(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            OutlinedTextField(
                value = passwordState,
                onValueChange = {
                    passwordState = it
                    passwordError = validatePassword(it)
                },
                label = { Text(text = "Пароль") },
                singleLine = true,
                modifier = Modifier.padding(8.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                },
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) {
                        Text(text = passwordError!!, color = Color.Red)
                    }
                }
            )
            Button(
                onClick = {
                    vm.signUp(
                        name = nameState.value.text,
                        email = emailState.value.text,
                        password = passwordState,
                        city = cityState.value,
                        birthday = selectedDate,
                        gender = genderState.value,
                        context = context
                    )
                },
                modifier = Modifier.padding(8.dp),
                enabled = passwordError == null && passwordState.isNotEmpty()
            ) {
                Text(text = "Регистрация")
            }
            Text(
                text = "Уже есть аккаунт? Войти - >",
                color = MainBlue,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { navigateTo(navController, DestinationScreen.Login.route) }
            )
        }
    }
}

fun validatePassword(password: String): String? {
    if (password.length < 8) {
        return "Пароль должен содержать минимум 8 символов"
    }
    if (!password.any { it.isDigit() }) {
        return "Пароль должен содержать хотя бы одну цифру"
    }
    if (!password.any { it.isLowerCase() }) {
        return "Пароль должен содержать хотя бы одну строчную букву"
    }
    if (!password.any { it.isUpperCase() }) {
        return "Пароль должен содержать хотя бы одну заглавную букву"
    }
    return null
}


