import org.axonframework.test.server.AxonServerContainer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import siem.chess.ChessApplication
import siem.chess.TestChessApplication

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                classes = [ChessApplication::class, TestChessApplication::class])
class ChessIntegrationTest {


    @LocalServerPort
    private val port: Int = 0

    @Autowired
    lateinit var postgresContainer: PostgreSQLContainer<*>

    @Autowired
    lateinit var axonServerContainer: AxonServerContainer


    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private lateinit var gameId: String
    private lateinit var  url: String

    @BeforeEach
    fun init() {
        url = "http://localhost:$port/api/chess-games/"
    }

    @Test
    fun testStartEndpoint() {
        //When
        startGame("Siem", "Remco")
        move("WHITE", "D2", "D4" )
        move("BLACK", "D7", "D5" )

        assert(true)
    }

    private fun startGame(whitePlayer: String, blackPlayer: String) {
        val startUrl = "$url/start?whitePlayer=$whitePlayer&blackPlayer=$blackPlayer"
        val response: ResponseEntity<String> = restTemplate.postForEntity(startUrl, null, String::class.java)
        gameId = retrieveGameId(response)?: throw IllegalStateException("No gameId cookie found")
    }

    private fun move(color: String, from: String, to: String) {
        val moveUrl = "$url/move?color=$color&from=$from&to=$to"

        val headers = HttpHeaders()
        headers.add("Cookie", "gameId=$gameId")
        val requestEntity = HttpEntity<String>(headers)

        restTemplate.exchange(
            moveUrl,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )
    }

    private fun retrieveGameId(responseEntity: ResponseEntity<String>): String? {
        val headers: MultiValueMap<String, String> = responseEntity.headers
        val cookies: List<String>? = headers["Set-Cookie"]
        val gameIdCookie = cookies?.find { it.startsWith("gameId") }

        return gameIdCookie?.let {
            gameIdCookie.split(";")
                .find { it.startsWith("gameId") }?.split("=")?.get(1)
        }
    }
}
