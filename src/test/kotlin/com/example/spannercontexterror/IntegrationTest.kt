package com.example.spannercontexterror

import com.example.spannercontexterror.utils.SpannerTestContainerConfiguration
import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.NoCredentialsProvider
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension

@ContextConfiguration(
    initializers = [
        SpannerTestContainerConfiguration::class]
)
@ExtendWith(value = [SpringExtension::class, SystemStubsExtension::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationTest {
    companion object {

        @SystemStub
        lateinit var environmentVariables: EnvironmentVariables

        @TestConfiguration
        internal class EmulatorConfiguration {
            @Bean
            fun googleCredentials(): CredentialsProvider {
                return NoCredentialsProvider.create()
            }
        }
    }
}