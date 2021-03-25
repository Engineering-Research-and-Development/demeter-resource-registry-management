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

        //Permit those requests
        http.authorizeRequests().antMatchers("/swagger").permitAll();
        http.authorizeRequests().antMatchers("/swagger-ui/**").permitAll();
        http.authorizeRequests().antMatchers("/swagger-ui.html").permitAll();
        http.authorizeRequests().antMatchers("/swagger-resources/**").permitAll();
        http.authorizeRequests().antMatchers("/webjars/springfox-swagger-ui/**").permitAll();
        http.authorizeRequests().antMatchers("/api-docs/**").permitAll();
        http.authorizeRequests().antMatchers("/v2/api-docs").permitAll();
        http.authorizeRequests().antMatchers("/api-doc.html").permitAll();
        http.authorizeRequests().antMatchers("/api-doc.pdf").permitAll();

        //Filter requests
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
