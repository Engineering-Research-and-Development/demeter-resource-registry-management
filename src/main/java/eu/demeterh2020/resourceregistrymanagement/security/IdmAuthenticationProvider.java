package eu.demeterh2020.resourceregistrymanagement.security;

import eu.demeterh2020.resourceregistrymanagement.security.dto.RrmToken;
import eu.demeterh2020.resourceregistrymanagement.security.dto.IdmToken;
import eu.demeterh2020.resourceregistrymanagement.security.dto.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class IdmAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(IdmAuthenticationProvider.class);

    @Autowired
    RestTemplate restTemplate;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        logger.debug("Authenticating authenticationToken");
        RrmToken auth = (RrmToken) authentication;
        String accessToken = auth.getToken();

        UserInfo userInfo = getUserInfoFromToken(accessToken);

        if (userInfo != null) {
            logger.debug("x-subject-token valid");
            return new RrmToken(accessToken, userInfo, auth.getAuthorities());
        }
        logger.error("Bad x-subject-token ");

        return null;
    }


    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == RrmToken.class;
    }

    /* Method for validation x-subject-token on IDM
     */
    private UserInfo getUserInfoFromToken(String token) {

        logger.debug("method getUserInfoFromToken() called.");

        String idmUrl = "https://acs.bse.h2020-demeter-cloud.eu:5443/v1/auth/tokens";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token);
        headers.set("X-Subject-Token", token);

        try {
            ResponseEntity<IdmToken> response = restTemplate.exchange(idmUrl, HttpMethod.GET, new HttpEntity<>(headers), IdmToken.class);
            return response.getBody().getUser();

        } catch (HttpClientErrorException ex) {
            logger.error("IDM: " + ex.getStatusCode() + ", " + ex.getResponseBodyAsString());
        }
        return null;
    }
}
