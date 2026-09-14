package com.playzelo.ludo.models;

import java.io.Serializable;

public class PlayerDto implements Serializable {
    private String userId;
    private String username;

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
