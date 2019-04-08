package repository

import models.ParsedReTweet
import models.ParsedReply
import models.ParsedTweet
import models.ParsedUser
import org.neo4j.driver.v1.Driver

class RelationshipRepositoryImpl(private val driver: Driver): RelationshipRepository {

    override fun createFollowsRelationship(follower: ParsedUser, followee: ParsedUser) {
        val followerID = follower.id
        val followeeID = followee.id
        try {
            driver.session()
                .writeTransaction {
                    it.run("""
                        MATCH (follower: User {id: $followerID})
                        MATCH (followee: User {id: $followeeID})
                        MERGE (follower)-[:FOLLOWS]->(followee)
                        RETURN follower.id, followee.id""".trimIndent())
                        .single().get(0)
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun createTweetedRelationship(user: ParsedUser, tweet: ParsedTweet) {
        val userID = user.id
        val tweetID = tweet.id
        try {
            driver.session()
                .writeTransaction {
                    it.run("""
                        MATCH (u: User {id: $userID})
                        MATCH (t: Tweet {id: $tweetID})
                        MERGE (u)-[:TWEETED]->(t)
                        RETURN u.id, t.id""".trimIndent())
                        .single().get(0)
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun createRetweetedRelationship(user: ParsedUser, tweet: ParsedReTweet) {
        val userID = user.id
        val tweetID = tweet.id
        val date = tweet.created_at
        try {
            driver.session()
                .writeTransaction {
                    it.run("""
                        MATCH (u: User {id: $userID})
                        MATCH (t: Tweet {id: $tweetID})
                        MERGE (u)-[:POSTED_RETWEET {timestamp: '$date'}]->(t)
                        RETURN u.id, t.id""".trimIndent())
                        .single().get(0)
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun createRepliedRelationship(user: ParsedUser, tweet: ParsedReply) {
        val userID = user.id
        val tweetID = tweet.id
        val date = tweet.created_at
        try {
            driver.session()
                .writeTransaction {
                    it.run("""
                        MATCH (u: User {id: $userID})
                        MATCH (t: Tweet {id: $tweetID})
                        MERGE (u)-[:POSTED_REPLY {timestamp: '$date'}]->(t)
                        RETURN u.id, t.id""".trimIndent())
                        .single().get(0)
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun createRetweetedFromRelationship(tweetID: Long, retweet: ParsedReTweet) {
        val retweetID = retweet.id
        val date = retweet.created_at
        try {
            driver.session()
                .writeTransaction {
                    it.run("""
                        MATCH (t: Tweet {id: $tweetID})
                        MATCH (re: Tweet {id: $retweetID})
                        MERGE (t)<-[:RETWEETED_FROM {timestamp: '$date'}]-(re)
                        RETURN t.id, re.id""".trimIndent())
                        .single().get(0)
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun createRepliedToRelationship(tweetID: Long, reply: ParsedReply) {
        val retweetID = reply.id
        val date = reply.created_at
        try {
            driver.session()
                .writeTransaction {
                    it.run("""
                        MATCH (t: Tweet {id: $tweetID})
                        MATCH (re: Tweet {id: $retweetID})
                        MERGE (t)<-[:REPLIED_TO {timestamp: '$date'}]-(re)
                        RETURN t.id, re.id""".trimIndent())
                        .single().get(0)
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}