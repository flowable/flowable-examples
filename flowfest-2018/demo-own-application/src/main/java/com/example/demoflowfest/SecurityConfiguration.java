package com.example.demoflowfest;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * @author Filip Hrisafov
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .requestMatchers(EndpointRequest.to(InfoEndpoint.class, HealthEndpoint.class)).permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
            .antMatchers("/*-api/**").hasRole("REST")
            .anyRequest().authenticated()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and()
            .csrf().disable()
            .httpBasic();
    }

    //@Bean
    public UserDetailsService customUserDetailsService() {
        UserDetails user1 = User.withUsername("custom-actuator")
            .password("{noop}test")
            .roles("ACTUATOR")
            .build();

        UserDetails user2 = User.withUsername("custom-rest")
            .password("{noop}test")
            .roles("REST")
            .build();
        return new InMemoryUserDetailsManager(user1, user2);
    }
}
