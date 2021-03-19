package eu.demeterh2020.resourceregistrymanagement.security.dto;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class RrmToken extends AbstractAuthenticationToken {

    private String token;
    private UserInfo userInfo;

    public RrmToken(UserInfo userInfo, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userInfo = userInfo;
        this.setAuthenticated(true);
    }

    public RrmToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    public RrmToken(String token, UserInfo userInfo, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public RrmToken(String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
