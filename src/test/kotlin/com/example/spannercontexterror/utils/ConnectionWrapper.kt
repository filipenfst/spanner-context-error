package com.example.spannercontexterror.utils

import io.r2dbc.spi.Batch
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionMetadata
import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.Statement
import io.r2dbc.spi.TransactionDefinition
import io.r2dbc.spi.ValidationDepth
import org.reactivestreams.Publisher
import java.time.Duration

data class ConnectionWrapper(
    private val connection: Connection
) : Connection {
    override fun close(): Publisher<Void> {
        return connection.close()
    }

    override fun beginTransaction(): Publisher<Void> {
        return connection.beginTransaction()
    }

    override fun beginTransaction(definition: TransactionDefinition): Publisher<Void> {
        return beginTransaction()
    }

    override fun commitTransaction(): Publisher<Void> {
        return connection.commitTransaction()
    }

    override fun createBatch(): Batch {
        return connection.createBatch()
    }

    override fun createSavepoint(name: String): Publisher<Void> {
        return connection.createSavepoint(name)
    }

    override fun createStatement(sql: String): Statement {
        return connection.createStatement(sql)
    }

    override fun isAutoCommit(): Boolean {
        return connection.isAutoCommit
    }

    override fun getMetadata(): ConnectionMetadata {
        return connection.metadata
    }

    override fun getTransactionIsolationLevel(): IsolationLevel {
        return connection.transactionIsolationLevel
    }

    override fun releaseSavepoint(name: String): Publisher<Void> {
        return connection.releaseSavepoint(name)
    }

    override fun rollbackTransaction(): Publisher<Void> {
        return connection.rollbackTransaction()
    }

    override fun rollbackTransactionToSavepoint(name: String): Publisher<Void> {
        return connection.rollbackTransactionToSavepoint(name)
    }

    override fun setAutoCommit(autoCommit: Boolean): Publisher<Void> {
        return connection.setAutoCommit(autoCommit)
    }

    override fun setLockWaitTimeout(timeout: Duration): Publisher<Void> {
        return connection.setLockWaitTimeout(timeout)
    }

    override fun setStatementTimeout(timeout: Duration): Publisher<Void> {
        return connection.setStatementTimeout(timeout)
    }

    override fun setTransactionIsolationLevel(isolationLevel: IsolationLevel): Publisher<Void> {
        return connection.setTransactionIsolationLevel(isolationLevel)
    }

    override fun validate(depth: ValidationDepth): Publisher<Boolean> {
        return connection.validate(depth)
    }
}
