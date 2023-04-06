package com.example.spannercontexterror


import com.example.spannercontexterror.utils.ContextTestEntity
import com.example.spannercontexterror.utils.ContextTestRepository
import com.example.spannercontexterror.utils.TransactionProvider
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Hooks
import java.util.UUID


class ContextPropagationTest(
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

    private val initialDBEntities = (1 until 10).map {
        ContextTestEntity(
            id = "id-$it",
            value = "value-$it",
        )
    }

    @BeforeEach
    fun setup(): Unit = runBlocking {
        contextTestCoroutineRepository.deleteAll(initialDBEntities)
        contextTestCoroutineRepository.saveAll(initialDBEntities).collect()
    }

    @Test
    fun contextLostTest(): Unit = runBlocking {
        Assertions.assertEquals(contextTestCoroutineRepository.findAll().count(), 10)
    }

    @Test
    fun transactionFailTest(): Unit = runBlocking {
        Hooks.disableContextLossTracking()

        val entities = (0 until 5).map { UUID.randomUUID() }.map {
            ContextTestEntity(
                id = "id-$it",
                value = "value-$it",
            )
        }.asFlow()

        transactionProvider.withTransaction {
            contextTestCoroutineRepository.saveAll(entities).count().also {
                println("Final Save $it")
            }
        }
    }
}



