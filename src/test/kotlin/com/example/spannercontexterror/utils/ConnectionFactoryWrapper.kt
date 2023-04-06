package com.example.spannercontexterror.utils

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class ConnectionFactoryWrapper(private val connectionFactory: ConnectionFactory) : ConnectionFactory {
    override fun create(): Publisher<out Connection> {
        return Mono.deferContextual { contextView ->
            connectionFactory.create().toMono().map {
                ConnectionWrapper(connection = it)
            }.contextWrite(contextView)
        }
    }

    override fun getMetadata(): ConnectionFactoryMetadata {
        return connectionFactory.metadata
    }
}