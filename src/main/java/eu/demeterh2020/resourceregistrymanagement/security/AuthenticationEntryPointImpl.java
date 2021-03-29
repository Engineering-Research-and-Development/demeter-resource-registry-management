package eu.demeterh2020.resourceregistrymanagement.security;

import eu.demeterh2020.resourceregistrymanagement.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * No username or/and password provided or wrong username or/and password (401)
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationEntryPointImpl.class);

    @Qualifier("handlerExceptionResolver")
    @Autowired
    private HandlerExceptionResolver resolver;

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                         AuthenticationException authenticationException) throws IOException, ServletException {

        log.warn("Access denied for USER: {}; URL: {}. ",
                httpServletRequest.getUserPrincipal() == null ? "N/A" : httpServletRequest.getUserPrincipal().getName(),
                httpServletRequest.getRequestURL() +
                        (httpServletRequest.getQueryString() == null ? "" : "?" + httpServletRequest.getQueryString()));

        resolver.resolveException(httpServletRequest, httpServletResponse, null, new UnauthorizedException("Auth Token invalid or expired"));
    }
}