package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [BypassedAppEntity::class], version = 1, exportSchema = false)
abstract class RuDirectDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: RuDirectDatabase? = null

        fun getDatabase(context: Context): RuDirectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RuDirectDatabase::class.java,
                    "rudirect_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with default Russian catalog
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.appDao()?.insertAll(DefaultCatalog.INITIAL_APPS)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object DefaultCatalog {
    val INITIAL_APPS = listOf(
        // Marketplaces & Retail
        BypassedAppEntity(
            packageName = "ru.ozon.app.android",
            appName = "Ozon",
            category = AppCategory.MARKETPLACE.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.HIGH.name,
            directBypassReason = "Предотвращает капчи, искажение региональных цен и блокировки корзины при VPN",
            iconFallbackColor = 0xFF005BFF
        ),
        BypassedAppEntity(
            packageName = "com.wildberries.work",
            appName = "Wildberries",
            category = AppCategory.MARKETPLACE.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.HIGH.name,
            directBypassReason = "Обходит защиту антифрода, исключает сбои геолокации ПВЗ и блокировку карт",
            iconFallbackColor = 0xFFCB11AB
        ),
        BypassedAppEntity(
            packageName = "com.avito.android",
            appName = "Авито",
            category = AppCategory.MARKETPLACE.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.HIGH.name,
            directBypassReason = "Прямой доступ к объявлениям и звонкам; сервис блокирует доступ с зарубежных IP",
            iconFallbackColor = 0xFF00AAFF
        ),
        BypassedAppEntity(
            packageName = "ru.yandex.market",
            appName = "Яндекс Маркет",
            category = AppCategory.MARKETPLACE.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.MODERATE.name,
            directBypassReason = "Быстрая загрузка каталогов, прямая работа Яндекс Сплита и Плюса",
            iconFallbackColor = 0xFFFFCC00
        ),
        BypassedAppEntity(
            packageName = "com.sbermarket",
            appName = "Купер (СберМаркет)",
            category = AppCategory.MARKETPLACE.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.MODERATE.name,
            directBypassReason = "Служба доставки продуктов требует прямого подключения к локальным серверам",
            iconFallbackColor = 0xFF21A038
        ),
        BypassedAppEntity(
            packageName = "ru.samokat.app",
            appName = "Самокат",
            category = AppCategory.MARKETPLACE.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.MODERATE.name,
            directBypassReason = "Прямой поиск ближайших дарксторов и моментальная оплата СБП",
            iconFallbackColor = 0xFFFF3366
        ),

        // Banking & Financial
        BypassedAppEntity(
            packageName = "ru.sberbankmobile",
            appName = "СберБанк Онлайн",
            category = AppCategory.BANKING.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.CRITICAL.name,
            directBypassReason = "Строгая система безопасности Сбера: блокирует вход и переводы при активном зарубежном VPN",
            iconFallbackColor = 0xFF21A038
        ),
        BypassedAppEntity(
            packageName = "com.idamob.tinkoff.android",
            appName = "Т-Банк (Тинькофф)",
            category = AppCategory.BANKING.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.CRITICAL.name,
            directBypassReason = "Антифрод Т-Банка замораживает сессии и требует подтверждения при подозрительных IP",
            iconFallbackColor = 0xFFFFDD2D
        ),
        BypassedAppEntity(
            packageName = "ru.alfabank.mobile.android",
            appName = "Альфа-Банк",
            category = AppCategory.BANKING.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.CRITICAL.name,
            directBypassReason = "Прямой мобильный интернет для быстрой авторизации без геоблокировок",
            iconFallbackColor = 0xFFEF3124
        ),
        BypassedAppEntity(
            packageName = "ru.vtb24.mobilebanking.android",
            appName = "ВТБ Онлайн",
            category = AppCategory.BANKING.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.CRITICAL.name,
            directBypassReason = "Доступ к ВТБ Онлайн ограничен из-за границы; требуется чистый российский трафик",
            iconFallbackColor = 0xFF0A2896
        ),
        BypassedAppEntity(
            packageName = "ru.gazprombank.android.mobilebank.app",
            appName = "Газпромбанк",
            category = AppCategory.BANKING.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.CRITICAL.name,
            directBypassReason = "Защита банковских операций и платежей через прямой канал оператора",
            iconFallbackColor = 0xFF004696
        ),
        BypassedAppEntity(
            packageName = "ru.nspk.mirpay",
            appName = "Mir Pay",
            category = AppCategory.BANKING.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.CRITICAL.name,
            directBypassReason = "Национальная система платежных карт отклоняет регистрацию и оплату через VPN",
            iconFallbackColor = 0xFF00965E
        ),

        // Government & Tax
        BypassedAppEntity(
            packageName = "ru.gosuslugi.online",
            appName = "Госуслуги",
            category = AppCategory.GOSUSLUGI.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.CRITICAL.name,
            directBypassReason = "Госуслуги блокируют доступ при наличии признаков зарубежного VPN или прокси",
            iconFallbackColor = 0xFF0D4CD3
        ),
        BypassedAppEntity(
            packageName = "ru.gosuslugi.pos",
            appName = "Госуслуги Решаем вместе",
            category = AppCategory.GOSUSLUGI.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.HIGH.name,
            directBypassReason = "Прямая отправка геопривязанных обращений без сетевых помех",
            iconFallbackColor = 0xFF0D4CD3
        ),
        BypassedAppEntity(
            packageName = "ru.nalog.FL",
            appName = "Мой налог / ФНС",
            category = AppCategory.GOSUSLUGI.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.CRITICAL.name,
            directBypassReason = "Личный кабинет налогоплательщика фильтрует не-российские сетевые соединения",
            iconFallbackColor = 0xFF005BAA
        ),
        BypassedAppEntity(
            packageName = "ru.mos.app",
            appName = "Моя Москва (Mos.ru)",
            category = AppCategory.GOSUSLUGI.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.HIGH.name,
            directBypassReason = "Городские услуги и сервисы передачи показаний ЖКХ",
            iconFallbackColor = 0xFFCC0000
        ),

        // Messengers, Transport & SuperApps
        BypassedAppEntity(
            packageName = "com.max.app",
            appName = "МАКС (Max Messenger)",
            category = AppCategory.MESSENGER_TAXI.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.HIGH.name,
            directBypassReason = "Отслеживание VPN-протоколов и корпоративных соединений, прямой скоростной доступ",
            iconFallbackColor = 0xFF00C853
        ),
        BypassedAppEntity(
            packageName = "ru.yandex.taxi",
            appName = "Яндекс Go (Такси)",
            category = AppCategory.MESSENGER_TAXI.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.MODERATE.name,
            directBypassReason = "Точное определение геолокации без задержек прокси-серверов",
            iconFallbackColor = 0xFFFC3F1D
        ),
        BypassedAppEntity(
            packageName = "com.vkontakte.android",
            appName = "ВКонтакте",
            category = AppCategory.MESSENGER_TAXI.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.MODERATE.name,
            directBypassReason = "Стриминг видео в 4K и прямая загрузка контента без лимитов VPN-трафика",
            iconFallbackColor = 0xFF0077FF
        ),
        BypassedAppEntity(
            packageName = "ru.mail.mailapp",
            appName = "Почта Mail.ru",
            category = AppCategory.MESSENGER_TAXI.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.MODERATE.name,
            directBypassReason = "Прямое подключение к почтовым серверам и мгновенные push-уведомления",
            iconFallbackColor = 0xFF168DE2
        )
    )
}
