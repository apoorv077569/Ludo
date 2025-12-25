package com.playzelo.ludomodule.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Player {
    @SerializedName("userId")
    private String userId;

    @SerializedName("color")
    private String color;

    @SerializedName("tokens")
    private List<Integer> tokens;

    public String getUserId() {
        return userId;
    }

    public String getColor() {
        return color;
    }

    public List<Integer> getTokens() {
        return tokens;
    }
}
