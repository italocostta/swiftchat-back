package com.pd.swiftchat.dto;

public class JwtResponse {
    private String token;
    private String userType;
    private String userName;

    public JwtResponse(String token, String userType, String userName) {
        this.token = token;
        this.userType = userType;
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
