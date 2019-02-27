import repository.GraphRepositoryImpl
import repository.Neo4jConnection
import repository.SchemaConstraints

fun main() {

    val connection = Neo4jConnection("bolt://localhost:7687")
    val graphRepository = GraphRepositoryImpl(connection.getDriver())
    val schemaConstraints = SchemaConstraints(connection.getDriver())

    graphRepository.deleteAll()
    schemaConstraints.dropAll()
    schemaConstraints.createConstraints()

    graphRepository.createUserNode(23145, "J. Cole")
    graphRepository.createUserNode(214354, "Logic")
    graphRepository.createUserNode(12453, "Eminem")
    graphRepository.createFollowsRelationship(23145, 12453)
    graphRepository.createFollowsRelationship(214354, 12453)
    graphRepository.createFollowsRelationship(12453, 214354)

    graphRepository.createTweetNode(12345, "Wet Dreamz")
    graphRepository.createTweetedRelationship("J. Cole", 12345)

    graphRepository.createTweetNode(65432, "Everyday")
    graphRepository.createTweetedRelationship("Logic", 65432)
    graphRepository.createRetweetedRelationship("Eminem", 65432)
}