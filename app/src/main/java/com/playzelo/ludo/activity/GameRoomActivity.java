package com.playzelo.ludo.activity;


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
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.playzelo.ludo.R;
import com.playzelo.ludo.databinding.ActivityGameRoomBinding;
import com.playzelo.ludo.databinding.LudoLayoutBinding;
import org.jetbrains.annotations.Contract;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


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
    private ActivityGameRoomBinding binding;
    private LudoLayoutBinding ludoBinding;

    // Game state
    private int currentPlayerIndex = 0; // Track current player turn
    private int currentDiceValue = 0;
    private MediaPlayer diceSound;
    private AnimatorSet diceAnimator;
    private String roomId;
    private Random random = new Random();

    // UI elements
    private ImageView[] diceViews = new ImageView[4];
    private final Player[] players = new Player[4];

    // Dice drawable resources
    private static final int[] DICE_DRAWABLES = {
            R.drawable.dice_1, R.drawable.dice_2, R.drawable.dice_3,
            R.drawable.dice_4, R.drawable.dice_5, R.drawable.dice_6
    };
    private final TextView[] playerInfoTextViews = new TextView[4];

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

    // Token class
    static class Token {
        ImageView view;
        int position = -1; // -1 = not on board yet
        View currentCell = null; // Track current cell

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
        int score = 0;

        Player(PlayerColor color, View[] path) {
            this.color = color;
            this.path = path;
        }

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
        binding = ActivityGameRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ludoBinding = LudoLayoutBinding.bind(binding.ludoLayout.getRoot());
        initializeSound();
        initializeDice();

        // Get player data from Intent
        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");
        authToken = getIntent().getStringExtra("auth_token");
        roomId = getIntent().getStringExtra("roomId");
        entry_fee = getIntent().getDoubleExtra("entryFee", 0);
        prize_pool = getIntent().getDoubleExtra("winPrize", 0);

        List<String> playerIds = getIntent().getStringArrayListExtra("playerIds");
        List<String> playerColors = getIntent().getStringArrayListExtra("playerColors");

        initializePlayers(playerIds, playerColors);
        Log.d(LOG_TAG, "Username: " + username + " userId: " + userId + " roomId: " + roomId);
        initializePlayerInfoTextViews();
        diceViews = new ImageView[]{binding.leftDice, binding.topDiceLeft, binding.topDiceRight, binding.rightDice};
        binding.backButton.setOnClickListener(v -> finish());
        binding.prizePool.setText(String.valueOf(prize_pool));

        // Enable dice for first player
        updateTurnUI();
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

                String playerName = id;
                if (id.equals(userId)) {
                    playerName = username;
                }

                players[index] = new Player(color, paths[index]);
                players[index].setPlayerDetails(id, playerName);

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

    private void initializePlayerInfoTextViews() {
        playerInfoTextViews[PlayerColor.YELLOW.getIndex()] = binding.bottomLeftPlayerName;
        playerInfoTextViews[PlayerColor.GREEN.getIndex()] = binding.leftPlayerName;
        playerInfoTextViews[PlayerColor.RED.getIndex()] = binding.rightPlayerName;
        playerInfoTextViews[PlayerColor.BLUE.getIndex()] = binding.bottomRightPlayerName;
    }

    private void updateTurnUI() {
        // Disable all dice first
        for (int i = 0; i < diceViews.length; i++) {
            diceViews[i].setEnabled(false);
            diceViews[i].setAlpha(0.5f);
        }

        // Enable current player's dice
        if (players[currentPlayerIndex] != null) {
            diceViews[currentPlayerIndex].setEnabled(true);
            diceViews[currentPlayerIndex].setAlpha(1.0f);
            Toast.makeText(this, players[currentPlayerIndex].username + "'s turn", Toast.LENGTH_SHORT).show();
        }
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

                // 🎲 Random dice value for this player
                currentDiceValue = random.nextInt(6) + 1;
                diceView.setImageResource(DICE_DRAWABLES[currentDiceValue - 1]);

                Log.d(LOG_TAG, playerColor + " rolled: " + currentDiceValue);

                Player currentPlayer = players[playerColor.getIndex()];
                if (currentPlayer == null) {
                    Log.e(LOG_TAG, "Player is null for color: " + playerColor);
                    diceView.setClickable(true);
                    return;
                }

                // Enable tokens for THIS player
                checkForAutoMove(currentPlayer);

                diceView.setClickable(true);
            }
        });

        diceAnimator.start();
    }

    private void checkForAutoMove(Player currentPlayer) {
        if (currentDiceValue == STARTING_DICE_VALUE) {
            List<Token> homeTokens = getHomeTokens(currentPlayer);
            List<Token> boardTokens = getBoardMovableTokens(currentPlayer);

            if (!homeTokens.isEmpty() && !boardTokens.isEmpty()) {
                enableSelectableTokens(currentPlayer, homeTokens, boardTokens);
                Toast.makeText(this, "Choose a token to move!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!homeTokens.isEmpty() && boardTokens.isEmpty()) {
                Token homeToken = homeTokens.get(0);
                performAutoMove(currentPlayer, homeToken);
                return;
            }

            if (homeTokens.isEmpty() && boardTokens.size() == 1) {
                performAutoMove(currentPlayer, boardTokens.get(0));
                return;
            }

            if (homeTokens.isEmpty() && boardTokens.size() > 1) {
                enableSelectableTokens(currentPlayer, new ArrayList<>(), boardTokens);
                Toast.makeText(this, "Choose a token to move!", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Token movableToken = getSingleMovableToken(currentPlayer);
            if (movableToken != null) {
                performAutoMove(currentPlayer, movableToken);
            } else {
                List<Token> movableTokens = getAllMovableTokens(currentPlayer);
                if (!movableTokens.isEmpty()) {
                    enableSelectableTokensForNonSix(currentPlayer, movableTokens);
                    Toast.makeText(this, "Choose a token to move!", Toast.LENGTH_SHORT).show();
                } else {
                    // No movable tokens, skip turn
                    Toast.makeText(this, "No valid moves! Turn skipped.", Toast.LENGTH_SHORT).show();
                    switchToNextPlayer();
                }
            }
        }
    }

    private List<Token> getHomeTokens(Player player) {
        List<Token> homeTokens = new ArrayList<>();
        for (Token token : player.tokens) {
            if (token.position == -1) {
                homeTokens.add(token);
            }
        }
        return homeTokens;
    }

    private List<Token> getBoardMovableTokens(Player player) {
        List<Token> boardTokens = new ArrayList<>();
        for (Token token : player.tokens) {
            if (token.position != -1 && canTokenMove(player, token, currentDiceValue)) {
                boardTokens.add(token);
            }
        }
        return boardTokens;
    }

    private List<Token> getAllMovableTokens(Player player) {
        List<Token> movableTokens = new ArrayList<>();
        for (Token token : player.tokens) {
            if (canTokenMove(player, token, currentDiceValue)) {
                movableTokens.add(token);
            }
        }
        return movableTokens;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void enableSelectableTokens(Player player, List<Token> homeTokens, List<Token> boardTokens) {
        player.setTokensEnabled(false);
        player.clearTokenHighlights();

        for (Token token : homeTokens) {
            token.view.setEnabled(true);
            token.view.setBackground(getDrawable(R.drawable.token_ring));
        }

        for (Token token : boardTokens) {
            token.view.setEnabled(true);
            token.view.setBackground(getDrawable(R.drawable.token_ring));
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void enableSelectableTokensForNonSix(Player player, List<Token> movableTokens) {
        player.setTokensEnabled(false);
        player.clearTokenHighlights();

        for (Token token : movableTokens) {
            token.view.setEnabled(true);
            token.view.setBackground(getDrawable(R.drawable.token_ring));
        }
    }

    private void performAutoMove(Player currentPlayer, @NonNull Token movableToken) {
        movableToken.view.postDelayed(() -> {
            Log.d(LOG_TAG, "Auto-moving token");
            currentPlayer.clearTokenHighlights();
            for (Token t : currentPlayer.tokens) {
                t.view.clearAnimation();
            }
            moveToken(currentPlayer, movableToken);
            currentPlayer.setTokensEnabled(false);

            // Switch turn after auto-move (unless rolled 6)
            if (currentDiceValue != STARTING_DICE_VALUE) {
                switchToNextPlayer();
            }
        }, 500);
    }

    @Nullable
    private Token getSingleMovableToken(@NonNull Player player) {
        List<Token> movableTokens = new ArrayList<>();

        for (Token token : player.tokens) {
            if (canTokenMove(player, token, currentDiceValue)) {
                movableTokens.add(token);
            }
        }

        return movableTokens.size() == 1 ? movableTokens.get(0) : null;
    }

    @Contract(pure = true)
    private boolean canTokenMove(Player player, @NonNull Token token, int diceValue) {
        if (token.position == -1) {
            return diceValue == STARTING_DICE_VALUE;
        }

        int newPos = token.position + diceValue;
        return newPos < player.path.length;
    }

    private void onTokenClicked(@NonNull Player player, Token token) {
        // Only allow current player to move tokens
        if (player.color.getIndex() != currentPlayerIndex) {
            Toast.makeText(this, "Not your turn!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if token can move
        if (!canTokenMove(player, token, currentDiceValue)) {
            Toast.makeText(this, "Cannot move this token!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(LOG_TAG, "Token clicked and moved");
        player.clearTokenHighlights();
        for (Token token1 : player.tokens) {
            token1.view.clearAnimation();
        }
        moveToken(player, token);
        player.setTokensEnabled(false);
        Toast.makeText(GameRoomActivity.this, "Token moved!", Toast.LENGTH_SHORT).show();

        // Switch turn (unless rolled 6)
        if (currentDiceValue != STARTING_DICE_VALUE) {
            switchToNextPlayer();
        }
    }

    private void switchToNextPlayer() {
        // Move to next player
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % 4;
        } while (players[currentPlayerIndex] == null);

        updateTurnUI();
    }

    private void moveToken(@NonNull Player player, @NonNull Token token) {
        PlayerColor playerColor = player.getColor();
        View[] path = player.path;

        if (path == null) {
            Log.e(LOG_TAG, "Invalid player color or path not found.");
            return;
        }

        int correctNewPos;
        final int steps = currentDiceValue;
        int oldPos = token.position;

        if (oldPos == -1 && steps == 6) {
            correctNewPos = 0;
            token.position = correctNewPos;
            View startingCell = path[correctNewPos];
            animateTokenToPosition(token, startingCell);
            Log.d(LOG_TAG, "Token moved from home to starting position (index 0)");
        } else if (oldPos != -1) {
            correctNewPos = oldPos + steps;

            if (correctNewPos >= path.length) {
                Toast.makeText(this, "Cannot move that far", Toast.LENGTH_SHORT).show();
                return;
            }

            token.position = correctNewPos;
            animateTokenStep(player, token, oldPos, steps);

            // Check if token reached center (win condition)
            if (correctNewPos == path.length - 1) {
                player.score++;
                updatePlayerScoreUI(player.color.getColorName(), player.score);
                Toast.makeText(this, player.username + " scored!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void animateTokenToPosition(Token token, View targetCell) {
        ensureTokenParenting(token);

        float[] coordinates = calculateTokenPosition(token, targetCell, 0, 1);

        token.view.animate()
                .x(coordinates[0])
                .y(coordinates[1])
                .setDuration(DICE_ANIMATION_DURATION * 2)
                .withEndAction(() -> {
                    placeTokenOnCell(token, targetCell);
                    Log.d(LOG_TAG, "Token successfully placed at starting position");
                })
                .start();
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

        float[] coordinates = calculateTokenPosition(token, cell, 0, 1);

        token.view.animate()
                .x(coordinates[0])
                .y(coordinates[1])
                .setDuration(DICE_ANIMATION_DURATION)
                .withEndAction(() -> {
                    token.position = nextPos;
                    if (stepsLeft == 1) {
                        placeTokenOnCell(token, cell);
                    } else {
                        animateTokenStep(player, token, nextPos, stepsLeft - 1);
                    }
                })
                .start();
    }

    @NonNull
    @Contract("_, _, _, _ -> new")
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

        float offset = tokenSize * 0.3f;

        switch (totalTokens) {
            case 1:
                newX = cellCenterX - tokenSize / 2f;
                newY = cellCenterY - tokenSize / 2f;
                break;

            case 2:
                if (tokenIndex == 0) {
                    newX = cellCenterX - offset;
                    newY = cellCenterY - offset;
                } else {
                    newX = cellCenterX + offset - tokenSize;
                    newY = cellCenterY + offset - tokenSize;
                }
                break;

            case 3:
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
                double angle = (2 * Math.PI / totalTokens) * tokenIndex;
                float radius = tokenSize * 0.35f;
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
        tokensInCell.add(token);
        cellTokensMap.put(cell, tokensInCell);
        token.currentCell = cell;

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

            float scaleFactor = calculateScaleFactor(totalTokens);
            float[] coordinates = calculateTokenPosition(token, cell, i, totalTokens);

            token.view.animate()
                    .x(coordinates[0])
                    .y(coordinates[1])
                    .scaleX(scaleFactor)
                    .scaleY(scaleFactor)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            token.view.bringToFront();
        }

        Log.d(LOG_TAG, "Rearranged " + totalTokens + " tokens in cell");
    }

    private float calculateScaleFactor(int totalTokens) {
        switch (totalTokens) {
            case 1:
                return 1.0f;
            case 2:
                return 0.8f;
            case 3:
                return 0.7f;
            case 4:
                return 0.6f;
            default:
                return 0.5f;
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
                    rearrangeTokensInCell(entry.getKey());
                }
                break;
            }
        }
    }

    @SuppressLint({"SetTextI18n", "RestrictedApi"})
    private void updatePlayerScoreUI(@NonNull String playerColorStr, int newScore) {
        PlayerColor playerColor;
        try {
            playerColor = PlayerColor.valueOf(playerColorStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Invalid player color: " + playerColorStr);
            return;
        }

        TextView scoreTextView = null;
        switch (playerColor) {
            case RED:
                scoreTextView = ludoBinding.redScore;
                break;
            case BLUE:
                scoreTextView = ludoBinding.blueScore;
                break;
            case GREEN:
                scoreTextView = ludoBinding.greenScore;
                break;
            case YELLOW:
                scoreTextView = ludoBinding.yellowScore;
                break;
        }

        if (scoreTextView != null) {
            scoreTextView.setText("Score: " + newScore);
        }
    }

    private void playDiceSound() {
        if (diceSound != null) {
            diceSound.start();
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
    }
}
