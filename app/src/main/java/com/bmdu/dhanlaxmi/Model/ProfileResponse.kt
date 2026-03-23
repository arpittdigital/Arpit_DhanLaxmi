package com.bmdu.dhanlaxmi.Model

data class ProfileResponse(
    val success: Boolean,
    val data: ProfileData?
)

data class ProfileData(
    val name: String,
    val password: String,
    val phone: String?,
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

// Data model
data class ContactResponse(
    val status: Boolean,
    val message: String,
    val data: List<ContactData>
)

data class ContactData(
    val id: Int,
    val contact_number: String,
    val whatsapp_number: String,
    val upi_id: String,
    val telegram_link: String
)

















