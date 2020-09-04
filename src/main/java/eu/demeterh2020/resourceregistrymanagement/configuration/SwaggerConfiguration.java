package eu.demeterh2020.resourceregistrymanagement.configuration;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Autowired
    TypeResolver typeResolver;

    @Bean
    @ConditionalOnMissingBean
    public Docket apiSwagger2Description() {

        return new Docket(DocumentationType.SWAGGER_2)
                .alternateTypeRules(
                        AlternateTypeRules.newRule(
                        typeResolver.resolve(List.class, LocalDateTime.class),
                        typeResolver.resolve(List.class, Date.class), Ordered.HIGHEST_PRECEDENCE),
                        AlternateTypeRules.newRule(typeResolver.resolve(Map.class, String.class, LocalDateTime.class),
                                typeResolver.resolve(Map.class, String.class, Date.class), Ordered.HIGHEST_PRECEDENCE))
                .select()
                .apis(RequestHandlerSelectors.basePackage("eu.demeterh2020.resourceregistrymanagement"))
                .paths(not(or(regex("/error"), regex("/actuator.*"))))
                .paths(not(pathsToIgnore()))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Resource Registry Management")
                .description("Core DEH service Resource Registry Management")
                .contact(new Contact("Marko Stojanovic", "https://github.com/marest94",
                        "marko.stojanovic@eng.it"))
                .build();
    }

    public Predicate<String> pathsToIgnore() {
        return PathSelectors.none();
    }
}
