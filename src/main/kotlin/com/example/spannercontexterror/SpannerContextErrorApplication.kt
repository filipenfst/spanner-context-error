package com.example.spannercontexterror

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpannerContextErrorApplication

fun main(args: Array<String>) {
    runApplication<SpannerContextErrorApplication>(*args)
}
