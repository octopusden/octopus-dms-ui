package org.octopusden.octopus.dms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
open class DmsUiApplication

fun main(args: Array<String>) {
    runApplication<DmsUiApplication>(*args)
}
