package com.example.spannercontexterror.utils

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.ReadOnlyProperty
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import java.io.Serializable

@Repository
interface ContextTestRepository : CoroutineCrudRepository<ContextTestEntity, String>

@Table("context_test_table")
data class ContextTestEntity(
    @Id
    @Column("id")
    private val id: String,
    @Column("value")
    val value: String
) : Serializable, Persistable<String> {

    @Transient
    @ReadOnlyProperty
    private var isUpdate = false
    override fun getId(): String = id

    override fun isNew(): Boolean = !isUpdate

    fun asUpdate() = this.apply {
        isUpdate = true
    }
}

data class TestContext(
    val value: String,
)
