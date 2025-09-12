package org.example.spacecore

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<SpaceCoreApplication>().with(TestcontainersConfiguration::class).run(*args)
}
