package com.example.spannercontexterror.utils

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional
@Repository
class TransactionProvider {
    suspend fun withTransaction(action: suspend () -> Unit) {
        action()
    }
}