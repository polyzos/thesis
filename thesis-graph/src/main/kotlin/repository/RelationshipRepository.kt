package repository

import models.ParsedReTweet
import models.ParsedReply
import models.ParsedTweet
import models.ParsedUser

interface RelationshipRepository {

    fun createFollowsRelationship(follower: ParsedUser, followee: ParsedUser)

    fun createTweetedRelationship(user: ParsedUser, tweet: ParsedTweet)

    fun createRetweetedRelationship(user: ParsedUser, tweet: ParsedReTweet)

    fun createRepliedRelationship(user: ParsedUser, tweet: ParsedReply)

    fun createRetweetedFromRelationship(tweetID: Long, retweet: ParsedReTweet)

    fun createRepliedToRelationship(tweetID: Long, reply: ParsedReply)
}