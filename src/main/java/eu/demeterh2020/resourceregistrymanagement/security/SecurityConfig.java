package eu.demeterh2020.resourceregistrymanagement.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .addFilterAfter(accessTokenExtractorFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests().anyRequest().authenticated();


    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customAuthenticationProvider());
    }

    @Bean
    public IdmAuthenticationProvider customAuthenticationProvider() {
        return new IdmAuthenticationProvider();
    }

    @Bean
    public AuthenticationTokenExtractorFilter accessTokenExtractorFilter() {
        return new AuthenticationTokenExtractorFilter();
    }
}
