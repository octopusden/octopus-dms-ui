package org.octopusden.octopus.dms.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
open class WebConfig : WebFluxConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler(
            "/static/**",
            "/bundle.js",
            "/main.css",
            "/favicon.ico",
            "/index.html",
        ).addResourceLocations("classpath:/static/")
    }
}
