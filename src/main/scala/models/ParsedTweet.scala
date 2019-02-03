package models

import java.util.Date

import com.danielasfregola.twitter4s.entities.{Tweet, TweetId, User}

case class ParsedTweet(
                        created_at: Date,
                        current_user_retweet: Option[TweetId] = None,
                        favorite_count: Int = 0,
                        id: Long,
                        in_reply_to_screen_name: Option[String] = None,
                        in_reply_to_status_id: Option[Long] = None,
                        in_reply_to_user_id: Option[Long] = None,
                        lang: Option[String] = None,
                        retweet_count: Long = 0,
                        retweeted: Boolean = false,
                        retweeted_status: Option[Tweet] = None,
                        source: String,
                        text: String,
                        user: Option[User] = None
                      )
