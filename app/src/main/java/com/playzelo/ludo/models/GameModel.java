package com.playzelo.ludo.models;

public class GameModel {
    private String title;
    private String prizePool;
    private String entryFee;
    private String joinedPlayers;

    public GameModel(String title, String prizePool, String entryFee, String joinedPlayers) {
        this.title = title;
        this.prizePool = prizePool;
        this.entryFee = entryFee;
        this.joinedPlayers = joinedPlayers;
    }

    public String getTitle() {
        return title;
    }

    public String getPrizePool() {
        return prizePool;
    }

    public String getEntryFee() {
        return entryFee;
    }

    public String getJoinedPlayers() {
        return joinedPlayers;
    }
}
