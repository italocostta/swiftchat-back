package com.pd.swiftchat.dto;

public class LoginRequest {
    private String cpfCnpj;
    private String password;

    // Getters e setters
    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
