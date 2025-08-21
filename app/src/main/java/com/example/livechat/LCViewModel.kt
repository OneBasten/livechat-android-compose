package com.example.livechat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livechat.data.CHATS
import com.example.livechat.data.CITYCHATS
import com.example.livechat.data.ChatData
import com.example.livechat.data.ChatUser
import com.example.livechat.data.Event
import com.example.livechat.data.FcmApi
import com.example.livechat.data.MESSAGE
import com.example.livechat.data.Message
import com.example.livechat.data.GroupMessage
import com.example.livechat.data.GroupMessageDepartments
import com.example.livechat.data.NotificationBody
import com.example.livechat.data.SendMessageDto
import com.example.livechat.data.DEPARTMENTS
import com.example.livechat.data.USER_NODE
import com.example.livechat.data.UserData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import retrofit2.Retrofit
import retrofit2.HttpException
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.util.Calendar

@HiltViewModel
class LCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    var db: FirebaseFirestore,
    val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : ViewModel() {
    var inProcess = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val messageForChats = mutableStateOf<List<GroupMessage>>(listOf())
    val messageForDepartmentChats = mutableStateOf<List<GroupMessageDepartments>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration? = null
    var selectedDepartment by mutableStateOf("Администрация")
        private set

    fun updateSelectedDepartment(type: String) {
        selectedDepartment = type
    }

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    private val api: FcmApi = Retrofit.Builder()
        .baseUrl("http://YOUR_API_IP:8080/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create()

    fun sendMessage(messageText: String, deviceToken: String, name: String, chatId: String) {
        viewModelScope.launch {
            val messageDto = SendMessageDto(
                to = deviceToken,
                notification = NotificationBody(
                    title = "$name:",
                    body = messageText
                ),
                data = mapOf("chatId" to chatId)
            )
            try {
                api.sendMessage(messageDto)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setUserOnlineStatus(isOnline: Boolean) {
        val userId = auth.currentUser?.uid ?: return

        val updates = hashMapOf<String, Any>(
            "isOnline" to isOnline
        )

        if (!isOnline) {
            updates["lastSeen"] = System.currentTimeMillis()
        }

        db.collection(USER_NODE).document(userId)
            .update(updates)
            .addOnFailureListener { e ->
                Log.e("OnlineStatus", "Error updating online status", e)
            }
    }

    fun populateMessages(chatId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener = db.collection(CHATS).document(chatId).collection(MESSAGE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chatMessages.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.normtime }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun populateMessagesCity(cityId: String) {
        Log.d("LCViewModel", "populateMessagesCity вызван для cityId: $cityId")
        inProgressChatMessage.value = true
        currentChatMessageListener = db.collection(CITYCHATS).whereEqualTo("cityId", cityId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    messageForChats.value = value.documents.mapNotNull {
                        it.toObject<GroupMessage>()
                    }.sortedBy { it.timestamp }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun populateMessagesDepartment(departmentId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener = db.collection(DEPARTMENTS).whereEqualTo("departmentId", departmentId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    messageForDepartmentChats.value = value.documents.mapNotNull {
                        it.toObject<GroupMessageDepartments>()
                    }.sortedBy { it.timestamp }
                    inProgressChatMessage.value = false
                }
            }
    }


    fun onSendReplyCity(message: String, cityId: String, userId: String, deviceToken: String) {
        val currentTimeMillis = System.currentTimeMillis()
        val msg = GroupMessage(
            deviceToken = deviceToken,
            message = message,
            timestamp = currentTimeMillis.toString(),
            normtime = currentTimeMillis,
            senderId = userData.value?.userId,
            name = userData.value?.name,
            imageUrl = userData.value?.imageUrl,
            city = userData.value?.city,
            birthday = userData.value?.birthday,
            gender = userData.value?.gender,
            cityId = cityId,
            newMessageChats = userData.value?.newMessageChats,
            additionalFields = userData.value?.additionalFields ?: emptyMap()
        )
        db.collection(CITYCHATS).document().set(msg)
            .addOnSuccessListener {
                updateNewMessageCity(cityId, true, userId)
            }
        Log.d("UserData", userData.value.toString())
    }

    private fun updateNewMessageCity(cityId: String, newMessageCity: Boolean, userId: String) {
        db.collection(USER_NODE)
            .whereEqualTo("city", cityId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {

                        if (document.id != userId) {

                            db.collection(USER_NODE).document(document.id)
                                .update("newMessageCity", newMessageCity)
                                .addOnSuccessListener {

                                    Log.d("LCViewModel", "newMessageCity updated for user: ${document.id}")
                                }
                                .addOnFailureListener { exception ->

                                    Log.e("LCViewModel", "Error updating newMessageCity for user: ${document.id}", exception)
                                }
                        }
                    }
                } else {
                    Log.d("LCViewModel", "No users found for cityId: $cityId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LCViewModel", "Error getting users for cityId: $cityId", exception)
            }
    }


    fun resetNewMessageCity(userId: String, newMessageCity: Boolean) {
        db.collection(USER_NODE)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        db.collection(USER_NODE).document(document.id)
                            .update("newMessageCity", newMessageCity)
                            .addOnSuccessListener {
                                Log.d("LCViewModel", "newMessageCity updated for user: ${document.id}")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("LCViewModel", "Error updating newMessageCity for user: ${document.id}", exception)
                            }
                    }
                } else {
                    Log.d("LCViewModel", "No users found for cityId: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LCViewModel", "Error getting users for cityId: $userId", exception)
            }
    }

    fun resetNewMessageChats(userId: String, newMessageChats: Boolean) {
        db.collection(USER_NODE)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        db.collection(USER_NODE).document(document.id)
                            .update("newMessageChats", newMessageChats)
                            .addOnSuccessListener {
                                Log.d("LCViewModel", "newMessageChats updated for user in USER_NODE: ${document.id}")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("LCViewModel", "Error updating newMessageChats for user in USER_NODE: ${document.id}", exception)
                            }
                    }
                } else {
                    Log.d("LCViewModel", "No users found for userId in USER_NODE: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LCViewModel", "Error getting users for userId in USER_NODE: $userId", exception)
            }
        db.collection(CITYCHATS)
            .whereEqualTo("senderId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        db.collection(CITYCHATS).document(document.id)
                            .update("newMessageChats", newMessageChats)
                            .addOnSuccessListener {
                                Log.d("LCViewModel", "newMessageChats updated for user in CITYCHATS: ${document.id}")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("LCViewModel", "Error updating newMessageChats for user in CITYCHATS: ${document.id}", exception)
                            }
                    }
                } else {
                    Log.d("LCViewModel", "No users found for userId in CITYCHATS: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LCViewModel", "Error getting users for userId in CITYCHATS: $userId", exception)
            }
        db.collection(DEPARTMENTS)
            .whereEqualTo("senderId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        db.collection(DEPARTMENTS).document(document.id)
                            .update("newMessageChats", newMessageChats)
                            .addOnSuccessListener {
                                Log.d("LCViewModel", "newMessageChats updated for user in DEPARTMENTCHATS: ${document.id}")
                            }
                            .addOnFailureListener { exception ->
                                Log.e(
                                    "LCViewModel",
                                    "Error updating newMessageChats for user in DEPARTMENTCHATS: ${document.id}", exception)
                            }
                    }
                } else {
                    Log.d("LCViewModel", "No users found for userId in DEPARTMENTCHATS: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LCViewModel", "Error getting users for userId in DEPARTMENTCHATS: $userId", exception)
            }
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userId),
                Filter.equalTo("user2.userId", userId),
            )
        )
            .get()
            .addOnSuccessListener { chatQuerySnapshot ->
                for (document in chatQuerySnapshot.documents) {
                    val chatData = document.toObject(ChatData::class.java)
                    chatData?.let {
                        if (it.user1.userId == userId) {
                            it.user1.newMessageChats = newMessageChats
                        } else if (it.user2.userId == userId) {
                            it.user2.newMessageChats = newMessageChats
                        }
                        document.reference.set(it)
                            .addOnFailureListener { exception ->
                                Log.e("LCViewModel", "Error updating newMessageChats in CHATS for document: ${document.id}", exception)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LCViewModel", "Error retrieving chats for userId: $userId", exception)
            }
    }



    fun onSendReplyDepartment(departmentId: String, cityId: String, message: String, deviceToken: String) {
        val currentTimeMillis = System.currentTimeMillis()
        val msg = GroupMessageDepartments(
            message = message,
            timestamp = currentTimeMillis.toString(),
            normtime = currentTimeMillis,
            senderId = userData.value?.userId,
            name = userData.value?.name,
            imageUrl = userData.value?.imageUrl,
            city = userData.value?.city,
            birthday = userData.value?.birthday,
            gender = userData.value?.gender,
            departmentId = departmentId,
            deviceToken = deviceToken,
            newMessageChats = userData.value?.newMessageChats
        )
        db.collection(DEPARTMENTS).document().set(msg)
        userData.value?.userId?.let { updateFieldForAllUsersExcept(departmentId, it, true, cityId) }

    }


fun updateFieldForAllUsersExcept(escapedItemName: String, excludedUserId: String, newValue: Boolean, cityId: String) {
    val db = FirebaseFirestore.getInstance()
    val usersCollectionRef = db.collection(USER_NODE)

    usersCollectionRef.whereEqualTo("city", cityId).get()
        .addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val userId = document.id
                if (userId != excludedUserId) {
                    if (document.getBoolean(escapedItemName) == false) {
                        val updateField = mapOf(escapedItemName to newValue)

                        usersCollectionRef.document(userId).update(updateField)
                            .addOnSuccessListener {
                                println("Поле '$escapedItemName' успешно обновлено для пользователя $userId")
                            }
                            .addOnFailureListener { e ->
                                println("Ошибка обновления поля '$escapedItemName' для пользователя $userId: $e")
                            }
                        if (document.getBoolean("newMessageDepts") == false) {
                            db.collection(USER_NODE).document(userId)
                                .update("newMessageDepts", newValue)
                                .addOnSuccessListener {
                                    println("Поле 'newMessageDepts' успешно обновлено для пользователя $userId")
                                }
                                .addOnFailureListener { e ->
                                    println("Ошибка обновления поля 'newMessageDepts' для пользователя $userId: $e")
                                }
                        }
                    }
                }
            }
        }
        .addOnFailureListener { e ->
            println("Ошибка получения пользователей: $e")
        }
}

    fun resetFieldForAllUsersExcept(escapedItemName: String, excludedUserId: String, newValue: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val usersCollectionRef = db.collection(USER_NODE)
        usersCollectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val userId = document.id
                    if (userId == excludedUserId) {
                        if (document.getBoolean(escapedItemName) == true) {
                            val updateField = mapOf(escapedItemName to newValue)
                            usersCollectionRef.document(userId).update(updateField)
                            db.collection(USER_NODE).document(document.id)
                                .update("newMessageDepts", newValue)
                                .addOnSuccessListener {
                                    println("Поле '$escapedItemName' успешно обновлено для пользователя $userId")
                                }
                                .addOnFailureListener { e ->
                                    println("Ошибка обновления поля '$escapedItemName' для пользователя $userId: $e")
                                }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                println("Ошибка получения пользователей: $e")
            }
    }


    fun addNewFieldToUser(escapedItemName: String, userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection(USER_NODE).document(userId)
        val newField = mapOf(escapedItemName to false)
        userDocRef.update(newField)
            .addOnSuccessListener {
                println("Поле '$escapedItemName' успешно добавлено для пользователя $userId")
            }
            .addOnFailureListener { e ->
                println("Ошибка добавления поля '$escapedItemName': $e")
            }
    }



    fun checkIfFieldExists(escapedItemName: String, userId: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection(USER_NODE).document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val fieldExists = document.contains(escapedItemName)
                onResult(fieldExists)
            } else {
                onResult(false)
            }
        }.addOnFailureListener { e ->
            println("Ошибка при проверке поля '$escapedItemName': $e")
            onResult(false)
        }
    }

    fun removeFieldFromUser(escapedItemName: String, userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection(USER_NODE).document(userId)

        userDocRef.update(mapOf(escapedItemName to FieldValue.delete()))
            .addOnSuccessListener {
                println("Поле '$escapedItemName' успешно удалено для пользователя $userId")
            }
            .addOnFailureListener { e ->
                println("Ошибка удаления поля '$escapedItemName': $e")
            }
    }


    fun depopulateMessage() {
        chatMessages.value = listOf()
        messageForChats.value = listOf()
        messageForDepartmentChats.value = listOf()
        currentChatMessageListener = null
    }

    fun populateChats() {
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId),
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProcessChats.value = false
            }
        }
    }

    fun onSendReply(
        chatID: String,
        message: String,
        replyToMessageId: String? = null,
        replyToMessageText: String? = null,
        replyToMessageSender: String? = null
    ) {
        val currentTimeMillis = System.currentTimeMillis()
        val msg = Message(
            userData.value?.userId,
            message,
            timestamp = currentTimeMillis.toString(),
            normtime = currentTimeMillis,
            replyToMessageId = replyToMessageId,
            replyToMessageText = replyToMessageText,
            replyToMessageSender = replyToMessageSender
        )
        db.collection(CHATS).document(chatID)
            .collection(MESSAGE).document().set(msg)
            .addOnSuccessListener {
                db.collection(CHATS).document(chatID).update(
                    "lastMessage", message,
                    "timeLastMessage", currentTimeMillis,
                    "idLastMessage", userData.value?.userId
                )
            }
            .addOnFailureListener {
                handleException(it)
            }
    }

    fun userChats(chatID: String, userId: String, bool: Boolean) {
        val userDocRef = db.collection(USER_NODE).document(userId)
        userDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val currentChats = document.get("chats") as? Map<String, Boolean> ?: emptyMap()
                val updatedChats = currentChats + (chatID to bool)

                userDocRef.set(mapOf("chats" to updatedChats), SetOptions.merge())
            }
        }
    }

