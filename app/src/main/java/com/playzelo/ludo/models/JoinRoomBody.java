package com.playzelo.ludo.models;

public class JoinRoomBody {
    private String userId;
    private String username;

    public JoinRoomBody(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}
