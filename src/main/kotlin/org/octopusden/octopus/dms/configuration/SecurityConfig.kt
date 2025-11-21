package org.octopusden.octopus.dms.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
@EnableWebFluxSecurity
open class SecurityConfig(
    @Value("\${auth-server.logout-url}")
    private val logoutUrl: String
) {

    @Bean
    open fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(
                        "/static/**",
                        "/bundle.js",
                        "/main.css",
                        "/favicon.ico",
                        "/logout",
                        "/actuator/**",
                    ).permitAll()
                    .pathMatchers(
                        "/",
                        "/index.html",
                        "/auth/**",
                        "/rest/api/**",
                    ).authenticated()
                    .anyExchange().authenticated()
            }
            .oauth2Login(Customizer.withDefaults())
            .logout { logout ->
                logout
                    .logoutSuccessHandler { webFilterExchange, _ ->
                        val response = webFilterExchange.exchange.response
                        response.statusCode = HttpStatus.FOUND
                        response.headers.location = URI.create(logoutUrl)
                        Mono.empty()
                    }
            }
            .csrf { it.disable() }
        return http.build()
    }
}
