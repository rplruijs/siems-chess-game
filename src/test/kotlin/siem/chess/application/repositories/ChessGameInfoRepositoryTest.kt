package siem.chess.application.repositories

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import siem.chess.ChessApplication
import siem.chess.TestChessApplication
import siem.chess.domain.queryside.ActorType
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




        val logMessageStartGame43 = LogMessage(
            gameId = "43",
            logTime = now,
            logMessageType = LogMessageType.GAME_STARTED,
            causedBy = ActorType.SYSTEM,
            message = "Game started"
        )

        val logMessageD2D4Game43 = LogMessage(
            gameId = "43" ,
            logTime = nowPlus1Second,
            logMessageType = LogMessageType.MOVE,
            causedBy = ActorType.WHITE_PLAYER,
            message = "D2-D4")

        val logMessageD7D5Game43 = LogMessage(
            gameId = "43",
            logTime = nowPlus2Second,
            logMessageType = LogMessageType.MOVE,
            message = "D7-D5",
            causedBy = ActorType.BLACK_PLAYER,
        )


        val logMessageStartGame11 = LogMessage(
            gameId = "11",
            logTime = now,
            logMessageType = LogMessageType.GAME_STARTED,
            causedBy = ActorType.SYSTEM,
            message = "Game started")



        givenLogMessage(logMessageStartGame43)
        givenLogMessage(logMessageD2D4Game43)
        givenLogMessage(logMessageD7D5Game43)

        givenLogMessage(logMessageStartGame11)


        //When
        val checkGameLogInfo = chessGameInfoLogRepository.getChessGameLog("43")

        //Then
        assertThat(checkGameLogInfo.gameId).isEqualTo("43")

        val expectedCheckGameLogInfo =  listOf(logMessageStartGame43, logMessageD2D4Game43, logMessageD7D5Game43)

        assertThat(checkGameLogInfo.entries).hasSameElementsAs(expectedCheckGameLogInfo)
    }

    private fun givenLogMessage(logMessage: LogMessage) {
        chessGameInfoLogRepository.insertLogMessage(logMessage)
    }

    private fun nowWithoutNano(): LocalDateTime {
        return LocalDateTime.now().withNano(0)
    }
}