package repository

import models.ParsedReTweet
import models.ParsedReply
import models.ParsedTweet
import models.ParsedUser
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Values

class NodeRepositoryImpl(private val driver: Driver): NodeRepository {

    override fun createUserNode(user: ParsedUser) {
        val name = user.screen_name
        val id = user.id
        val followers = user.followers_count
        val friends = user.friends_count
        try {
            driver.session()
                .writeTransaction {
                    it.run("""
                        MERGE (user: User {id: $id})
                        ON CREATE SET user.screen_name = '$name', user.followers = $followers, user.friends = $friends
                        RETURN user.id""".trimIndent())
                        .single().get(0)
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun createTweetNode(tweet: ParsedTweet) {
        val id = tweet.id
        val text = tweet.text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'")
        val date = tweet.created_at.toString()
        try {
            driver.session()
                .writeTransaction {
                    it.run("""
                        MERGE (tweet: Tweet {id: $id})
                        ON CREATE SET tweet.text = '$text', tweet.created_at = '$date', tweet.type = 'TWEET'
                        RETURN tweet.id""".trimIndent())
                        .single().get(0)
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun createRetweetNode(tweet: ParsedReTweet) {
        val id = tweet.id
        val text = tweet.text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'")
        val date = tweet.created_at.toString()
        try {
            driver.session()
                .writeTransaction {
                    it.run("""
                        MERGE (tweet: Tweet {id: $id})
                        ON CREATE SET tweet.text = '$text', tweet.created_at = '$date', tweet.type = 'RETWEET'
                        RETURN tweet.id""".trimIndent())
                        .single().get(0)
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun createReplyNode(tweet: ParsedReply) {
        val id = tweet.id
        val text = tweet.text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'")
        val date = tweet.created_at.toString()
        try {
            driver.session()
                .writeTransaction {
                    it.run("""
                        MERGE (tweet: Tweet {id: $id})
                        ON CREATE SET tweet.text = '$text', tweet.created_at = '$date', tweet.type = 'REPLY'
                        RETURN tweet.id""".trimIndent())
                        .single().get(0)
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}