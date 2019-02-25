package runners

import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Values.parameters

class Neo4jConnection(uri: String,
                      user: String? = null,
                      password: String? = null): AutoCloseable {
    private val driver: Driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))

    fun clearDB() {
        println("Deleting all previous records from the database.")
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (n) DETACH DELETE n
                        """.trimIndent()
                    )
                }
        } catch (e: Throwable) {
            println("Failed txn: $e")
        }
    }

    fun createUserNode(id: Long, screenName: String) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MERGE (u: User {screen_name:'$screenName',id : '$id'})
                            RETURN u.id""",
                        parameters("id", id, "screen_name", screenName))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn: $e")
        }
    }

    fun createFollowsRelationship(follower: String, followee: String) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (follower: User {screen_name: '$follower'})
                            MATCH (followee: User {screen_name: '$followee'})
                            MERGE (follower)-[:FOLLOWS]->(followee)
                            RETURN follower.screen_name, followee.screen_name""",
                        parameters("screen_name", follower))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn: $e")
        }
    }

    fun createPostNode(id: Long, type: String) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MERGE (p: Post {id:'$id', type:'$type'})
                            RETURN p.id""",
                        parameters("id", id, "type", type))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn: $e")
        }
    }

    fun createTweetedRelationship(screenName: String, id: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run( """
                        MATCH (u: User {screen_name:'$screenName'})
                        MATCH (p: Post {id:'$id'})
                        MERGE (u)-[:TWEETED]->(p)
                        RETURN u.id, p.id
                        """,
                        parameters("screen_name", screenName, "id", id))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn: $e")
        }
    }

    fun createRetweetedFromRelationship(tweetId: Long, retweetId: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (p1:Post {id:'$tweetId'})
                            MATCH (p2:Post {id:'$retweetId'})
                            MERGE (p1)-[:RETWEETED_FROM]->(p2)
                            RETURN p1.id, p2.id
                            """,
                        parameters("tweetId", tweetId, "retweetId", retweetId))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn: $e")
        }
    }

    fun createRetweetedRelationship(screenName: String, id: Long) {
        try {
            driver.session()
                .writeTransaction {
                    it.run(
                        """
                            MATCH (u:User {screen_name:'$screenName'})
                            MATCH (p: Post {id:'$id'})
                            MERGE (u)-[:MADE_RETWEETED]->(p)
                            RETURN u.id, p.id
                            """,
                        parameters("screen_name", screenName, "id", id))
                        .single().get(0).asString()
                }
        } catch (e: Throwable) {
            println("Failed txn: $e")
        }
    }

    override fun close() {
        driver.close()
    }
}