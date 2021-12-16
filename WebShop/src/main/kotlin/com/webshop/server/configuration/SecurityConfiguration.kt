package com.webshop.server.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class SecurityConfiguration: WebSecurityConfigurerAdapter() {

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
            .withUser("user").password(passwordEncoder().encode("user"))
            .authorities("user")
            .roles("user")
            .and()
            .withUser("admin").password(passwordEncoder().encode("admin"))
            .authorities("admin")
            .roles("admin")

    }
    override fun configure(http: HttpSecurity) {
        http.csrf().disable().authorizeRequests()
            .antMatchers("/").permitAll()
            .antMatchers("/goods/admin/**").hasAuthority("admin")
            .antMatchers("/goods/admin/**").hasRole("admin")
            .antMatchers("/goods/**").hasAnyAuthority("admin", "user")
            .antMatchers("/goods/**").hasAnyRole("admin", "user")
            .anyRequest().authenticated()
            .and()
            .httpBasic()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