fun updateChatStatus(chatID: String, userId: String) {
    val usersRef = db.collection(USER_NODE)

    usersRef.get().addOnSuccessListener { querySnapshot ->
        for (document in querySnapshot) {
            val currentUserId = document.id

            if (currentUserId == userId) continue

            val chats = document.get("chats") as? Map<String, Boolean>
            if (chats != null && chats.containsKey(chatID)) {
                val updatedChats = chats.toMutableMap()
                updatedChats[chatID] = true
                document.reference.set(mapOf("chats" to updatedChats), SetOptions.merge())
            }
        }
    }.addOnFailureListener { exception ->
        Log.e("Firestore Error", "Ошибка получения пользователей: ", exception)
    }
}



    fun convertMillisToLocalTime(millis: Long): String? {
        val now = System.currentTimeMillis()
        val diff = now - millis

        return when {
            diff < 60 * 1000 -> "только что"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} мин. назад"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} ч. назад"
            else -> {
                val dateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())
                dateFormat.format(Date(millis))
            }
        }
    }


    fun calculateAge(birthday: String): Int {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val birthDate: Date = sdf.parse(birthday) ?: return 0
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.time = birthDate
        val birthYear = calendar.get(Calendar.YEAR)

        return currentYear - birthYear - if (calendar.get(Calendar.DAY_OF_YEAR) > Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) 1 else 0
    }

    fun signUp(
        name: String,
        email: String,
        password: String,
        city: String,
        birthday: String,
        gender: String,
        context: Context
    ) {
        inProcess.value = true
        if (name.isEmpty() or  email.isEmpty() or password.isEmpty()
            or city.isEmpty() or birthday.isEmpty() or gender.isEmpty()
        ) {
            Toast.makeText(context, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }
        var uid = auth.currentUser?.uid
        inProcess.value = true
        db.collection(USER_NODE).whereEqualTo("userId", uid).get().addOnSuccessListener {
            if (it.isEmpty) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        signIn.value = true
                        createOrUpdateProfile(
                            email = email,
                            name = name,
                            city = city,
                            birthday = birthday,
                            gender = gender,
                            context = context
                        )
                    } else {
                        handleException(it.exception, customMessage = "Ошибка регистрации", context)
                    }
                }
            }
        }
    }

    fun loginIn(email: String, password: String, context: Context) {
        if (email.isEmpty() or password.isEmpty()) {
            Toast.makeText(context, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        } else {
            inProcess.value = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        signIn.value = true
                        inProcess.value = false
                        auth.currentUser?.uid?.let {
                            getUserData(it)
                        }
                    } else {
                        Toast.makeText(context, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun uploadProfileImage(uri: Uri, context: Context) {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.let {
            val fileHash = calculateHash(it)
            checkIfImageExists(fileHash) { existingUrl ->
                if (existingUrl != null) {
                    createOrUpdateProfile(imageUrl = existingUrl, context = context)
                } else {
                    uploadImage(uri, fileHash) { imageUrl ->
                        createOrUpdateProfile(imageUrl = imageUrl.toString(), context = context)
                    }
                }
            }
        }
    }

    private fun calculateHash(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(8192)
        var read: Int
        while (inputStream.read(buffer).also { read = it } > 0) {
            digest.update(buffer, 0, read)
        }
        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun checkIfImageExists(fileHash: String, onResult: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/$fileHash")
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            onResult(uri.toString())
        }.addOnFailureListener {
            onResult(null)
        }
    }

    private fun uploadImage(uri: Uri, fileHash: String, onSuccess: (Uri) -> Unit) {
        inProcess.value = true
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/$fileHash")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
            inProcess.value = false
        }.addOnFailureListener {
            handleException(it)
        }
    }


    fun updateUserImageUrl(userId: String, context: Context) {
        db.collection(USER_NODE).document(userId)
            .update("imageUrl", null)
            .addOnSuccessListener {
                Toast.makeText(context, "Фото удалено", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Не удалось обновить данные пользователя", context)
            }
    }

    fun onSendImage(chatID: String, imageUri: Uri, message: String? = null, replyToMessageId: String? = null, replyToMessageText: String? = null, replyToMessageSender: String? = null) {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val fileHash = calculateHash(inputStream!!)

        checkIfImageExists(fileHash) { existingUrl ->
            if (existingUrl != null) {
                sendMessageWithImage(chatID, existingUrl, message, replyToMessageId, replyToMessageText, replyToMessageSender)
            } else {
                uploadImage(imageUri, fileHash) { downloadUrl ->
                    sendMessageWithImage(chatID, downloadUrl.toString(), message, replyToMessageId, replyToMessageText, replyToMessageSender)
                }
            }
        }
    }

    private fun sendMessageWithImage(
        chatID: String,
        imageUrl: String,
        message: String? = null,
        replyToMessageId: String? = null,
        replyToMessageText: String? = null,
        replyToMessageSender: String? = null
    ) {
        val currentTimeMillis = System.currentTimeMillis()
        val msg = Message(
            userData.value?.userId,
            message,
            timestamp = currentTimeMillis.toString(),
            normtime = currentTimeMillis,
            replyToMessageId = replyToMessageId,
            replyToMessageText = replyToMessageText,
            replyToMessageSender = replyToMessageSender,
            imageUrl = imageUrl
        )

        val lastMessageText = message ?: "Изображение"

        db.collection(CHATS).document(chatID)
            .collection(MESSAGE).document().set(msg)
            .addOnSuccessListener {
                db.collection(CHATS).document(chatID).update(
                    "lastMessage", lastMessageText,
                    "timeLastMessage", currentTimeMillis,
                    "idLastMessage", userData.value?.userId
                )
            }
            .addOnFailureListener {
                handleException(it)
            }
    }

    fun createOrUpdateProfile(
        name: String? = null,
        imageUrl: String? = null,
        city: String? = null,
        birthday: String? = null,
        gender: String? = null,
        email: String? = null,
        context: Context
    ) {

        var uid = auth.currentUser?.uid

        var userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            city = city ?: userData.value?.city,
            birthday = birthday ?: userData.value?.birthday,
            gender = gender ?: userData.value?.gender,
            email = email ?: userData.value?.email
        )

        uid?.let {
            inProcess.value = true

            db.collection(USER_NODE).document(uid).get().addOnSuccessListener {

                if (it.exists()) {

                    val existingUserData = it.toObject(UserData::class.java)

                    existingUserData?.let {
                        val updatedData = UserData(
                            userId = uid,
                            name = name ?: existingUserData.name,
                            imageUrl = imageUrl ?: existingUserData.imageUrl,
                            city = city ?: existingUserData.city,
                            birthday = birthday ?: existingUserData.birthday,
                            gender = gender ?: existingUserData.gender
                        )


                        db.collection(USER_NODE).document(uid).update(updatedData.toMap())
                            .addOnSuccessListener {
                                inProcess.value = false
                                getUserData(uid)
                            }
                            .addOnFailureListener { exception ->
                                handleException(exception, "Cannot Update User Data")
                            }
                        updateDataInChatNodes(
                            updatedData.userId!!,
                            updatedData.imageUrl,
                            updatedData.name,
                            updatedData.city,
                            updatedData.birthday,
                            updatedData.gender
                        )
                        updatedData.city?.let { it1 ->
                            updateDataInDepartmentChatNodes(
                                updatedData.userId!!,
                                updatedData.name,
                                updatedData.imageUrl,
                                it1,
                                updatedData.birthday,
                                updatedData.gender
                            )
                        }
                        updatedData.city?.let { it1 ->
                            updateDataInCityChatNodes(
                                updatedData.userId!!,
                                updatedData.name,
                                updatedData.imageUrl,
                                it1,
                                updatedData.birthday,
                                updatedData.gender
                            )
                        }
                    }
                } else {
                    db.collection(USER_NODE).document(uid).set(userData)

                    inProcess.value = false

                    getUserData(uid)
                }
            }
                .addOnFailureListener {
                    handleException(it, "Невозможно получить данные пользователя", context = context)
                }
        }
    }

    private fun updateDataInChatNodes(
        userId: String,
        imageUrl: String?,
        name: String?,
        city: String?,
        birthday: String?,
        gender: String?
    ) {
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userId),
                Filter.equalTo("user2.userId", userId),
            )
        )
            .get()
            .addOnSuccessListener { chatQuerySnapshot ->
                for (document in chatQuerySnapshot.documents) {
                    val chatData = document.toObject(ChatData::class.java)
                    chatData?.let {
                        chatData.user1?.let { user1 ->
                            if (user1.userId == userId) {
                                user1.imageUrl = imageUrl
                                user1.name = name
                                user1.city = city
                                user1.birthday = birthday
                                user1.gender = gender
                            }
                        }
                        chatData.user2?.let { user2 ->
                            if (user2.userId == userId) {
                                user2.imageUrl = imageUrl
                                user2.name = name
                                user2.city = city
                                user2.birthday = birthday
                                user2.gender = gender
                            }
                        }
                        document.reference.set(chatData)
                            .addOnFailureListener { exception ->
                                handleException(exception, "Cannot Update Chat Data")
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Cannot Retrieve Chats")
            }
    }

    private fun updateDataInCityChatNodes(
        userId: String,
        name: String?,
        imageUrl: String?,
        city: String,
        birthday: String?,
        gender: String?
    ) {
        db.collection(CITYCHATS).whereEqualTo(
            "senderId", userId
        )
            .get()
            .addOnSuccessListener { chatQuerySnapshot ->
                for (document in chatQuerySnapshot.documents) {
                    val groupMessageData = document.toObject(GroupMessage::class.java)
                    groupMessageData?.let {
                        if (groupMessageData.senderId == userId) {
                            groupMessageData.name = name
                            groupMessageData.imageUrl = imageUrl
                            groupMessageData.city = city
                            groupMessageData.birthday = birthday
                            groupMessageData.gender = gender
                        }
                        document.reference.set(groupMessageData)
                    }
                }
            }
    }

    private fun updateDataInDepartmentChatNodes(
        userId: String,
        name: String?,
        imageUrl: String?,
        city: String,
        birthday: String?,
        gender: String?
    ) {
        db.collection(DEPARTMENTS).whereEqualTo("senderId", userId)
            .get()
            .addOnSuccessListener { chatQuerySnapshot ->
                for (document in chatQuerySnapshot.documents) {
                    val groupMessageData = document.toObject(GroupMessageDepartments::class.java)
                    groupMessageData?.let {
                        if (groupMessageData.senderId == userId) {
                            groupMessageData.name = name
                            groupMessageData.imageUrl = imageUrl
                            groupMessageData.city = city
                            groupMessageData.birthday = birthday
                            groupMessageData.gender = gender
                        }
                        document.reference.set(groupMessageData)
                    }
                }
            }
    }



    private fun getUserData(uid: String) {
        inProcess.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "Cannot Retrieve User")
                inProcess.value = false
                return@addSnapshotListener
            }
            if (value != null) {
                val userDataMap = value.data ?: emptyMap()

                val chatsMap = userDataMap["chats"] as? Map<String, Any> ?: emptyMap()

                val user = UserData(
                    userId = userDataMap["userId"] as? String,
                    name = userDataMap["name"] as? String,
                    imageUrl = userDataMap["imageUrl"] as? String,
                    city = userDataMap["city"] as? String,
                    birthday = userDataMap["birthday"] as? String,
                    gender = userDataMap["gender"] as? String,
                    email = userDataMap["email"] as? String,
                    newMessageCity = userDataMap["newMessageCity"] as? Boolean ?: false,
                    newMessageDepts = userDataMap["newMessageDepts"] as? Boolean ?: false,
                    newMessageChats = userDataMap["newMessageChats"] as? Boolean ?: true,
                    additionalFields = userDataMap.filterKeys { it !in listOf("userId", "name",
                        "number", "imageUrl", "city", "birthday", "gender", "newMessageCity", "chats", "email") },
                    chats = chatsMap
                )

                userData.value = user
                inProcess.value = false
                populateChats()
            }
        }
    }



    fun handleException(
        exception: Exception? = null,
        customMessage: String = "",
        context: Context? = null
    ) {
        Log.e("@@vm", "handleException: ", exception)
        Log.d("@@vm", "handleException: $customMessage")
        if (context != null) {
            Toast.makeText(context, customMessage, Toast.LENGTH_SHORT).show()
        }
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNullOrEmpty()) errorMsg else customMessage

        eventMutableState.value = Event(message)
        inProcess.value = false
    }

    fun logout(context: Context) {
        auth.signOut()
        signIn.value = false
        userData.value = null
        depopulateMessage()
        currentChatMessageListener = null
        eventMutableState.value = Event("Logged Out")
        Toast.makeText(context, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
        val intent = (context as Activity).intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        (context as Activity).finish()
    }


    fun onAddChats(
        senderId: String,
        reply: String,
        userDeviceToken: String,
        chatPartnerDeviceToken: String,
        onSuccess: (String) -> Unit
    ){
        val currentTimeMillis = System.currentTimeMillis()
        db.collection(CHATS).where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.userId", senderId),
                    Filter.equalTo("user2.userId", userData.value?.userId)
                ),
                Filter.and(
                    Filter.equalTo("user1.userId", userData.value?.userId),
                    Filter.equalTo("user2.userId", senderId)
                )
            )
        ).get().addOnSuccessListener {
            if (it.isEmpty) {
                db.collection(USER_NODE).whereEqualTo("userId", senderId).get()
                    .addOnSuccessListener {
                        if (it.isEmpty) {
                            handleException(customMessage = "Пользователь не найден")
                        } else {
                            val chatPartner = it.toObjects<UserData>()[0]
                            val id = db.collection(CHATS).document().id
                            val chat = ChatData(
                                chatId = id,
                                lastMessage = reply,
                                timeLastMessage = currentTimeMillis,
                                idLastMessage = userData.value?.userId,
                                ChatUser(
                                    userData.value?.userId,
                                    userData.value?.name,
                                    userData.value?.imageUrl,
                                    userData.value?.city,
                                    userData.value?.birthday,
                                    userData.value?.gender,
                                    userDeviceToken,
                                    newMessageChats = userData.value?.newMessageChats,
                                    banUser = false
                                ),
                                ChatUser(
                                    chatPartner.userId,
                                    chatPartner.name,
                                    chatPartner.imageUrl,
                                    chatPartner.city,
                                    chatPartner.birthday,
                                    chatPartner.gender,
                                    deviceToken = chatPartnerDeviceToken,
                                    newMessageChats = chatPartner.newMessageChats,
                                    banUser = false
                                )
                            )
                            db.collection(CHATS).document(id).set(chat).addOnSuccessListener {
                                onSuccess(id) // Возвращаем chatId через callback
                                onSendReply(id, reply)
                                userData.value?.userId?.let { it1 -> userChats(id, it1, false) }
                                chatPartner.userId?.let { it1 -> userChats(id, it1, true) }
                            }
                        }
                    }.addOnFailureListener {
                        handleException(it)
                    }
            } else {
                handleException(customMessage = "Чат уже добавлен")
            }
        }

    }

    fun banUserInChat(chatId: String, userId: String, banValue: Boolean) {
        val chatRef = db.collection(CHATS).document(chatId)
        chatRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val chatData = documentSnapshot.toObject(ChatData::class.java)
                if (chatData != null) {
                    if (chatData.user1.userId == userId) {
                        chatRef.update("user1.banUser", banValue)
                            .addOnSuccessListener {
                                println("Пользователь $userId забанен в чате $chatId")
                            }
                            .addOnFailureListener { e ->
                                handleException(e)
                            }
                    } else if (chatData.user2.userId == userId) {
                        chatRef.update("user2.banUser", banValue)
                            .addOnSuccessListener {
                                println("Пользователь $userId забанен в чате $chatId")
                            }
                            .addOnFailureListener { e ->
                                handleException(e)
                            }
                    } else {
                        handleException(customMessage = "Пользователь $userId не найден в чате $chatId")
                    }
                } else {
                    handleException(customMessage = "Данные чата не найдены")
                }
            } else {
                handleException(customMessage = "Чат $chatId не найден")
            }
        }.addOnFailureListener { e ->
            handleException(e)
        }
    }

    fun updateUserEmail(
        newEmail: String,
        password: String,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        inProcess.value = true
        val user = auth.currentUser ?: run {
            inProcess.value = false
            onFailure("Пользователь не авторизован")
            return
        }

        // 1. Сначала выполняем reauthentication
        val credential = EmailAuthProvider.getCredential(user.email ?: "", password)

        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (!reauthTask.isSuccessful) {
                inProcess.value = false
                onFailure("Неверный пароль")
                return@addOnCompleteListener
            }

            // 2. После успешной reauthentication отправляем письмо подтверждения
            user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { verifyTask ->
                inProcess.value = false

                if (verifyTask.isSuccessful) {
                    // 3. Обновляем email в Firestore
                    db.collection(USER_NODE).document(user.uid)
                        .update("email", newEmail)
                        .addOnSuccessListener {
                            onSuccess()
                            Toast.makeText(
                                context,
                                "Письмо с подтверждением отправлено на $newEmail",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("EmailUpdate", "Firestore update error", e)
                            onFailure("Email изменен, но не обновлен в профиле")
                        }
                } else {
                    val error = verifyTask.exception?.message ?: "Неизвестная ошибка"
                    Log.e("EmailUpdate", "Verify email error", verifyTask.exception)

                    when {
                        error.contains("email-already-in-use") ->
                            onFailure("Этот email уже используется")
                        error.contains("invalid-email") ->
                            onFailure("Некорректный email адрес")
                        else ->
                            onFailure("Ошибка отправки письма подтверждения")
                    }
                }
            }
        }.addOnFailureListener {
            inProcess.value = false
            onFailure("Ошибка аутентификации: ${it.message}")
        }
    }

    fun deleteAccount(
        context: Context,
        currentPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        inProcess.value = true
        val user = auth.currentUser
        val uid = user?.uid ?: run {
            inProcess.value = false
            onFailure("Пользователь не авторизован")
            return
        }

        // 1. Повторная аутентификация
        val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)

        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (!reauthTask.isSuccessful) {
                inProcess.value = false
                onFailure("Неверный пароль")
                return@addOnCompleteListener
            }

            // 2. Удаление данных пользователя
            db.collection(USER_NODE).document(uid).delete()
                .addOnSuccessListener {
                    // 3. Обновление ссылок в других коллекциях
                    updateUserReferencesEverywhere(uid, context, {
                        // 4. Удаление аккаунта
                        user.delete().addOnCompleteListener { deleteTask ->
                            inProcess.value = false
                            if (deleteTask.isSuccessful) {
                                onSuccess()
                            } else {
                                onFailure("Ошибка при удалении аккаунта: ${deleteTask.exception?.message}")
                            }
                        }
                    }, { exception ->
                        inProcess.value = false
                        onFailure("Ошибка при обновлении данных: ${exception.message}")
                    })
                }
                .addOnFailureListener { exception ->
                    inProcess.value = false
                    onFailure("Ошибка при удалении профиля: ${exception.message}")
                }
        }
    }

    private fun updateUserReferencesEverywhere(
        userId: String,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val batch = db.batch()

        // Обновляем данные в чатах (CHATS)
        db.collection(CHATS)
            .where(
                Filter.or(
                    Filter.equalTo("user1.userId", userId),
                    Filter.equalTo("user2.userId", userId)
                )
            )
            .get()
            .addOnSuccessListener { chatsSnapshot ->
                chatsSnapshot.documents.forEach { doc ->
                    val chatData = doc.toObject(ChatData::class.java)
                    chatData?.let {
                        if (it.user1?.userId == userId) {
                            batch.update(
                                doc.reference,
                                "user1",
                                ChatUser(
                                    userId = null,
                                    name = "Удаленный пользователь",
                                    imageUrl = null,
                                    city = null,
                                    birthday = null,
                                    gender = null,
                                    deviceToken = null,
                                    newMessageChats = null
                                )
                            )
                        } else if (it.user2?.userId == userId) {
                            batch.update(
                                doc.reference,
                                "user2",
                                ChatUser(
                                    userId = null,
                                    name = "Удаленный пользователь",
                                    imageUrl = null,
                                    city = null,
                                    birthday = null,
                                    gender = null,
                                    deviceToken = null,
                                    newMessageChats = null
                                )
                            )
                        }
                    }
                }

                // Обновляем данные в CITYCHATS
                db.collection(CITYCHATS)
                    .whereEqualTo("senderId", userId)
                    .get()
                    .addOnSuccessListener { cityChatsSnapshot ->
                        cityChatsSnapshot.documents.forEach { doc ->
                            batch.update(
                                doc.reference,
                                mapOf(
                                    "name" to "Удаленный пользователь",
                                    "imageUrl" to null,
                                    "city" to null,
                                    "birthday" to null,
                                    "gender" to null
                                )
                            )
                        }

                        // Обновляем данные в DEPARTMENTS
                        db.collection(DEPARTMENTS)
                            .whereEqualTo("senderId", userId)
                            .get()
                            .addOnSuccessListener { deptSnapshot ->
                                deptSnapshot.documents.forEach { doc ->
                                    batch.update(
                                        doc.reference,
                                        mapOf(
                                            "name" to "Удаленный пользователь",
                                            "imageUrl" to null,
                                            "city" to null,
                                            "birthday" to null,
                                            "gender" to null
                                        )
                                    )
                                }

                                // Выполняем все обновления
                                batch.commit()
                                    .addOnSuccessListener {
                                        onSuccess()
                                    }
                                    .addOnFailureListener { exception ->
                                        inProcess.value = false
                                        handleException(exception, "Ошибка при обновлении данных", context)
                                        onFailure(exception)
                                    }
                            }
                            .addOnFailureListener { exception ->
                                inProcess.value = false
                                handleException(exception, "Ошибка при получении данных департаментов", context)
                                onFailure(exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        inProcess.value = false
                        handleException(exception, "Ошибка при получении городских чатов", context)
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                inProcess.value = false
                handleException(exception, "Ошибка при получении чатов пользователя", context)
                onFailure(exception)
            }
    }

}

