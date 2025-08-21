package com.example.livechat.data
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.livechat.MainActivity
import com.example.livechat.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class PushNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        try {
            val notification = message.notification
            val data = message.data

            if (notification != null) {
                val chatId = data["chatId"]

                // Создаем интент для MainActivity
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("chatId", chatId)
                }

                // Создаем pending intent
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Создаем канал уведомлений (для Android 8.0+)
                val channelId = "chat_channel"
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Chat Messages",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Chat notifications"
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                // Строим уведомление
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.chat)
                    .setContentTitle(notification.title)
                    .setContentText(notification.body)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                // Показываем уведомление
                notificationManager.notify(Random.nextInt(), notificationBuilder.build())
            }
        } catch (e: Exception) {
            Log.e("PushNotification", "Error processing notification", e)
        }
    }
}