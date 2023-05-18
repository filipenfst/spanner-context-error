package com.example.spannercontexterror


import com.example.spannercontexterror.utils.ConnectionFactoryWrapper
import com.example.spannercontexterror.utils.ContextTestEntity
import com.example.spannercontexterror.utils.ContextTestRepository
import com.example.spannercontexterror.utils.TransactionProvider
import com.google.cloud.spanner.r2dbc.SpannerConnectionFactoryProvider
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.shaded.org.awaitility.Awaitility
import reactor.core.publisher.Hooks
import java.util.UUID
import java.util.concurrent.TimeUnit


@ActiveProfiles("test", "test-with-wrapper")
class TransactionTest(
    @Autowired
    private val contextTestCoroutineRepository: ContextTestRepository,
    @Autowired
    private val transactionProvider: TransactionProvider,
) : IntegrationTest() {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setupAll() {
            Hooks.enableAutomaticContextPropagation()
            Hooks.enableContextLossTracking()
            Hooks.onOperatorDebug()
        }
    }


    @BeforeEach
    fun setup(): Unit = runBlocking {
        (1 until 10).map {
            ContextTestEntity(
                id = "id-$it",
                value = "value-$it",
            )
        }.also {
            contextTestCoroutineRepository.saveAll(it).collect()
        }
    }

    @Test
    fun test(): Unit = runBlocking {
        val entities = (0 until 10).map { UUID.randomUUID() }.map {
            ContextTestEntity(
                id = "id-$it",
                value = "value-$it",
            )
        }
        val entitiesFlow = entities.asFlow()

        val job = GlobalScope.launch {
            repeat(10) {
                try {
                    transactionProvider.withTransaction {
                        contextTestCoroutineRepository.saveAll(entitiesFlow).onEach {
                            contextTestCoroutineRepository.findById(it.id)?.also { c -> print("Found $c") }
                        }.also {
                            println("Saved $it")
                        }
                        throw TestException()
                    }
                } catch (e: TestException) {
                    println("Exception: ${e.message}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }

            transactionProvider.withTransaction {
                contextTestCoroutineRepository.saveAll(entitiesFlow).count().also {
                    println("Final Save $it")
                }
            }
        }

        Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .pollDelay(1, TimeUnit.MILLISECONDS)
            .until {
                runBlocking {
                    entities.mapNotNull {
                        contextTestCoroutineRepository.findById(it.id)
                    }.size == entities.size
                }
            }
        job.join()
    }

    private class TestException : RuntimeException("Test exception")
}


@Configuration
@Profile("test-with-wrapper")
class DatabaseConfiguration(
    @Value("\${spring.r2dbc.url}") private val url: String
) : AbstractR2dbcConfiguration() {
    @Bean
    override fun connectionFactory(): ConnectionFactory {
        return ConnectionFactoryWrapper(
            ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                    .option(SpannerConnectionFactoryProvider.URL, url)
                    .option(ConnectionFactoryOptions.DRIVER, "cloudspanner")
                    .build(),
            )
        )
    }

    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): R2dbcTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }
}

