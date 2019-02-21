package models

import java.util.*

data class Post (
    val created_at: Date,
    val id: Long,
    val text: String,
    val user_followers_count: Int,
    val user_friends_count: Int,
    val user_id: Long,
    val user_screen_name: String,
    val user_verified: Boolean
)

data class RetweetInfo (
    val created_at: Date,
    val id: Long,
    val retweet_status: RetweetStatus,
    val text: String,
    val user_followers_count: Int,
    val user_friends_count: Int,
    val user_id: Long,
    val user_screen_name: String,
    val user_verified: Boolean
)

data class RetweetStatus(
    val retweet_status_id: Long,
    val retweet_status_text: String,
    val user: UserInfo
    )

data class UserInfo(
    val followers_count: Int,
    val friends_count: Int,
    val id: Long,
    val screen_name: String,
    val verified: Boolean
)