package repository

import models.ParsedReTweet
import models.ParsedReply
import models.ParsedTweet
import models.ParsedUser

interface NodeRepository {

    fun createUserNode(user: ParsedUser)

    fun createTweetNode(tweet: ParsedTweet)

    fun createRetweetNode(tweet: ParsedReTweet)

    fun createReplyNode (tweet: ParsedReply)
}