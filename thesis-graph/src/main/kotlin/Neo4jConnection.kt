import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Values.parameters

class Neo4jConnection(uri: String,
                      user: String? = null,
                      password: String? = null): AutoCloseable {
    private val driver: Driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))

    fun createUserNode(id: Long, screenName: String) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MERGE (user: User {screen_name:'$screenName',id : '$id'})
                            RETURN user.id""",
                        parameters("id", id, "screen_name", screenName))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn in createUserNode: $e")
        }
    }

    fun createFollowsRelationship(follower: Long, followee: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (follower: User {id: '$follower'})
                            MATCH (followee: User {id: '$followee'})
                            MERGE (follower)-[:FOLLOWS]->(followee)
                            RETURN follower.id, followee.id""",
                        parameters("id", follower, "id", followee))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn in createFollowsRelationship: $e")
        }
    }

    fun createTweetNode(id: Long, type: String) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MERGE (tweet: Tweet {id: '$id', type:'$type'})
                            RETURN tweet.id""",
                        parameters("id", id, "type", type))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn in createTweetNode: $e")
        }
    }

    fun createTweetedRelationship(userid: Long, tweetid: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run( """
                        MATCH (user:User {id:'$userid'})
                        MATCH (tweet:Tweet {id:'$tweetid'})
                        MERGE (user)-[:TWEETED]->(tweet)
                        RETURN user.id, tweet.id
                        """,
                        parameters("id", userid, "id", tweetid))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn in createTweetedRelationship: $e")
        }
    }

    fun createRetweetedRelationship(userid: Long, tweetid: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (user:User {id:'$userid'})
                            MATCH (tweet:Tweet {id:'$tweetid'})
                            MERGE (user)-[:RETWEETED]->(tweet)
                            RETURN user.id, tweet.id
                            """,
                        parameters("id", userid, "id", tweetid))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn in createRetweetedRelationship: $e")
        }
    }

    fun getTweetInfo(id: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                       MATCH (tweet:Tweet {id: '$id'})
                       RETURN tweet.id
                       """,
                    parameters("id", id))
                    .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn in getTweetInfo: $e")
        }
    }

    fun deleteAll() {
        try {
            println("Dropping all data from the database.")
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (n)
                            DETACH DELETE n
                        """
                    )
                }
        } catch (e: Throwable) {
            println("Failed txn in deleteAll: $e")
        }
    }

    override fun close() {
        driver.close()
    }
}