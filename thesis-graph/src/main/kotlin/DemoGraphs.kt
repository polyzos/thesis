import org.neo4j.driver.v1.Driver
import repository.*

fun main() {

    val connection = Neo4jConnection("bolt://localhost:7687")
    val graphRepository = GraphRepositoryImpl(connection.getDriver())
    val schemaConstraints = SchemaConstraints(connection.getDriver())
    val nodeRepository = NodeRepositoryImpl(connection.getDriver())
    val relationshipRepository = RelationshipRepositoryImpl(connection.getDriver())

//    graphRepository.deleteAll()
//    schemaConstraints.dropAll()
//    schemaConstraints.createConstraints()

//    graphRepository.createUserNode(23145, "J. Cole")
//    graphRepository.createUserNode(214354, "Logic")
//    graphRepository.createUserNode(12453, "Eminem")
//    graphRepository.createFollowsRelationship(23145, 12453)
//    graphRepository.createFollowsRelationship(214354, 12453)
//    graphRepository.createFollowsRelationship(12453, 214354)
//
//    graphRepository.createTweetNode(12345, "Wet Dreamz")
//    graphRepository.createTweetedRelationship("J. Cole", 12345)
//
//    graphRepository.createTweetNode(65432, "Everyday")
//    graphRepository.createTweetedRelationship("Logic", 65432)
//    graphRepository.createRetweetedRelationship("Eminem", 65432)

    getTweetChain(connection.getDriver())


}

fun getTweetChain(driver: Driver) {
    try {
        val chain = driver.session()
            .writeTransaction {
                it.run("""
                    MATCH p=(:Tweet)-[re1:REPLIED_TO|RETWEETED_FROM*]->(:Tweet)-[re2:REPLIED_TO|RETWEETED_FROM]->(:Tweet {type: 'TWEET'})
                    RETURN re1.timestamp, re2.timestamp LIMIT 150""".trimIndent()).list()
            }
        chain.forEach { println(it) }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}