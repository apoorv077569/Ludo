package com.playzelo.ludo.models;

public class AutoMatchRequest {
    private double entryFee;
    private double winPrize;
    private String type;

    public AutoMatchRequest(double entryFee, double winPrize, String type) {
        this.entryFee = entryFee;
        this.winPrize = winPrize;
        this.type = type;
    }

    public double getEntryFee() {
        return entryFee;
    }

    public void setEntryFee(int entryFee) {
        this.entryFee = entryFee;
    }

    public double getWinPrize() {
        return winPrize;
    }

    public void setWinPrize(int winPrize) {
        this.winPrize = winPrize;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}