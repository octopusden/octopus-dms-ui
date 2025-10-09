package org.octopusden.octopus.dms.config

import org.octopusden.cloud.commons.security.client.AuthServerClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@Import(AuthServerClient::class)
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
                    ).permitAll()
                    .pathMatchers(
                        "/",
                        "/index.html",
                        "/auth/**",
                        "/rest/api/**",
                        "/actuator/**",
                    ).authenticated()
                    .anyExchange().authenticated()
            }
            .oauth2Login(Customizer.withDefaults())
            .logout {
                it.logoutSuccessHandler { exchange, _ ->
                    exchange.exchange.response.statusCode = HttpStatus.FOUND
                    exchange.exchange.response.headers.add(HttpHeaders.LOCATION, logoutUrl)
                    Mono.empty()
                }
            }
            .csrf { it.disable() }
        return http.build()
    }
}
