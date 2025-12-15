package org.octopusden.octopus.dms.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
open class SecurityConfig(
    @Value("\${auth-server.logout-url}")
    private val logoutUrl: String
) {

    @Bean
    open fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.authorizeExchange { exchanges ->
            exchanges.pathMatchers("/actuator/**").permitAll()
            exchanges.anyExchange().authenticated()
        }.oauth2Login(
            Customizer.withDefaults()
        ).oidcLogout{
            it.backChannel(Customizer.withDefaults())
        }.logout { logout ->
            logout.logoutSuccessHandler { exchange, _ ->
                exchange.exchange.response.apply {
                    statusCode = HttpStatus.FOUND
                    headers.add(HttpHeaders.LOCATION, logoutUrl)
                }
                exchange.exchange.session.flatMap { it.invalidate() }
            }
        }.csrf { it.disable() }
        return http.build()
    }
}
