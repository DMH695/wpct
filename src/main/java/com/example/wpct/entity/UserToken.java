package com.example.wpct.entity;

import lombok.Data;
import org.apache.shiro.authc.UsernamePasswordToken;

@Data
public class UserToken extends UsernamePasswordToken {
    private LoginType loginType;
    private String username;

    public UserToken(String username, String password, boolean rememberMe,LoginType loginType) {
        super(username, password, rememberMe);
        this.username = username;
        this.loginType = loginType;
    }


    public LoginType getLoginType() {
        return loginType;
    }

    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }
}
