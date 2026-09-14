package com.playzelo.ludo.models;

import java.io.Serializable;
import java.util.List;

public class RoomDto implements Serializable {
    private String roomId;
    private int type;
    private List<PlayerDto> players;
    private String status;

    public String getRoomId() {
        return roomId;
    }

    public int getType() {
        return type;
    }

    public List<PlayerDto> getPlayers() {
        return players;
    }

    public String getStatus() {
        return status;
    }
}
