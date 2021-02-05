package eu.demeterh2020.resourceregistrymanagement.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class LoggingFilter extends OncePerRequestFilter {

    private final static Logger log = LoggerFactory.getLogger(eu.demeterh2020.resourceregistrymanagement.filter.LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        MDC.put("requestId", UUID.randomUUID().toString());

        logRequestInfo(request);

        filterChain.doFilter(request, response);
    }

    private void logRequestInfo(HttpServletRequest request) {

        StringBuilder parameterMap = new StringBuilder();

        if (request.getParameterMap() != null) {

            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {

                parameterMap.append(entry.getKey() + ":" + Arrays.toString(entry.getValue()));
            }
        }

//        String authorization = request.getHeader("Authorization");

        log.info("RequestURI: {} Method: {} ParameterMap: {}", request.getRequestURI(), request.getMethod(), parameterMap.toString());
    }
}