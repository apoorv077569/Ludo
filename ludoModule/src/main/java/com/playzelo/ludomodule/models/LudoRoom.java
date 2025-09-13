package com.playzelo.ludomodule.models;

import java.util.List;

public class LudoRoom {
    private String roomId;
    private int type;
    private List<Player> players;
    private String status;
    public String getRoomId() { return roomId; }
    public int getType() { return type; }
    public List<Player> getPlayers() { return players; }
    public String getStatus() { return status; }
}