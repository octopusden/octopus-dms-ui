package org.octopusden.octopus.dms.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler

@Configuration
@EnableWebFluxSecurity
open class SecurityConfig(
    private val clientRegistrationRepository: ReactiveClientRegistrationRepository
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
                        "/actuator/**",
                        "/logout",
                        "/logout/connect/back-channel/**",
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
                logout.logoutSuccessHandler(oidcLogoutSuccessHandler())
            }
            .oidcLogout { oidcLogout ->
                oidcLogout.backChannel(Customizer.withDefaults())
            }
            .csrf { it.disable() }
        return http.build()
    }

    private fun oidcLogoutSuccessHandler(): ServerLogoutSuccessHandler {
        val handler = OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository)
        handler.setPostLogoutRedirectUri("{baseUrl}")
        return handler
    }
}
