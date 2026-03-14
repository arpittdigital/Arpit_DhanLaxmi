package com.bmdu.dhanlaxmi.Model

data class ProfileResponse(
    val success: Boolean,
    val data: ProfileData?
)

data class ProfileData(
    val name: String,
    val password: String,
    val customer_id: String,
    val wallet_amount: Int?
)


data class NotificationResponse(
    val success: Boolean,
    val message: String?,
    val data: List<NotificationItem>?
)

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val is_read: Int,           // 0 = unread, 1 = read
    val created_at: String?,
    val updated_at: String?
)


















