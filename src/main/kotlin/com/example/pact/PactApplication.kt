package com.example.pact

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PactApplication

fun main(args: Array<String>) {
	runApplication<PactApplication>(*args)
}
