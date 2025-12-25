package com.playzelo.ludo.models;

import com.google.gson.JsonElement;

import java.util.List;

public class LudoRoomResponse {

    private boolean success;
    private String message;
    private String roomId;
    private String status;
    private List<Player> players;
    private double entryFee;
    private double winPrize;
    private int dice;

    // 🔥 new fields as JsonElement (string/object dono handle karega)
    private JsonElement nextPlayer;
    private JsonElement currentPlayer;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getStatus() {
        return status;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public double getEntryFee() {
        return entryFee;
    }

    public double getWinPrize() {
        return winPrize;
    }

    public void setEntryFee(double entryFee) {
        this.entryFee = entryFee;
    }

    public void setWinPrize(double winPrize) {
        this.winPrize = winPrize;
    }

    public int getDiceValue() {
        return dice;
    }

    public void setDice(int dice) {
        this.dice = dice;
    }

    // 👇 getters for flexible handling
    public JsonElement getNextPlayer() {
        return nextPlayer;
    }

    public JsonElement getCurrentPlayer() {
        return currentPlayer;
    }
}
