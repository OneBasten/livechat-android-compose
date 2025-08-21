package com.example.livechat.data


data class UserData(
    var userId: String? = "",
    var name: String? = "",
    var imageUrl: String? = "",
    var city: String? = "",
    var birthday: String? = "",
    var gender: String? = "",
    var email: String? = "",
    var newMessageCity: Boolean = false,
    var newMessageDepts: Boolean = false,
    var newMessageChats: Boolean = true,
    var isOnline: Boolean = false,
    var lastSeen: Long? = null,
    var additionalFields: Map<String, Any> = emptyMap(),
    var chats: Map<String, Any> = emptyMap()
) {
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "imageUrl" to imageUrl,
        "city" to city,
        "birthday" to birthday,
        "gender" to gender,
        "newMessageCity" to newMessageCity,
        "newMessageDepts" to newMessageDepts,
        "newMessageChats" to newMessageChats,
        "isOnline" to isOnline,
        "lastSeen" to lastSeen
    ) + additionalFields + chats
}

data class SendMessageDto(
    val to: String?,
    val notification: NotificationBody,
    val data: Map<String, String> = emptyMap()
)

data class NotificationBody(
    val title: String,
    val body: String
)


data class ChatData(
    val chatId: String? = "",
    val lastMessage: String? = "",
    val timeLastMessage: Long = 0L,
    val idLastMessage: String? = "",
    val user1: ChatUser = ChatUser(),
    val user2: ChatUser = ChatUser()
)

data class ChatUser(
    val userId: String? = "",
    var name: String? = "",
    var imageUrl: String? = "",
    var city: String? = "",
    var birthday: String? = "",
    var gender: String? = "",
    var deviceToken: String? = "",
    var newMessageChats: Boolean? = false,
    var banUser: Boolean? = false
)

data class Message(
    var sendBy: String? = "",
    val message: String? = "",
    val timestamp: String? = "",
    val normtime: Long = System.currentTimeMillis(),
    val replyToMessageId: String? = null,
    val replyToMessageText: String? = null,
    val replyToMessageSender: String? = null,
    val imageUrl: String? = null
)

data class GroupMessage(
    var deviceToken: String? = "",
    var senderId: String?="",
    val message: String?="",
    val timestamp: String?="",
    val normtime: Long = 0L,
    var name: String? = "",
    var imageUrl: String? = "",
    var city: String? = "",
    var birthday: String? = "",
    var gender: String? = "",
    var cityId: String? = "",
    var newMessageChats: Boolean? = false,
    var isExpanded: Boolean = false,
    var additionalFields: Map<String, Any> = emptyMap()
)
data class GroupMessageDepartments(
    var senderId: String?="",
    val message: String?="",
    val timestamp: String?="",
    val normtime: Long = 0L,
    var name: String? = "",
    var imageUrl: String? = "",
    var city: String? = "",
    var birthday: String? = "",
    var gender: String? = "",
    var departmentId: String? = "",
    var deviceToken: String? = "",
    var newMessageChats: Boolean? = false,
    var isExpanded: Boolean = false
)

data class CityDepartments(
    val cityName: String,
    val administration: List<String>,
    val itDepartment: List<String>,
    val finance: List<String>,
    val hr: List<String>,
    val marketing: List<String>,
)

val cityDepartmentList = listOf(
    CityDepartments(
        cityName = "Москва",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Санкт-Петербург",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Новосибирск",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Екатеринбург",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Казань",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Нижний Новгород",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Красноярск",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Челябинск",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Самара",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Уфа",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Ростов-на-Дону",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Краснодар",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Омск",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Воронеж",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Пермь",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    ),
    CityDepartments(
        cityName = "Волгоград",
        administration = listOf(
            "Дирекция (ген. директор + топ-менеджмент)",
            "Юридический отдел",
            "Служба безопасности",
            "Канцелярия"
        ),
        itDepartment = listOf(
            "Backend-разработка",
            "Frontend-разработка",
            "Мобильная разработка",
            "DevOps",
            "Техподдержка"
        ),
        finance = listOf(
            "Бухгалтерия",
            "Финансовый контроль",
            "Экономический анализ",
            "Казначейство"
        ),
        hr = listOf(
            "Рекрутинг",
            "Обучение персонала",
            "Корпоративная культура",
            "Кадровый учёт"
        ),
        marketing = listOf(
            "Digital-маркетинг",
            "Аналитика рынка",
            "Креативный отдел",
            "SMM"
        )
    )
)