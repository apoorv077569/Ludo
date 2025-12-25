package com.playzelo.ludomodule.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MoveTokenResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("moved")
    private boolean moved;

    @SerializedName("movedTokenIndex")
    private int movedTokenIndex;

    @SerializedName("newPos")
    private int newPos;

    @SerializedName("captured")
    private Object captured; // Agar captured player ka data aata hai to isko ek model me map kar sakte ho

    @SerializedName("currentPlayer")
    private CurrentPlayer currentPlayer;

    @SerializedName("nextPlayer")
    private NextPlayer nextPlayer;

    @SerializedName("dice")
    private int dice;

    @SerializedName("score")
    private int score;

    @SerializedName("gameOver")
    private boolean gameOver;

    @SerializedName("winner")
    private Object winner; // Agar winner ka data detailed hoga to model bana lena

    // ✅ Getters
    public boolean isSuccess() {
        return success;
    }

    public boolean isMoved() {
        return moved;
    }

    public int getMovedTokenIndex() {
        return movedTokenIndex;
    }

    public int getNewPos() {
        return newPos;
    }

    public Object getCaptured() {
        return captured;
    }

    public CurrentPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    public NextPlayer getNextPlayer() {
        return nextPlayer;
    }
    public int score(){
        return score;
    }

    public int getDice() {
        return dice;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Object getWinner() {
        return winner;
    }

    // ===================== INNER MODELS =====================

    public static class CurrentPlayer {
        @SerializedName("userId")
        private String userId;

        @SerializedName("color")
        private String color;

        @SerializedName("tokens")
        private List<Integer> tokens;

        @SerializedName("score")
        private int score;

        public String getUserId() {
            return userId;
        }

        public String getColor() {
            return color;
        }

        public List<Integer> getTokens() {
            return tokens;
        }

        public int getScore() {
            return score;
        }
    }

    public static class NextPlayer {
        @SerializedName("userId")
        private String userId;

        @SerializedName("color")
        private String color;
        @SerializedName("score")
        private int score;

        public String getUserId() {
            return userId;
        }

        public String getColor() {
            return color;
        }
        public int getScore(){
            return score;
        }
    }
}
