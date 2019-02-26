package repository

import org.neo4j.driver.v1.Values

interface GraphRepository {

    fun createUserNode(id: Long, screenName: String)

    fun createFollowsRelationship(follower: Long, followee: Long)

    fun createTweetNode(id: Long, type: String)

    fun createTweetedRelationship(screenName: String, id: Long)

    fun createRetweetedRelationship(screenName: String, id: Long)

    fun createRepliedToRelationship(tweetId: Long, replyId: Long)

    fun createRetweetedFromRelationship(tweetId: Long, retweetId: Long)

    fun getTweetInfo(id: Long)

    fun deleteAll()

    fun getTotalNodesCount(): Int

    fun getUserNodesCount(): Int

    fun getTweetNodesCount(): Int

    fun getTotalEdgesCount(): Int

    fun getFollowsEdgesCount(): Int

    fun getTweetedEdgesCount(): Int

    fun getRetweetedEdgesCount(): Int

}