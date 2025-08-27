# LiveChat - Android Client

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.21-blue?logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.4-brightgreen)
![Min SDK](https://img.shields.io/badge/min%20SDK-24-orange)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

</div>

Корпоративный мессенджер для Android, разработанный на современном стеке технологий: **Kotlin** и **Jetpack Compose**. Приложение предназначено для внутреннего общения сотрудников распределенной компании с филиалами в разных городах.

<p align="center">
  <img src="https://img.shields.io/badge/Architecture-MVVI / Clean-yellow" alt="Architecture">
  <img src="https://img.shields.io/badge/DI-Hilt-important" alt="Dependency Injection">
  <img src="https://img.shields.io/badge/Async-Coroutines / Flow-9cf" alt="Coroutines">
</p>

## 🚀 Возможности

*   **📱 Личные чаты:** Общение один-на-один с другими сотрудниками.
*   **🏙️ Городские чаты:** Публичные каналы для общения всех сотрудников в вашем городе.
*   **🏢 Чаты отделов:** Специализированные каналы для отделов (Администрация, IT, Финансы, HR, Маркетинг).
*   **🔔 Push-уведомления:** Получайте уведомления о новых сообщениях, даже когда приложение свернуто.
*   **👤 Профиль пользователя:** Настройте свою учетную запись, смените фото, город и другие данные.
*   **🎨 Темная тема:** Поддержка светлой и темной темы оформления.
*   **⚡ Современный UI:** Полностью реализован на Jetpack Compose с анимациями и адаптивным дизайном.

## 🛠️ Технологический стек

*   **Язык:** [Kotlin](https://kotlinlang.org/)
*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Навигация:** [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
*   **Архитектура:** MVVI / Clean Architecture (ViewModel + State Flow)
*   **DI (Внедрение зависимостей):** [Dagger Hilt](https://dagger.dev/hilt/)
*   **Асинхронность:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html) + [Flow](https://kotlinlang.org/docs/flow.html)
*   **Backend:** [Firebase](https://firebase.google.com/)
    *   **Аутентификация:** Firebase Auth
    *   **База данных:** Cloud Firestore
    *   **Хранилище:** Firebase Storage
    *   **Push-уведомления:** Firebase Cloud Messaging (FCM)
*   **Сетевые запросы:** [Retrofit](https://square.github.io/retrofit/) + [Moshi](https://github.com/square/moshi)
*   **Загрузка изображений:** [Coil](https://coil-kt.github.io/coil/)

## 📦 Установка и запуск

1.  **Клонируйте репозиторий:**
    ```bash
    git clone https://github.com/OneBasten/livechat-android-compose.git
    ```
2.  **Откройте проект в Android Studio** (желательно последней версии).
3.  **Добавьте файл конфигурации Firebase:**
    *   Зарегистрируйте приложение в [Консоли Firebase](https://console.firebase.google.com/).
    *   Скачайте файл `google-services.json` и поместите его в папку `app/` вашего проекта.
4.  **Соберите и запустите** приложение на эмуляторе или физическом устройстве.

## 🔧 Настройка сервера для уведомлений

Для работы push-уведомлений необходим собственный сервер. Инструкции по его настройке и запуску смотрите в репозитории [livechat-server](https://github.com/OneBasten/livechat-server).

## Скриншоты

|      requestNotificationPermission   |            SignUp Screen             |                Login Screen               |                Calendar                   |
| :----------------------------------: | :----------------------------------: | :---------------------------------------: | :---------------------------------------: |
| <img src="screenshots/notifications.png" width="230">   | <img src="screenshots/signup.png" width="300">          |        <img src="screenshots/login.png" width="300">         |        <img src="screenshots/calendar.png" width="300">      |

|            Profile Screen           |                Change Email                |                Delete account            |
| :----------------------------------:| :---------------------------------------:  | :---------------------------------------:|
| ![](screenshots/profilescreen.png)  |        ![](screenshots/changeemail.png)    |      ![](screenshots/deleteaccount.png)  | 

|            Chat Screen              |                Single Chat Screen         |                User Blocking             |
| :----------------------------------:| :---------------------------------------: | :---------------------------------------:|
| ![](screenshots/chatscreen.png)     |        ![](screenshots/singlechat.png)    |      ![](screenshots/profileblock.png)   | 

|            City Screen              |                Profile Info               |           
| :----------------------------------:| :---------------------------------------: |
| ![](screenshots/cityscreen.png)     |   ![](screenshots/profileinfochat.png)    |   

|            Department Screen          |                Department Chat           |           
| :----------------------------------:  | :---------------------------------------:|
| ![](screenshots/departmentscreen.png) |   ![](screenshots/departmentchat.png)    | 

## Скриншоты (темная тема)

|            SignUp Screen             |                Login Screen               |
| :----------------------------------: | :---------------------------------------: |
| ![](screenshots/signupblack.png)     |        ![](screenshots/loginblack.png)    |

|            Profile Screen           |                Change Email                |                Delete account            |
| :----------------------------------:| :---------------------------------------:  | :---------------------------------------:|
| ![](screenshots/profilescreenblack.png)  |        ![](screenshots/changeemailblack.png)    |      ![](screenshots/deleteaccountblack.png)  | 

|            Chat Screen              |                Single Chat Screen         |                User Blocking             |
| :----------------------------------:| :---------------------------------------: | :---------------------------------------:|
| ![](screenshots/chatscreenblack.png)     |        ![](screenshots/singlechatblack.png)    |      ![](screenshots/profileblockblack.png)   | 

|            City Screen               |                Profile Info               |           
| :----------------------------------: | :---------------------------------------: |
| ![](screenshots/cityscreenblack.png) |   ![](screenshots/profileinfochatblack.png)    |   

|            Department Screen               |                Department Chat            |           
| :----------------------------------:       | :---------------------------------------: |
| ![](screenshots/departmentscreenblack.png) |   ![](screenshots/departmentchatblack.png)| 



