package models

import java.util.*


data class ParsedTweet(
    val created_at: Date,
    val id: Long,
    val text: String,
    val user_followers_count: Long,
    val user_friends_count: Long,
    val user_id: Long,
    val user_screen_name: String
)