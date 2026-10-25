package com.example.data

enum class AppCategory(val title: String, val subtitle: String) {
    ALL("Все", "Все сервисы"),
    BANKING("Банки", "Сбер, Т-Банк, ВТБ, Mir Pay"),
    MARKETPLACE("Маркетплейсы", "Озон, WB, Авито, Маркет"),
    GOSUSLUGI("Госуслуги", "Портал Госуслуг, ФНС, Мос.ру"),
    MESSENGER_TAXI("Сервисы и связь", "МАКС, Такси, VK, Купер, Еда"),
    OTHER("Другие", "Пользовательские приложения")
}

enum class TrackingRisk(val label: String, val description: String) {
    CRITICAL("Строгая блокировка", "Блокирует вход и операции при зарубежном VPN IP"),
    HIGH("Детектирует VPN", "Повышенный антифрод, капчи, искажение региональных цен"),
    MODERATE("Региональный контент", "Снижение скорости или недоступность отдельных функций")
}
