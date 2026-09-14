package com.playzelo.ludo.models;

public class CreateRoomRequest {
    private int type;
    private String userId;
    private String username;

    public CreateRoomRequest(int type, String userId, String username) {
        this.type = type;
        this.userId = userId;
        this.username = username;
    }
}
