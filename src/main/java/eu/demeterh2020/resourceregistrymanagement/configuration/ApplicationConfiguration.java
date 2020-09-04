package eu.demeterh2020.resourceregistrymanagement.configuration;

import eu.demeterh2020.resourceregistrymanagement.filter.LoggingFilter;
import eu.demeterh2020.resourceregistrymanagement.logging.LoggingInterceptor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ApplicationConfiguration {


    @Bean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }

    @Bean
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilterRegistration() {

        FilterRegistrationBean<LoggingFilter> filterRegistrationBean = new FilterRegistrationBean<LoggingFilter>();
        filterRegistrationBean.setFilter(loggingFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return filterRegistrationBean;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
