package eu.demeterh2020.resourceregistrymanagement.configuration;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import eu.demeterh2020.resourceregistrymanagement.filter.LoggingFilter;
import eu.demeterh2020.resourceregistrymanagement.logging.LoggingInterceptor;
import eu.demeterh2020.resourceregistrymanagement.util.listener.DehResourceMongoEventListener;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

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
                .addServersItem(new Server().url("https://acs.bse.h2020-demeter-cloud.eu:1029"))
                .info(new Info().title("Resource Registry Management API").version(appVersion));
    }

    @Bean
    public MappingMongoConverter mongoConverter(MongoDatabaseFactory mongoFactory, MongoMappingContext mongoMappingContext) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoFactory);
        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        mongoConverter.setMapKeyDotReplacement("-DOT");
        return mongoConverter;
    }

    @Bean
    public GridFSBucket getGridFSBuckets(MongoDatabaseFactory mongoFactory) {
        MongoDatabase db = mongoFactory.getMongoDatabase();
        return GridFSBuckets.create(db);
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
