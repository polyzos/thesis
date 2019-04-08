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

data class ParsedReTweet(
    val created_at: Date,
    val id: Long,
    val retweeted_status_id: Long,
    val retweeted_status_text: String,
    val retweeted_status_user_followers_count: Long,
    val retweeted_status_user_friends_count: Long,
    val retweeted_status_user_id: Long,
    val retweeted_status_user_screen_name: String,
    val text: String,
    val user_followers_count: Long,
    val user_friends_count: Long,
    val user_id: Long,
    val user_screen_name: String
)

data class ParsedReply(
    val created_at: Date,
    val id: Long,
    val in_reply_to_screen_name: String,
    val in_reply_to_status_id: Long,
    val in_reply_to_user_id: Long,
    val text: String,
    val user_followers_count: Long,
    val user_friends_count: Long,
    val user_id: Long,
    val user_screen_name: String
)

data class ParsedUser(
    val id: Long,
    val screen_name: String,
    val followers_count: Long,
    val friends_count: Long
)