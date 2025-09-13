package com.playzelo.ludomodule.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.playzelo.ludomodule.R;
import com.playzelo.ludomodule.apiservice.LudoApiHelper;
import com.playzelo.ludomodule.databinding.ActivityLudoGameRoomBinding;
import com.playzelo.ludomodule.databinding.LudoLayoutBinding;
import com.playzelo.ludomodule.models.LudoRoomResponse;
import com.playzelo.ludomodule.models.MoveTokenRequest;
import com.playzelo.ludomodule.models.MoveTokenResponse;
import com.playzelo.ludomodule.utils.SocketManager;

import org.jetbrains.annotations.Contract;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameRoomActivity extends AppCompatActivity {

    // Constants
    private static final int DICE_ANIMATION_DURATION = 300;
    private static final int TOKEN_COUNT = 4;
    private static final int STARTING_DICE_VALUE = 6;
    private static final String LOG_TAG = "LudoGame";

    // Multi-token support
    private final Map<View, List<Token>> cellTokensMap = new HashMap<>();

    private String userId, authToken, username;
    private double entry_fee, prize_pool;
    private Animation pulseAnimation;
    private ActivityLudoGameRoomBinding binding;
    private LudoLayoutBinding ludoBinding;

    public enum PlayerColor {
        YELLOW("yellow", 0), GREEN("green", 1), RED("red", 2), BLUE("blue", 3);

        private final int index;
        private final String colorName;

        PlayerColor(String colorName, int index) {
            this.colorName = colorName;
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public String getColorName() {
            return colorName;
        }

        public static PlayerColor fromString(String colorName) {
            for (PlayerColor pc : PlayerColor.values()) {
                if (pc.colorName.equalsIgnoreCase(colorName)) {
                    return pc;
                }
            }
            throw new IllegalArgumentException("Unknown player color: " + colorName);
        }
    }

    // Game state
    private int currentDiceValue = 0;
    private MediaPlayer diceSound;
    private AnimatorSet diceAnimator;
    private String roomId;
    private ImageView[][] tokenViews;

    // UI elements
    private ImageView[] diceViews = new ImageView[4];
    private final Player[] players = new Player[4];

    // Dice drawable resources
    private static final int[] DICE_DRAWABLES = {
            R.drawable.dice_1, R.drawable.dice_2, R.drawable.dice_3,
            R.drawable.dice_4, R.drawable.dice_5, R.drawable.dice_6
    };
    private final TextView[] playerInfoTextViews = new TextView[4];


    // Token class
    static class Token {

        ImageView view;
        int position = -1; // -1 = not on board yet
        View currentCell = null;

// Track current cell

        Token(ImageView view) {
            this.view = view;
        }
    }

    // Player class to encapsulate player data
    static class Player {
        String userId;
        String username;
        Token[] tokens = new Token[TOKEN_COUNT];
        View[] path;
        PlayerColor color;

        Player(PlayerColor color, View[] path) {
            this.color = color;
            this.path = path;
        }

        // ✅ Add method to set player details
        void setPlayerDetails(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        PlayerColor getColor() {
            return this.color;
        }

        void setTokensEnabled(boolean enabled) {
            for (Token token : tokens) {
                if (token != null && token.view != null) {
                    token.view.setEnabled(enabled);
                }
            }
        }

        void clearTokenHighlights() {
            for (Token token : tokens) {
                if (token != null && token.view != null) {
                    token.view.setBackground(null);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLudoGameRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ludoBinding = LudoLayoutBinding.bind(binding.ludoLayout.getRoot());

        // Ensure socket is initialized and connected for realtime updates in game room
        if (SocketManager.getSocket() == null) {
            SocketManager.initSocket("https://playzelo-nrwt.onrender.com");
        }

        if (SocketManager.getSocket() != null && !SocketManager.getSocket().connected()) {
            SocketManager.getSocket().connect();
        }

        initializeSound();
        initializeDice();

        // ✅ Get player data from Intent
        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");
        authToken = getIntent().getStringExtra("auth_token");
        roomId = getIntent().getStringExtra("roomId");
        entry_fee = getIntent().getDoubleExtra("entryFee", 0);
        prize_pool = getIntent().getDoubleExtra("winPrize", 0);

        List<String> playerIds = getIntent().getStringArrayListExtra("playerIds");
        List<String> playerColors = getIntent().getStringArrayListExtra("playerColors");

        initializePlayers(playerIds, playerColors);

        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_scale);

        Log.d(LOG_TAG, "Username: " + username + " userId: " + userId + " Token: " + authToken + " roomId: " + roomId);
        initializePlayerInfoTextViews();
        diceViews = new ImageView[]{binding.leftDice, binding.topDiceLeft, binding.topDiceRight, binding.rightDice};
        binding.backButton.setOnClickListener(v -> leaveRoom());
        setUpSocketListener();
        binding.prizePool.setText(String.valueOf(prize_pool));
        CountDownTimer countDownTimer = getCountDownTimer();
        countDownTimer.start();

    }

    private void setUpSocketListener() {
        // socket listeners
        SocketManager.on("roomUpdate", args -> {
            JSONObject room = (JSONObject) args[0];
            Log.d("GameRoomActivity", "roomUpdate received: " + room.toString());
        });

        SocketManager.on("roomClosed", args -> runOnUiThread(() -> {
            Toast.makeText(this, "Room closed", Toast.LENGTH_SHORT).show();
            finish();
        }));

        SocketManager.on("leftRoom", args -> runOnUiThread(() -> {
            Toast.makeText(GameRoomActivity.this, "You left the room", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "You left the room.");
            finish();
        }));
    }

    private void initializeSound() {
        diceSound = MediaPlayer.create(this, R.raw.dice_roll);
    }

    private void initializeDice() {
        // Map dice views to array for easier management
        diceViews[PlayerColor.YELLOW.getIndex()] = binding.leftDice;
        diceViews[PlayerColor.GREEN.getIndex()] = binding.topDiceLeft;
        diceViews[PlayerColor.RED.getIndex()] = binding.topDiceRight;
        diceViews[PlayerColor.BLUE.getIndex()] = binding.rightDice;

        // Set click listeners for all dice

        for (PlayerColor color : PlayerColor.values()) {
            final PlayerColor playerColor = color;
            diceViews[color.getIndex()].setOnClickListener(v -> rollDice(playerColor));
        }

    }

    private void initializePlayers(List<String> playerIds, List<String> playerColors) {
        View[][] paths = {
                getYellowPath(), getGreenPath(), getRedPath(), getBluePath()
        };

        ImageView[][] tokenViews = {
                {ludoBinding.yellowToken1, ludoBinding.yellowToken2, ludoBinding.yellowToken3, ludoBinding.yellowToken4},
                {ludoBinding.greenToken1, ludoBinding.greenToken2, ludoBinding.greenToken3, ludoBinding.greenToken4},
                {ludoBinding.redToken1, ludoBinding.redToken2, ludoBinding.redToken3, ludoBinding.redToken4},
                {ludoBinding.blueToken1, ludoBinding.blueToken2, ludoBinding.blueToken3, ludoBinding.blueToken4}
        };

        if (playerIds == null || playerColors == null || playerIds.size() != playerColors.size()) {
            Log.e(LOG_TAG, "Invalid player data received from Intent");
            return;
        }

        // Loop through the received player data
        for (int i = 0; i < playerIds.size(); i++) {
            String id = playerIds.get(i);
            String colorString = playerColors.get(i);

            try {
                PlayerColor color = PlayerColor.fromString(colorString);
                int index = color.getIndex();

                // Set the player's username based on whether it's the current user or an opponent
                String playerName = id; // Default to ID for opponents
                if (id.equals(userId)) {
                    playerName = username; // Use the actual username for the current user
                }

                players[index] = new Player(color, paths[index]);
                players[index].setPlayerDetails(id, playerName);

                // LOGCAT me print karein kaun sa player kaun sa color hai
                if (id.equals(userId)) {
                    Log.d(LOG_TAG, "aapka ID: " + id + " aur aapka color: " + color.getColorName());
                } else {
                    Log.d(LOG_TAG, "Opponent ID: " + id + " aur opponent ka color: " + color.getColorName());
                }

                // Player ka name set karein TextView par
                if (playerInfoTextViews[index] != null) {
                    playerInfoTextViews[index].setText(playerName);
                }

                for (int j = 0; j < TOKEN_COUNT; j++) {
                    players[index].tokens[j] = new Token(tokenViews[index][j]);
                    final Player player = players[index];
                    final Token token = players[index].tokens[j];

                    token.view.setOnClickListener(v -> onTokenClicked(player, token));
                    token.view.setEnabled(false);
                }
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "Error: Unknown player color in Intent data: " + colorString);
            }
        }
    }
    @NonNull
    @Contract(" -> new")
    private CountDownTimer getCountDownTimer() {
        long totalTime = 5 * 60 * 1000;
        return new CountDownTimer(totalTime, 1000) {
            @Override
            public void onTick(long l) {
                int minutes = (int) (l / 1000) / 60;
                int second = (int) (l / 1000) % 60;

                @SuppressLint("DefaultLocale")
                String timeLeft = String.format("%02d:%02d", minutes, second);
                binding.timerText.setText(timeLeft);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                binding.timerText.setText("00:00");
                GameRoomActivity.this.runOnUiThread(() -> leaveRoom());
            }
        };
    }
    private void initializePlayerInfoTextViews() {
        playerInfoTextViews[PlayerColor.YELLOW.getIndex()] = binding.bottomLeftPlayerName;
        playerInfoTextViews[PlayerColor.GREEN.getIndex()] = binding.leftPlayerName;
        playerInfoTextViews[PlayerColor.RED.getIndex()] = binding.rightPlayerName;
        playerInfoTextViews[PlayerColor.BLUE.getIndex()] = binding.bottomRightPlayerName;
    }

    private void rollDice(@NonNull PlayerColor playerColor) {
        playDiceSound();

        final ImageView diceView = diceViews[playerColor.getIndex()];

        if (diceAnimator != null && diceAnimator.isRunning()) {
            diceAnimator.cancel();
        }

        diceView.setClickable(false);

        ObjectAnimator bounceUp = ObjectAnimator.ofFloat(diceView, "translationY", -100f);
        bounceUp.setDuration(150);

        ObjectAnimator bounceDown = ObjectAnimator.ofFloat(diceView, "translationY", 0f);
        bounceDown.setDuration(300);
        bounceDown.setInterpolator(new BounceInterpolator());

        ObjectAnimator rotate = ObjectAnimator.ofFloat(diceView, "rotation", 0f, 360f);
        rotate.setDuration(800);

        diceAnimator = new AnimatorSet();
        diceAnimator.playSequentially(bounceUp, bounceDown);
        diceAnimator.playTogether(rotate);

        diceAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (authToken == null || roomId == null) {
                    Log.e(LOG_TAG, "Auth Token or Room ID is null. Cannot roll dice.");
                    diceView.setClickable(true);
                    return;
                }

                LudoApiHelper apiHelper = LudoApiHelper.getInstance(authToken);
                apiHelper.rollDice(roomId, new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<LudoRoomResponse> call, @NonNull Response<LudoRoomResponse> response) {
                        diceView.setClickable(true);
                        if (response.isSuccessful() && response.body() != null) {
                            LudoRoomResponse roomResponse = response.body();
                            int currentDiceValue = roomResponse.getDiceValue();

                            // ✅ Invalid dice value ko handle karein taaki crash na ho
                            if (currentDiceValue >= 1 && currentDiceValue <= 6) {
                                diceView.setImageResource(DICE_DRAWABLES[currentDiceValue - 1]);
                                Log.d(LOG_TAG, playerColor + " rolled: " + currentDiceValue);

                                Player currentPlayer = players[playerColor.getIndex()];
                                if (currentPlayer == null) {
                                    Log.e(LOG_TAG, "Current player is null. Cannot roll dice.");
                                    return;
                                }

                                if (currentDiceValue == STARTING_DICE_VALUE) {
                                    for (Token token : currentPlayer.tokens) {
                                        if (token.position == -1) {
                                            token.view.startAnimation(pulseAnimation);
                                        }
                                    }
                                }
                                checkForAutoMove(currentPlayer);
                                if (getSingleMovableToken(currentPlayer) == null) {
                                    currentPlayer.setTokensEnabled(true);
                                }

                                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                String jsonResponse = gson.toJson(roomResponse);
                                Log.d(LOG_TAG, "Roll Dice API Response: \n" + jsonResponse);
                                Toast.makeText(GameRoomActivity.this, "Dice Rolled Successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(LOG_TAG, "Invalid dice value received from server: " + currentDiceValue);
                                Toast.makeText(GameRoomActivity.this, "Invalid dice value. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(LOG_TAG, "Roll Dice API Failed: " + response.code() + " " + response.message());
                            Toast.makeText(GameRoomActivity.this, "Failed to roll dice.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LudoRoomResponse> call, @NonNull Throwable t) {
                        diceView.setClickable(true);
                        Log.e(LOG_TAG, "Roll Dice API Call Failed", t);
                        Toast.makeText(GameRoomActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        diceAnimator.start();
    }

    private void checkForAutoMove(Player currentPlayer) {
        Token movableToken = getSingleMovableToken(currentPlayer);

        if (movableToken != null) {
            movableToken.view.postDelayed(() -> {
                int tokenIndex = -1;
                for (int i = 0; i < currentPlayer.tokens.length; i++) {
                    if (currentPlayer.tokens[i] == movableToken) {
                        tokenIndex = i;
                        break;
                    }
                }
                if (tokenIndex != -1) {
                    MoveTokenRequest moveTokenRequest = new MoveTokenRequest(tokenIndex);
                    LudoApiHelper.moveToken(
                            roomId,
                            moveTokenRequest,
                            authToken,
                            new Callback<>() {
                                @Override
                                public void onResponse(@NonNull Call<MoveTokenResponse> call, @NonNull Response<MoveTokenResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        // ✅ Server se successful response aane par hi token move karenge
                                        try {
                                            Gson gson = new Gson();
                                            String responseJson = gson.toJson(response.body());
                                            Log.d(LOG_TAG, "Auto-move API Response JSON: " + responseJson);
                                        } catch (Exception e) {
                                            Log.e(LOG_TAG, "Failed to log auto-move response JSON", e);
                                        }

                                        Log.d(LOG_TAG, "Auto-moved token successfully via API.");
                                        currentPlayer.clearTokenHighlights();
                                        for (Token t : currentPlayer.tokens) {
                                            t.view.clearAnimation();
                                        }
                                        moveToken(currentPlayer, movableToken, response.body());
                                        currentPlayer.setTokensEnabled(false);
                                    } else {
                                        try {
                                            String errorJson = response.errorBody() != null ? response.errorBody().string() : "null";
                                            Log.e(LOG_TAG, "Failed to auto-move token via API: " + response.code() + " | ErrorBody: " + errorJson);
                                        } catch (Exception e) {
                                            Log.e(LOG_TAG, "Error parsing auto-move errorBody", e);
                                        }
                                        Toast.makeText(GameRoomActivity.this, "Failed to auto-move token.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<MoveTokenResponse> call, @NonNull Throwable throwable) {
                                    Log.e(LOG_TAG, "Auto-move API call failed", throwable);
                                    Toast.makeText(GameRoomActivity.this, "Network error during auto-move.", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
            }, 1000); // 1 second delay
        }
    }

    @Nullable
    private Token getSingleMovableToken(@NonNull Player player) {
        List<Token> movableTokens = new ArrayList<>();

        for (Token token : player.tokens) {
            if (canTokenMove(player, token, currentDiceValue)) {
                movableTokens.add(token);
            }
        }

        // Return token only if there's exactly one movable token
        return movableTokens.size() == 1 ? movableTokens.get(0) : null;
    }

    @Contract(pure = true)
    private boolean canTokenMove(Player player, @NonNull Token token, int diceValue) {
        // Case 1: Token in yard - can only move with 6
        if (token.position == -1) {
            return diceValue == STARTING_DICE_VALUE;
        }

        // Case 2: Token on path - check if move is within bounds
        int newPos = token.position + diceValue;
        return newPos < player.path.length;
    }

    private void onTokenClicked(@NonNull Player player, Token token) {
        // Clear highlights and animations for this player's tokens
        int tokenIndex = -1;


        for (int i = 0; i < player.tokens.length; i++) {
            if (player.tokens[i] == token) {
                tokenIndex = i;
                break;
            }
        }

        if (tokenIndex != -1) {
            MoveTokenRequest moveTokenRequest = new MoveTokenRequest(tokenIndex);

            try {
                Gson gson = new Gson();
                String requestJson = gson.toJson(moveTokenRequest);
                Log.d(LOG_TAG, "MoveTokenRequest JSON: " + requestJson);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to log request JSON", e);
            }
            LudoApiHelper.moveToken(roomId,
                    moveTokenRequest,
                    authToken,
                    new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<MoveTokenResponse> call, @NonNull Response<MoveTokenResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {

                                try {
                                    Gson gson = new Gson();
                                    String responseJson = gson.toJson(response.body());
                                    Log.d(LOG_TAG, "MoveTokenResponse JSON: " + responseJson);
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "Failed to log response JSON", e);
                                }
                                Log.d(LOG_TAG, "Token moved successfully via api");
                                player.clearTokenHighlights();
                                for (Token token1 : player.tokens) {
                                    token1.view.clearAnimation();
                                }
                                moveToken(player, token, response.body());
                                player.setTokensEnabled(false);
                                Toast.makeText(GameRoomActivity.this, "Token moved!", Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    String errorJson = response.errorBody() != null ? response.errorBody().string() : "null";
                                    Log.e(LOG_TAG, "Failed to move token via API: " + response.code() + " | ErrorBody: " + errorJson);
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "Error parsing errorBody", e);
                                }
                                Toast.makeText(GameRoomActivity.this, "Failed to move token.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<MoveTokenResponse> call, @NonNull Throwable throwable) {
                            Log.e(LOG_TAG, "API call failed", throwable);
                            Toast.makeText(GameRoomActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        }
    }

    /**
     * UI par token ki movement ko handle karta hai.
     * Yeh method server se aayi hui data ka upyog karta hai.
     *
     * @param player       Jiske turn hai woh player object
     * @param token        Jis token ko move karna hai
     * @param moveResponse Server se mili hui MoveTokenResponse
     */
    private void moveToken(@NonNull Player player, @NonNull Token token, MoveTokenResponse moveResponse) {

        // 1. Player ke color ko enum mein convert karein
        PlayerColor playerColor;
        try {
            playerColor = player.getColor();
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Invalid player color: " + player.getColor());
            return;
        }

        View[] path = null;
        switch (playerColor) {
            case YELLOW:
                path = getYellowPath();
                break;
            case GREEN:
                path = getGreenPath();
                break;
            case RED:
                path = getRedPath();
                break;
            case BLUE:
                path = getBluePath();
                break;
            default:
                return;
        }

        if (path == null) {
            Log.e(LOG_TAG, "Invalid player color or path not found.");
            return;
        }
        final int newPos = moveResponse.getNewPos();
        final int steps = moveResponse.getDice();

        int oldPos = token.position;
        token.position = newPos;

        if (oldPos == -1 && steps == 6) {
            animateTokenStep(player, token, oldPos, steps);
        } else if (oldPos != -1) {
            animateTokenStep(player, token, oldPos, steps);
        }

        if (moveResponse.getCaptured() != null) {
            Log.d(LOG_TAG, "Token captured!");
        }

        if (moveResponse.isGameOver()) {
            Log.d(LOG_TAG, "Game Over! Winner: " + moveResponse.getWinner());
            Toast.makeText(this, "Game Over! Winner: " + moveResponse.getWinner(), Toast.LENGTH_LONG).show();
        }
    }

    private void ensureTokenParenting(@NonNull Token token) {
        ViewGroup root = ludoBinding.getRoot();
        if (token.view.getParent() != root) {
            ViewGroup currentParent = (ViewGroup) token.view.getParent();
            if (currentParent != null) {
                currentParent.removeView(token.view);
            }
            root.addView(token.view);
        }
    }

    private void animateTokenStep(Player player, Token token, int currentPos, int stepsLeft) {
        if (stepsLeft <= 0) {
            return;
        }

        int nextPos = currentPos + 1;
        if (nextPos >= player.path.length) {
            Log.w(LOG_TAG, "Animation would move beyond path end");
            return;
        }

        View cell = player.path[nextPos];
        if (cell == null) {
            Log.e(LOG_TAG, "Cell at position " + nextPos + " is null for " + player.color);
            return;
        }

        // For step animation, we don't need to handle multiple tokens yet
        float[] coordinates = calculateTokenPosition(token, cell, 0, 1);

        token.view.animate()
                .x(coordinates[0])
                .y(coordinates[1])
                .setDuration(DICE_ANIMATION_DURATION)
                .withEndAction(() -> {
                    token.position = nextPos;
                    if (stepsLeft == 1) {
                        // Final position - place properly with multi-token support
                        placeTokenOnCell(token, cell);
                    } else {
                        animateTokenStep(player, token, nextPos, stepsLeft - 1);
                    }
                })
                .start();
    }

    @NonNull
    @Contract("_, _,_ , _ -> new")
    private float[] calculateTokenPosition(@NonNull Token token, @NonNull View cell, int tokenIndex, int totalTokens) {
        View root = ludoBinding.getRoot();

        int[] rootLoc = new int[2];
        root.getLocationOnScreen(rootLoc);

        int[] cellLoc = new int[2];
        cell.getLocationOnScreen(cellLoc);

        float cellCenterX = cellLoc[0] - rootLoc[0] + cell.getWidth() / 2f;
        float cellCenterY = cellLoc[1] - rootLoc[1] + cell.getHeight() / 2f;

        float tokenSize = token.view.getWidth();
        float newX, newY;

        // 🔹 Offset kam rakha for tight grouping
        float offset = tokenSize * 0.3f;

        switch (totalTokens) {
            case 1:
                // Single token → center
                newX = cellCenterX - tokenSize / 2f;
                newY = cellCenterY - tokenSize / 2f;
                break;

            case 2:
                // Ek left upar, ek right neeche (tight diagonal)
                if (tokenIndex == 0) {
                    newX = cellCenterX - offset;
                    newY = cellCenterY - offset;
                } else {
                    newX = cellCenterX + offset - tokenSize;
                    newY = cellCenterY + offset - tokenSize;
                }
                break;

            case 3:
                // Triangle (tight, center me adjust)
                if (tokenIndex == 0) {
                    newX = cellCenterX - tokenSize / 2f;
                    newY = cellCenterY - offset - tokenSize / 2f;
                } else if (tokenIndex == 1) {
                    newX = cellCenterX - offset;
                    newY = cellCenterY + offset - tokenSize / 2f;
                } else {
                    newX = cellCenterX + offset - tokenSize;
                    newY = cellCenterY + offset - tokenSize / 2f;
                }
                break;

            case 4:
                // 2x2 square (tight placement, like Ludo King)
                if (tokenIndex == 0) {
                    newX = cellCenterX - offset;
                    newY = cellCenterY - offset;
                } else if (tokenIndex == 1) {
                    newX = cellCenterX + offset - tokenSize;
                    newY = cellCenterY - offset;
                } else if (tokenIndex == 2) {
                    newX = cellCenterX - offset;
                    newY = cellCenterY + offset - tokenSize;
                } else {
                    newX = cellCenterX + offset - tokenSize;
                    newY = cellCenterY + offset - tokenSize;
                }
                break;

            default:
                // 5+ tokens → chhote circle me arrange
                double angle = (2 * Math.PI / totalTokens) * tokenIndex;
                float radius = tokenSize * 0.35f; // small radius
                newX = (float) (cellCenterX + radius * Math.cos(angle)) - tokenSize / 2f;
                newY = (float) (cellCenterY + radius * Math.sin(angle)) - tokenSize / 2f;
                break;
        }

        return new float[]{newX, newY};
    }

    private void placeTokenOnCell(@NonNull Token token, @NonNull View cell) {
        removeTokenFromAllCells(token);
        ensureTokenParenting(token);
        List<Token> tokensInCell = cellTokensMap.getOrDefault(cell, new ArrayList<>());
        assert tokensInCell != null;
        tokensInCell.add(token);
        cellTokensMap.put(cell, tokensInCell);
        // Update token's current cell
        token.currentCell = cell;

        // Rearrange all tokens in the cell
        rearrangeTokensInCell(cell);
    }

    private void rearrangeTokensInCell(@NonNull View cell) {
        List<Token> tokensInCell = cellTokensMap.get(cell);
        if (tokensInCell == null || tokensInCell.isEmpty()) {
            return;
        }

        int totalTokens = tokensInCell.size();

        for (int i = 0; i < totalTokens; i++) {
            Token token = tokensInCell.get(i);

            // Calculate new size based on number of tokens
            float scaleFactor = calculateScaleFactor(totalTokens);

            // Calculate new position
            float[] coordinates = calculateTokenPosition(token, cell, i, totalTokens);

            // Animate to new position and scale
            token.view.animate()
                    .x(coordinates[0])
                    .y(coordinates[1])
                    .scaleX(scaleFactor)
                    .scaleY(scaleFactor)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            // Bring to front for visibility
            token.view.bringToFront();
        }

        Log.d(LOG_TAG, "Rearranged " + totalTokens + " tokens in cell");
    }

    private float calculateScaleFactor(int totalTokens) {
        switch (totalTokens) {
            case 1:
                return 1.0f; // Full size
            case 2:
                return 0.8f; // 80% size
            case 3:
                return 0.7f; // 70% size
            case 4:
                return 0.6f; // 60% size
            default:
                return 0.5f; // 50% size for more than 4
        }
    }

    private void removeTokenFromAllCells(@NonNull Token token) {
        Iterator<Map.Entry<View, List<Token>>> iterator = cellTokensMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<View, List<Token>> entry = iterator.next();
            List<Token> tokens = entry.getValue();

            if (tokens.remove(token)) {
                if (tokens.isEmpty()) {
                    iterator.remove();
                } else {
                    // Rearrange remaining tokens
                    rearrangeTokensInCell(entry.getKey());
                }
                break;
            }
        }
    }

    private void playDiceSound() {
        if (diceSound != null) {
            diceSound.start();
        }
    }

    private void highLightMovableToken(Player player, int diceValue) {
        player.setTokensEnabled(true);
        for (Token token : player.tokens) {
            if (canTokenMove(player, token, diceValue)) {
                token.view.startAnimation(pulseAnimation);
            } else {
                token.view.setEnabled(false);
            }
        }
    }

    private int getMovableTokenCount(Player player, int diceValue) {
        int count = 0;
        for (Token token : player.tokens) {
            if (canTokenMove(player, token, diceValue)) {
                count++;
            }
        }
        return count;
    }

    private void leaveRoom() {
        if (userId == null || roomId == null) {
            Toast.makeText(GameRoomActivity.this, "Room or user not found", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject object = new JSONObject();
            object.put("roomId", roomId);
            object.put("userId", userId);

            SocketManager.emit("leaveroom", object);
            Log.d("GameRoomActivity", "leaveRoom socket emitted: " + object);

        } catch (Exception e) {
            Log.e("GameRoomActivity", "Socket emit error: " + e.getMessage());
            Toast.makeText(this, "Failed to send leave event", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Contract(value = " -> new", pure = true)
    private View[] getYellowPath() {
        return new View[]{
                ludoBinding.pathBottom13, ludoBinding.pathBottom10, ludoBinding.pathBottom7, ludoBinding.pathBottom4, ludoBinding.pathBottom1,
                ludoBinding.pathLeft18, ludoBinding.pathLeft17, ludoBinding.pathLeft16, ludoBinding.pathLeft15, ludoBinding.pathLeft14, ludoBinding.pathLeft13,
                ludoBinding.pathLeft7, ludoBinding.pathLeft1, ludoBinding.pathLeft2, ludoBinding.pathLeft3, ludoBinding.pathLeft4, ludoBinding.pathLeft5, ludoBinding.pathLeft6,
                ludoBinding.pathTop16, ludoBinding.pathTop13, ludoBinding.pathTop10, ludoBinding.pathTop7, ludoBinding.pathTop4, ludoBinding.pathTop1, ludoBinding.pathTop2,
                ludoBinding.pathTop3, ludoBinding.pathTop6, ludoBinding.pathTop9, ludoBinding.pathTop12, ludoBinding.pathTop15, ludoBinding.pathTop18,
                ludoBinding.pathRight1, ludoBinding.pathRight2, ludoBinding.pathRight3, ludoBinding.pathRight4, ludoBinding.pathRight5, ludoBinding.pathRight6,
                ludoBinding.pathRight12, ludoBinding.pathRight18, ludoBinding.pathRight17, ludoBinding.pathRight16, ludoBinding.pathRight15, ludoBinding.pathRight14,
                ludoBinding.pathRight13, ludoBinding.pathBottom3, ludoBinding.pathBottom6, ludoBinding.pathBottom9, ludoBinding.pathBottom12, ludoBinding.pathBottom15,
                ludoBinding.pathBottom18, ludoBinding.pathBottom17, ludoBinding.pathBottom14, ludoBinding.pathBottom11, ludoBinding.pathBottom8, ludoBinding.pathBottom5,
                ludoBinding.pathBottom2, ludoBinding.center
        };
    }

    @NonNull
    @Contract(value = " -> new", pure = true)
    private View[] getGreenPath() {
        return new View[]{
                ludoBinding.pathLeft2, ludoBinding.pathLeft3, ludoBinding.pathLeft4, ludoBinding.pathLeft5, ludoBinding.pathLeft6,
                ludoBinding.pathTop16, ludoBinding.pathTop13, ludoBinding.pathTop10, ludoBinding.pathTop7, ludoBinding.pathTop4, ludoBinding.pathTop1,
                ludoBinding.pathTop2, ludoBinding.pathTop3, ludoBinding.pathTop6, ludoBinding.pathTop9, ludoBinding.pathTop12, ludoBinding.pathTop15,
                ludoBinding.pathTop18, ludoBinding.pathRight1, ludoBinding.pathRight2, ludoBinding.pathRight3,
                ludoBinding.pathRight4, ludoBinding.pathRight5, ludoBinding.pathRight6, ludoBinding.pathRight12, ludoBinding.pathRight18,
                ludoBinding.pathRight17, ludoBinding.pathRight16, ludoBinding.pathRight15, ludoBinding.pathRight14, ludoBinding.pathRight13,
                ludoBinding.pathBottom3, ludoBinding.pathBottom6, ludoBinding.pathBottom9, ludoBinding.pathBottom12,
                ludoBinding.pathBottom15, ludoBinding.pathBottom18, ludoBinding.pathBottom17, ludoBinding.pathBottom16,
                ludoBinding.pathBottom13, ludoBinding.pathBottom10, ludoBinding.pathBottom7, ludoBinding.pathBottom4, ludoBinding.pathBottom1,
                ludoBinding.pathLeft18, ludoBinding.pathLeft17, ludoBinding.pathLeft16, ludoBinding.pathLeft15,
                ludoBinding.pathLeft14, ludoBinding.pathLeft13, ludoBinding.pathLeft7, ludoBinding.pathLeft8, ludoBinding.pathLeft9,
                ludoBinding.pathLeft10, ludoBinding.pathLeft11, ludoBinding.pathLeft12, ludoBinding.center
        };
    }

    @NonNull
    @Contract(value = " -> new", pure = true)
    private View[] getRedPath() {
        return new View[]{
                ludoBinding.pathTop6, ludoBinding.pathTop9, ludoBinding.pathTop12, ludoBinding.pathTop15, ludoBinding.pathTop18,
                ludoBinding.pathRight1, ludoBinding.pathRight2, ludoBinding.pathRight3, ludoBinding.pathRight4, ludoBinding.pathRight5,
                ludoBinding.pathRight6, ludoBinding.pathRight12, ludoBinding.pathRight18, ludoBinding.pathRight17, ludoBinding.pathRight16,
                ludoBinding.pathRight15, ludoBinding.pathRight14, ludoBinding.pathRight13, ludoBinding.pathBottom3, ludoBinding.pathBottom6,
                ludoBinding.pathBottom9, ludoBinding.pathBottom12, ludoBinding.pathBottom15, ludoBinding.pathBottom18, ludoBinding.pathBottom17,
                ludoBinding.pathBottom16, ludoBinding.pathBottom13, ludoBinding.pathBottom10, ludoBinding.pathBottom7,
                ludoBinding.pathBottom4, ludoBinding.pathBottom1, ludoBinding.pathLeft18, ludoBinding.pathLeft17,
                ludoBinding.pathLeft16, ludoBinding.pathLeft15, ludoBinding.pathLeft14, ludoBinding.pathLeft13, ludoBinding.pathLeft7,
                ludoBinding.pathLeft1, ludoBinding.pathLeft2, ludoBinding.pathLeft3, ludoBinding.pathLeft4, ludoBinding.pathLeft5,
                ludoBinding.pathLeft6, ludoBinding.pathTop16, ludoBinding.pathTop13, ludoBinding.pathTop10, ludoBinding.pathTop7,
                ludoBinding.pathTop4, ludoBinding.pathTop1, ludoBinding.pathTop2, ludoBinding.pathTop5, ludoBinding.pathTop8, ludoBinding.pathTop11,
                ludoBinding.pathTop14, ludoBinding.pathTop17, ludoBinding.center
        };
    }

    @NonNull
    @Contract(value = " -> new", pure = true)
    private View[] getBluePath() {
        return new View[]{
                ludoBinding.pathRight17, ludoBinding.pathRight16, ludoBinding.pathRight15, ludoBinding.pathRight14, ludoBinding.pathRight13,
                ludoBinding.pathBottom3, ludoBinding.pathBottom6, ludoBinding.pathBottom9, ludoBinding.pathBottom12, ludoBinding.pathBottom15,
                ludoBinding.pathBottom18, ludoBinding.pathBottom17, ludoBinding.pathBottom16, ludoBinding.pathBottom13, ludoBinding.pathBottom10,
                ludoBinding.pathBottom7, ludoBinding.pathBottom4, ludoBinding.pathBottom1, ludoBinding.pathLeft18, ludoBinding.pathLeft17,
                ludoBinding.pathLeft16, ludoBinding.pathLeft15, ludoBinding.pathLeft14, ludoBinding.pathLeft13, ludoBinding.pathLeft7,
                ludoBinding.pathLeft1, ludoBinding.pathLeft2, ludoBinding.pathLeft3, ludoBinding.pathLeft4,
                ludoBinding.pathLeft5, ludoBinding.pathLeft6, ludoBinding.pathTop16, ludoBinding.pathTop13, ludoBinding.pathTop10,
                ludoBinding.pathTop7, ludoBinding.pathTop4, ludoBinding.pathTop1, ludoBinding.pathTop2, ludoBinding.pathTop3,
                ludoBinding.pathTop6, ludoBinding.pathTop9, ludoBinding.pathTop12, ludoBinding.pathTop15, ludoBinding.pathTop18,
                ludoBinding.pathRight1, ludoBinding.pathRight2, ludoBinding.pathRight3, ludoBinding.pathRight4,
                ludoBinding.pathRight5, ludoBinding.pathRight6, ludoBinding.pathRight12, ludoBinding.pathRight11,
                ludoBinding.pathRight10, ludoBinding.pathRight9, ludoBinding.pathRight8, ludoBinding.pathRight7, ludoBinding.center
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (diceSound != null) {
            diceSound.release();
            diceSound = null;
        }
        SocketManager.getSocket().off("leaveroom");
        SocketManager.getSocket().off("roomUpdate");
        SocketManager.getSocket().off("roomClosed");
    }
}
