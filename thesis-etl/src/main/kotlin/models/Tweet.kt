package models

import java.util.*

data class Tweet(
    val contributors: ArrayList<Contributor>,
    val coordinates: Coordinates? = null,
    val created_at: Date,
    val current_user_retweet: TweetId? = null,
    val entities: Entities? = null,
    val extended_entities: Entities? = null,
    val extended_tweet: ExtendedTweet? = null,
    val favorite_count: Int = 0,
    val favorited: Boolean = false,
    val filter_level: String? = null,
    val geo: Geo? = null,
    val id: Long,
    val id_str: String,
    val in_reply_to_screen_name: String? = null,
    val in_reply_to_status_id: Long? = null,
    val in_reply_to_status_id_str: String? = null,
    val in_reply_to_user_id: Long? = null,
    val in_reply_to_user_id_str: String? = null,
    val is_quote_status: Boolean = false,
    val lang: String? = null,
    val place: GeoPlace? = null,
    val possibly_sensitive: Boolean = false,
    val quoted_status_id: Long? = null,
    val quoted_status_id_str: String? = null,
    val quoted_status: Tweet? = null,
    val scopes: Map<String, Boolean>,
    val retweet_count: Long = 0,
    val retweeted: Boolean = false,
    val retweeted_status: Tweet? = null,
    val source: String,
    val text: String,
    val truncated: Boolean = false,
    val display_text_range: ArrayList<Int>? = null,
    val user: User? = null,
    val withheld_copyright: Boolean = false,
    val withheld_in_countries: ArrayList<String>? = null,
    val withheld_scope: String? = null,
    val metadata: StatusMetadata? = null)

data class StatusMetadata(val iso_language_code : String, val result_type : String)

data class Contributor(val id: Long, val id_str: String, val screen_name: String)
data class Coordinates(val coordinates: ArrayList<Double>,val `type`: String)
data class TweetId(val id: Long,val id_str: String)

data class Entities(val hashtags: ArrayList<HashTag>,
                    val media: ArrayList<Media>,
                    val symbols: ArrayList<Symbol>,
                    val url: Urls? = null,
                    val urls: ArrayList<UrlDetails>,
                    val user_mentions: ArrayList<UserMention>,
                    val description: Urls? = null)

data class HashTag(val text: String, val indices: ArrayList<Int>)
data class Symbol(val text: String, val indices: ArrayList<Int>)

data class Media(val display_url: String,
                 val expanded_url: String,
                 val id: Long,
                 val id_str: String,
                 val indices: ArrayList<Int>,
                 val media_url: String,
                 val media_url_https: String,
                 val sizes: Map<String, Size>,
                 val source_status_id: Long? = null,
                 val source_status_id_str: String?,
                 val `type`: String,
                 val url: String,
                 val video_info: VideoInfo?)

data class Size(val h: Int,val resize: String,val w: Int)
data class VideoInfo(val aspect_ratio: ArrayList<Int>, val duration_millis: Long?,val variants: ArrayList<Variant>)
data class Variant(val bitrate: Long?, val content_type: String, val url: String)

data class UserMention(val id: Long,
                       val id_str: String,
                       val indices: ArrayList<Int>,
                       val name: String,
                       val screen_name: String)

data class Urls(val urls: ArrayList<UrlDetails>)
data class UrlDetails(val url: String,val expanded_url: String,val display_url: String,val indices: ArrayList<Int>)

data class ExtendedTweet(val full_text: String,val display_text_range: ArrayList<Int>,val entities: Entities? = null,
                         val extended_entities: Entities? = null)

data class Geo(val coordinates: ArrayList<Double>,val  `type`: String)

data class GeoPlace(val attributes: Map<String, String>,
                    val bounding_box: Area,
                    val country: String,
                    val country_code: String,
                    val full_name: String,
                    val id: String,
                    val name: String,
                    val place_type: String,
                    val url: String,
                    val contained_within: ArrayList<GeoPlace>,
                    val centroid: ArrayList<Double>,
                    val polylines: ArrayList<String>)

data class Area(val coordinates: ArrayList<ArrayList<ArrayList<Double>>>, val `type`: String)

data class User(val blocked_by: Boolean = false,
                val blocking: Boolean = false,
                val contributors_enabled: Boolean = false,
                val created_at: Date,
                val default_profile: Boolean = false,
                val default_profile_image: Boolean = false,
                val description: String? = null,
                val email: String? = null,
                val entities: Entities? = null,
                val favourites_count: Int,
                val follow_request_sent: Boolean = false,
                val following: Boolean = false,
                val followers_count: Int,
                val friends_count: Int,
                val geo_enabled: Boolean = false,
                val has_extended_profile: Boolean = false,
                val id: Long,
                val id_str: String,
                val is_translation_enabled: Boolean = false,
                val is_translator: Boolean = false,
                val lang: String,
                val listed_count: Int,
                val location: String? = null,
                val muting: Boolean = false,
                val name: String,
                val notifications: Boolean = false,
                val profile_background_color: String,
                val profile_background_image_url: String?,
                val profile_background_image_url_https: String?,
                val profile_background_tile: Boolean = false,
                val profile_banner_url: String? = null,
                val profile_image_url: ProfileImage,
                val profile_image_url_https: ProfileImage,
                val profile_link_color: String,
                val profile_location: String?,
                val profile_sidebar_border_color: String,
                val profile_sidebar_fill_color: String,
                val profile_text_color: String,
                val profile_use_background_image: Boolean = false,
                val `protected`: Boolean = false,
                val screen_name: String,
                val show_all_inline_media: Boolean = false,
                val status: Tweet? = null,
                val statuses_count: Int,
                val time_zone: String? = null,
                val translator_type: String? = null,
                val url: String? = null,
                val utc_offset: Int? = null,
                val verified: Boolean = false,
                val withheld_in_countries: String? = null,
                val withheld_scope: String? = null)

data class ProfileImage(val mini: String,val normal: String,val bigger: String, val default: String)