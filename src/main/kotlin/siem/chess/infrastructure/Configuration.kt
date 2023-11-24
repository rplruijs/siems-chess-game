package siem.chess.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Configuration {
    @Bean
    fun initializingBean(objectMapper: ObjectMapper) : InitializingBean {
        return InitializingBean { objectMapper.activateDefaultTyping(
            objectMapper.polymorphicTypeValidator,
            ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT)
        }
    }
}