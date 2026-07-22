package com.campus.eventhub.web.dto;

import java.util.List;

public class UserResponseDto {
    private String email;
    private List<String> roles;

    public UserResponseDto() {}

    public UserResponseDto(String email, List<String> roles) {
        this.email = email;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}