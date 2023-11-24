package siem.chess

import org.axonframework.test.server.AxonServerContainer
import org.springframework.boot.devtools.restart.RestartScope
import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.with
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestChessApplication {

	@Bean
	@ServiceConnection
	@RestartScope
	fun postgresContainer(): PostgreSQLContainer<*> {
		return PostgreSQLContainer(DockerImageName.parse("postgres:latest"))
	}

	@Bean
	@ServiceConnection(name="axonServerContainer")
	@RestartScope
	fun axonServerContainer() : AxonServerContainer {
		return AxonServerContainer(DockerImageName.parse("axoniq/axonserver:latest-dev"))
	}

}

fun main(args: Array<String>) {
	fromApplication<ChessApplication>().with(TestChessApplication::class).run(*args)
}