package eu.demeterh2020.resourceregistrymanagement.configuration;

import eu.demeterh2020.resourceregistrymanagement.filter.LoggingFilter;
import eu.demeterh2020.resourceregistrymanagement.logging.LoggingInterceptor;
import eu.demeterh2020.resourceregistrymanagement.util.listener.DehResourceMongoEventListener;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
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


    @Bean
    public DehResourceMongoEventListener dehResourceMongoEventListenerMongoEventListener() {
        return new DehResourceMongoEventListener();
    }

    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("Resource Registry Management API").version(appVersion));
    }
}
