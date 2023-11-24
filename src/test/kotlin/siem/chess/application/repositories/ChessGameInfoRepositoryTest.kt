package siem.chess.application.repositories

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import siem.chess.ChessApplication
import siem.chess.TestChessApplication
import siem.chess.domain.queryside.LogMessage
import siem.chess.domain.queryside.LogMessageType
import java.time.LocalDateTime

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [ChessApplication::class, TestChessApplication::class])
class ChessGameInfoRepositoryTest {

    @Autowired
    lateinit var chessGameInfoLogRepository: ChessGameInfoLogRepository


    @Test
    fun `getLogMessages should return  all logMessages of a chessGame with a given gameId` () {

        //Given
        val now = nowWithoutNano()
        val nowPlus1Second = now.plusSeconds(1)
        val nowPlus2Second = now.plusSeconds(2)

        givenLogMessage(LogMessage(gameId = "43", logTime = now, logMessageType = LogMessageType.GAME_STARTED, message = "Game started"))
        givenLogMessage(LogMessage(gameId = "43" ,logTime = nowPlus1Second, logMessageType = LogMessageType.MOVE_BY_WHITE, message = "D2-D4"))
        givenLogMessage(LogMessage(gameId = "43", logTime = nowPlus2Second, logMessageType = LogMessageType.MOVE_BY_BLACK, message = "D7-D5"))

        givenLogMessage(LogMessage(gameId = "11", now, logMessageType = LogMessageType.GAME_STARTED, "D7-D5"))


        //When
        val checkGameLogInfo = chessGameInfoLogRepository.getChessGameLog("43")

        //Then
        assertThat(checkGameLogInfo.gameId).isEqualTo("43")

        val expectedCheckGameLogInfo =  listOf(
            LogMessage(gameId = "43", logTime = now, logMessageType = LogMessageType.GAME_STARTED, message = "Game started"),
            LogMessage(gameId = "43" ,logTime = nowPlus1Second, logMessageType = LogMessageType.MOVE_BY_WHITE, message = "D2-D4"),
            LogMessage(gameId = "43", logTime = nowPlus2Second, logMessageType = LogMessageType.MOVE_BY_BLACK, message = "D7-D5")
        )

        assertThat(checkGameLogInfo.entries).hasSameElementsAs(expectedCheckGameLogInfo)
    }

    private fun givenLogMessage(logMessage: LogMessage) {
        chessGameInfoLogRepository.insertLogMessage(logMessage)
    }

    private fun nowWithoutNano(): LocalDateTime {
        return LocalDateTime.now().withNano(0)
    }
}