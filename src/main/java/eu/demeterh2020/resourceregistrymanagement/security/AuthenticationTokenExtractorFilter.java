package eu.demeterh2020.resourceregistrymanagement.security;

import eu.demeterh2020.resourceregistrymanagement.exception.BadRequestException;
import eu.demeterh2020.resourceregistrymanagement.security.dto.RrmToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AuthenticationTokenExtractorFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationTokenExtractorFilter.class);

    @Qualifier("handlerExceptionResolver")
    @Autowired
    private HandlerExceptionResolver resolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        logger.debug("Filtering request");

        Authentication authentication = getAuthentication(request);

        if (authentication == null) {
            logger.warn("x-subject-token not present in header");
            if (request.getRequestURI().contains("swagger") || request.getRequestURI().contains("api-docs")) {
                logger.warn("Swagger call");
                filterChain.doFilter(request, response);
            } else {
                resolver.resolveException(request, response, null, new BadRequestException("x-subject-token is missing in header"));
            }
        } else {
            logger.info("x-subject-token present in header - setting authentication");
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        }
    }

    /* Method for extracting x-subject token from header
     */
    private Authentication getAuthentication(HttpServletRequest request) {

        String accessToken = request.getHeader("x-subject-token");

        if (accessToken != null) {
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));

            return new RrmToken(accessToken, authorities);
        }
        return null;
    }
}
