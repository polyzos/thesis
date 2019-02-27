package repository

interface GraphRepository {

    fun createUserNode(id: Long, screenName: String)

    fun createFollowsRelationship(follower: Long, followee: Long)

    fun createFollowsRelationship(follower: String, followee: String)

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