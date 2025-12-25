package com.playzelo.ludo.models;

import com.google.gson.annotations.SerializedName;

public class MoveTokenRequest {

    @SerializedName("tokenIndex")
    private int tokenIndex;

    public MoveTokenRequest(int tokenIndex) {
        this.tokenIndex = tokenIndex;
    }

    public int getTokenIndex() {
        return tokenIndex;
    }

    public void setTokenIndex(int tokenIndex) {
        this.tokenIndex = tokenIndex;
    }
}
