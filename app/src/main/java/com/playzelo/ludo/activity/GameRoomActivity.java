package com.playzelo.ludo.activity;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import io.socket.client.Socket;
import com.playzelo.ludo.utils.SocketManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.playzelo.ludo.R;
import com.playzelo.ludo.databinding.ActivityGameRoomBinding;
import com.playzelo.ludo.databinding.LudoLayoutBinding;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class GameRoomActivity extends AppCompatActivity {

    private static final int DICE_ANIMATION_DURATION = 300;
    private static final int TOKEN_COUNT = 4;
    private static final int STARTING_DICE_VALUE = 6;
    private static final String LOG_TAG = "LudoGame";

    private String currentTurnPlayerId = null;
    private PlayerColor currentTurnColor = null;
    private int currentPlayerIndex = 0;

    private final Map<View, List<Token>> cellTokensMap = new HashMap<>();

    private String userId, authToken, username, photo,gameId;
    private Animation pulseAnimation;
    private ActivityGameRoomBinding binding;
    private LudoLayoutBinding ludoBinding;

    public ImageView[][] getTokenViews() {
        return tokenViews;
    }

    public void setTokenViews(ImageView[][] tokenViews) {
        this.tokenViews = tokenViews;
    }

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
    private Socket socket;
    private ImageView[][] tokenViews;
    Random random = new Random();

    // UI elements
    private ImageView[] diceViews = new ImageView[4];
    private final Player[] players = new Player[4];

    // Dice drawable resources
    private static final int[] DICE_DRAWABLES = {
            R.drawable.dice_1, R.drawable.dice_2, R.drawable.dice_3,
            R.drawable.dice_4, R.drawable.dice_5, R.drawable.dice_6
    };
    private final TextView[] playerInfoTextViews = new TextView[4];
    private final TextView[] scoreTextViews = new TextView[4];
    private ImageView[] avatarViews = new ImageView[4];


    // Token class
    static class Token {
        ImageView view;
        int position = -1; // -1 = not on board yet
        View currentCell = null; // Track current cell

        Token(ImageView view) {
            this.view = view;
        }
    }

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
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_ring);

        socket = SocketManager.getSocket();

        socket.off("turnTaken");
        socket.off("turnSkipped");
        socket.off("gameFinished");

        setupSocketListener();

        initializeSound();
        initializeDice();
        initializeScoreTextViews();

        // Get player data from Intent
        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");
        authToken = getIntent().getStringExtra("token");
        roomId = getIntent().getStringExtra("roomId");
        photo = getIntent().getStringExtra("photo");
        gameId = getIntent().getStringExtra("gameId");

        currentPlayerIndex = getIntent().getIntExtra("currentPlayerIndex",0);
        currentTurnPlayerId = getIntent().getStringExtra("firstTurnPlayerId");


        List<String> playerIds = getIntent().getStringArrayListExtra("playerIds");
        List<String> playerColors = getIntent().getStringArrayListExtra("playerColors");
        List<String> playerNames = getIntent().getStringArrayListExtra("playerNames");

        avatarViews[PlayerColor.YELLOW.getIndex()] = binding.bottomLeftAvatar;
        avatarViews[PlayerColor.GREEN.getIndex()] = binding.leftPlayerAvatar;
        avatarViews[PlayerColor.RED.getIndex()] = binding.rightPlayerAvatar;
        avatarViews[PlayerColor.BLUE.getIndex()] = binding.bottomRightAvatar;

        initializePlayerInfoTextViews();
        initializePlayers(playerIds,playerNames, playerColors);

        if (playerIds.size() == 2) {
            hideUnusedPlayers(playerColors);
        }

        Log.d(LOG_TAG, "Username: " + username + " userId: " + userId + " Token: " + authToken + " roomId: " + roomId);
        Log.d(LOG_TAG, "gameId: " + gameId);
        Log.d(LOG_TAG, "Socket Connected: " + socket.connected());
        Log.d(LOG_TAG, "Socket ID: " + socket.id());


        diceViews = new ImageView[]{binding.leftDice, binding.topDiceLeft, binding.topDiceRight, binding.rightDice};
        binding.backButton.setOnClickListener(v -> leaveRoom());

        applyInitialTurn();
    }

    private void applyInitialTurn() {
        Log.d(LOG_TAG, "═══════════════════════════════════════");
        Log.d(LOG_TAG, "🎮 APPLYING INITIAL TURN");
        Log.d(LOG_TAG, "Current Turn Player ID: " + currentTurnPlayerId);
        Log.d(LOG_TAG, "Current Player Index: " + currentPlayerIndex);
        Log.d(LOG_TAG, "My User ID: " + userId);
        Log.d(LOG_TAG, "═══════════════════════════════════════");

        // 🔥 FALLBACK: If no turn info, try to find RED player (usually starts first)
        if (currentTurnPlayerId == null || currentTurnPlayerId.isEmpty()) {
            Log.w(LOG_TAG, "⚠️ No initial turn info from server");

            // Try RED player first (common Ludo rule)
            Player redPlayer = players[PlayerColor.RED.getIndex()];
            if (redPlayer != null && redPlayer.userId != null) {
                currentTurnPlayerId = redPlayer.userId;
                Log.d(LOG_TAG, "🔴 Defaulting to RED player: " + currentTurnPlayerId);
            } else {
                // Otherwise, request from server
                Log.w(LOG_TAG, "📤 Requesting game state from server...");
                requestGameState();
                return;
            }
        }

        // Apply turn to all dice
        for (PlayerColor color : PlayerColor.values()) {
            Player player = players[color.getIndex()];
            if (player == null) continue;

            boolean isThisPlayerTurn = player.userId != null &&
                    player.userId.equals(currentTurnPlayerId);

            ImageView dice = diceViews[color.getIndex()];

            if (isThisPlayerTurn) {
                // Show dice for current turn player
                showDiceForTurn(dice, player.userId.equals(userId));

                Log.d(LOG_TAG, "✅ " + color.getColorName() + " dice VISIBLE (their turn)");
            } else {
                // Hide dice for others
                hideDice(dice);

                Log.d(LOG_TAG, "❌ " + color.getColorName() + " dice HIDDEN");
            }
        }

        // Show message
        if (currentTurnPlayerId.equals(userId)) {
            Toast.makeText(this, "🎲 Your turn first!", Toast.LENGTH_LONG).show();
        } else {
            // Find opponent name
            Player turnPlayer = getPlayerById(currentTurnPlayerId);
            if (turnPlayer != null) {
                Toast.makeText(this, turnPlayer.username + "'s turn", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void requestGameState() {
        if (!socket.connected()) {
            Log.e(LOG_TAG, "❌ Socket not connected, cannot request game state");
            return;
        }

        try {
            JSONObject obj = new JSONObject();
            obj.put("gameId", gameId);
            obj.put("userId", userId);

            Log.d(LOG_TAG, "📤 Requesting game state...");
            socket.emit("getGameState", obj);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Request game state error: " + e.getMessage());
        }
    }

    private Player getPlayerById(String id) {
        for (Player p : players) {
            if (p != null && p.userId.equals(id)) return p;
        }
        return null;
    }

    private void animateDiceRoll(ImageView diceView, int finalValue, boolean isMyTurn) {

        // Cancel any existing animation
        if (diceAnimator != null && diceAnimator.isRunning()) {
            diceAnimator.cancel();
        }

        // Disable dice during animation
        diceView.setClickable(false);

        // Create bounce animation
        ObjectAnimator bounceUp = ObjectAnimator.ofFloat(diceView, "translationY", 0f, -80f);
        bounceUp.setDuration(150);

        ObjectAnimator bounceDown = ObjectAnimator.ofFloat(diceView, "translationY", -80f, 0f);
        bounceDown.setDuration(300);
        bounceDown.setInterpolator(new BounceInterpolator());

        // Create rotation animation
        ObjectAnimator rotate = ObjectAnimator.ofFloat(diceView, "rotation", 0f, 720f);
        rotate.setDuration(450);

        // Create scale animation for emphasis
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(diceView, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(diceView, "scaleY", 1f, 1.2f, 1f);
        scaleX.setDuration(450);
        scaleY.setDuration(450);

        // Combine animations
        diceAnimator = new AnimatorSet();
        diceAnimator.playSequentially(bounceUp, bounceDown);
        diceAnimator.play(rotate).with(bounceUp);
        diceAnimator.play(scaleX).with(scaleY).with(rotate);

        // Animate through random dice faces during roll
        final int[] randomSequence = generateRandomDiceSequence(6);
        final long frameDelay = 75; // milliseconds between frame changes

        for (int i = 0; i < randomSequence.length; i++) {
            final int diceFrame = randomSequence[i];
            diceView.postDelayed(() -> {
                diceView.setImageResource(DICE_DRAWABLES[diceFrame - 1]);
            }, i * frameDelay);
        }

        diceAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Set final dice value
                diceView.setImageResource(DICE_DRAWABLES[finalValue - 1]);

                // Re-enable if it's still my turn
                if (isMyTurn) {
                    diceView.setClickable(true);
                }

                Log.d(LOG_TAG, "✅ Dice animation completed: " + finalValue);
            }
        });

        diceAnimator.start();
    }

    private void animateDiceResult(ImageView diceView, int finalValue, Runnable onComplete) {

        // Reset rotation first
        diceView.setRotation(0f);

        // Bounce up
        diceView.animate()
                .translationY(-50f)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {

                    // Bounce down
                    diceView.animate()
                            .translationY(0f)
                            .setDuration(200)
                            .setInterpolator(new BounceInterpolator())
                            .withEndAction(() -> {

                                // Set final dice face
                                diceView.setImageResource(DICE_DRAWABLES[finalValue - 1]);

                                Log.d(LOG_TAG, "✅ Dice result: " + finalValue);

                                // Callback
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            })
                            .start();
                })
                .start();
    }

    private int[] generateRandomDiceSequence(int count) {
        int[] sequence = new int[count];
        for (int i = 0; i < count; i++) {
            sequence[i] = random.nextInt(6) + 1;
        }
        return sequence;
    }

    //    private void setupSocketListener() {
//
//        socket.onAnyIncoming(args -> {
//            String eventData = args.length > 0 ? args[0].toString() : "no data";
//            Log.d(LOG_TAG, "📥 INCOMING: " + eventData);
//        });
//
//        socket.on(Socket.EVENT_CONNECT, args -> {
//            Log.d(LOG_TAG, "✅ Socket connected - ID: " + socket.id());
//        });
//
//        socket.on(Socket.EVENT_DISCONNECT, args ->
//                Log.d(LOG_TAG, "❌ Socket disconnected"));
//
//        // 🔥 ADD THIS - Listen for game state/sync
//        socket.on("gameState", args -> runOnUiThread(() -> {
//            try {
//                JSONObject data = (JSONObject) args[0];
//                Log.d(LOG_TAG, "📥 gameState: " + data.toString());
//
//                int index = data.optInt("currentPlayerIndex",0);
//                JSONArray playersArray = data.optJSONArray("players");
//                if(playersArray != null && index < playersArray.length()){
//                    currentTurnPlayerId = playersArray.getJSONObject(index).optString("userId");
//                    currentPlayerIndex = index;
//                    updateAllDiceVisibility();
//                }
//                updateTurnFromGame(data);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }));
//
//        socket.on("turnTaken", args -> runOnUiThread(() -> {
//            try {
//                Log.d(LOG_TAG, "📥 turnTaken received!");
//                JSONObject data = (JSONObject) args[0];
//                Log.d(LOG_TAG, "📥 Data: " + data.toString());
//
//                String playerId = data.getString("playerId");
//                int dice = data.getInt("dice");
//
//                // 🔥 Check if this is MY turn response
//                if (playerId.equals(userId)) {
//                    // Cancel rolling animation for my dice
//                    Player myPlayer = getPlayerById(userId);
//                    if (myPlayer != null) {
//                        ImageView myDice = diceViews[myPlayer.getColor().getIndex()];
//                        ObjectAnimator rollingAnimator = (ObjectAnimator) myDice.getTag();
//                        if (rollingAnimator != null) {
//                            rollingAnimator.cancel();
//                            myDice.setTag(null);
//                        }
//                    }
//                }
//
//                int tokenIndex = data.optInt("tokenIndex", -1);
//                int from = data.optInt("from", -1);
//                int to = data.optInt("to", -1);
//                JSONObject game = data.getJSONObject("game");
//
//                handleServerTurn(playerId, dice, tokenIndex, from, to, game);
//
//            } catch (Exception e) {
//                Log.e(LOG_TAG, "turnTaken error: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }));
//        socket.on("turnSkipped", args -> runOnUiThread(() -> {
//            try {
//                Log.d(LOG_TAG, "📥 turnSkipped received!");
//                JSONObject data = (JSONObject) args[0];
//
//                // 🔥 Check if there's a reason
//                String reason = data.optString("reason", "Not your turn");
//                Log.d(LOG_TAG, "Skip reason: " + reason);
//
//                JSONObject game = data.optJSONObject("game");
//                if (game != null) {
//                    updateTurnFromGame(game);
//                }
//
//                Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }));
//
//        socket.on("gameFinished", args -> runOnUiThread(() -> {
//            try {
//                JSONObject data = (JSONObject) args[0];
//                String winnerId = data.getString("winner");
//                if (winnerId.equals(userId))
//                    Toast.makeText(this, "🎉 You Win!", Toast.LENGTH_LONG).show();
//                else
//                    Toast.makeText(this, "😢 You Lose!", Toast.LENGTH_LONG).show();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }));
//
//        socket.on("error", args -> runOnUiThread(() -> {
//            Log.e(LOG_TAG, "❌ Socket Error: " + args[0].toString());
//            Toast.makeText(this, "Error: " + args[0], Toast.LENGTH_SHORT).show();
//        }));
//    }

    private void setupSocketListener() {

        socket.onAnyIncoming(args -> {
            String eventData = args.length > 0 ? args[0].toString() : "no data";
            Log.d(LOG_TAG, "📥 INCOMING EVENT: " + eventData);
        });

        socket.on(Socket.EVENT_CONNECT, args -> {
            Log.d(LOG_TAG, "✅ Socket connected - ID: " + socket.id());
        });

        socket.on(Socket.EVENT_DISCONNECT, args ->
                Log.d(LOG_TAG, "❌ Socket disconnected"));

        // 🔥 Listen for game state
        socket.on("gameState", args -> runOnUiThread(() -> {
            try {
                JSONObject data = (JSONObject) args[0];
                Log.d(LOG_TAG, "📥 gameState received!");
                Log.d(LOG_TAG, data.toString(2));

                // Extract turn info
                if (data.has("currentPlayerIndex")) {
                    int index = data.getInt("currentPlayerIndex");
                    JSONArray playersArray = data.optJSONArray("players");

                    if (playersArray != null && index < playersArray.length()) {
                        JSONObject currentPlayer = playersArray.getJSONObject(index);
                        currentTurnPlayerId = currentPlayer.getString("userId");
                        currentPlayerIndex = index;

                        Log.d(LOG_TAG, "✅ Got turn from gameState: " + currentTurnPlayerId);

                        // Apply turn
                        updateAllDiceVisibility();
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "gameState error: " + e.getMessage());
                e.printStackTrace();
            }
        }));

        // 🔥 MAYBE server sends "currentGame" instead?
        socket.on("currentGame", args -> runOnUiThread(() -> {
            try {
                JSONObject data = (JSONObject) args[0];
                Log.d(LOG_TAG, "📥 currentGame received!");
                Log.d(LOG_TAG, data.toString(2));

                // Same logic as gameState
                if (data.has("currentPlayerIndex")) {
                    int index = data.getInt("currentPlayerIndex");
                    JSONArray playersArray = data.optJSONArray("players");

                    if (playersArray != null && index < playersArray.length()) {
                        JSONObject currentPlayer = playersArray.getJSONObject(index);
                        currentTurnPlayerId = currentPlayer.getString("userId");

                        updateAllDiceVisibility();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        socket.on("turnTaken", args -> runOnUiThread(() -> {
            try {
                Log.d(LOG_TAG, "📥 turnTaken received!");
                JSONObject data = (JSONObject) args[0];
                Log.d(LOG_TAG, "📥 Data: " + data.toString());

                String playerId = data.getString("playerId");
                int dice = data.getInt("dice");
                int tokenIndex = data.optInt("tokenIndex", -1);
                int from = data.optInt("from", -1);
                int to = data.optInt("to", -1);
                JSONObject game = data.getJSONObject("game");

                handleServerTurn(playerId, dice, tokenIndex, from, to, game);

            } catch (Exception e) {
                Log.e(LOG_TAG, "turnTaken error: " + e.getMessage());
                e.printStackTrace();
            }
        }));

        socket.on("turnSkipped", args -> runOnUiThread(() -> {
            try {
                Log.d(LOG_TAG, "📥 turnSkipped received!");
                JSONObject data = (JSONObject) args[0];

                String reason = data.optString("reason", "Not your turn");
                Log.d(LOG_TAG, "Skip reason: " + reason);

                JSONObject game = data.optJSONObject("game");
                if (game != null) {
                    updateTurnFromGame(game);
                }

                Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        socket.on("gameFinished", args -> runOnUiThread(() -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String winnerId = data.getString("winner");
                if (winnerId.equals(userId))
                    Toast.makeText(this, "🎉 You Win!", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "😢 You Lose!", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        socket.on("error", args -> runOnUiThread(() -> {
            Log.e(LOG_TAG, "❌ Socket Error: " + args[0].toString());
            Toast.makeText(this, "Error: " + args[0], Toast.LENGTH_SHORT).show();
        }));
    }
    private void handleServerTurn(String playerId, int dice, int tokenIndex, int from, int to, JSONObject game) {

        Player player = getPlayerById(playerId);
        if (player == null) {
            Log.e(LOG_TAG, "Player not found: " + playerId);
            return;
        }

        Log.d(LOG_TAG, "🎲 Server Dice: " + dice + " | Player: " + playerId);

        currentDiceValue = dice;

        ImageView diceView = diceViews[player.getColor().getIndex()];

        // Stop rolling animation
        stopDiceRollingAnimation(diceView);

        // Animate dice result, then move token
        animateDiceResult(diceView, dice, () -> {

            // Move token
            moveTokenFromServer(player, tokenIndex, from, to, dice);

            // Update turn after delay
            diceView.postDelayed(() -> updateTurnFromGame(game), 400);
        });
    }
    private void moveTokenFromServer(Player player, int tokenIndex, int from, int to, int dice) {

        if (tokenIndex < 0 || tokenIndex >= player.tokens.length) {
            Log.w(LOG_TAG, "Invalid token index: " + tokenIndex);
            return;
        }

        Token token = player.tokens[tokenIndex];

        if (from == -1 && dice == 6) {
            // Token coming out of home
            token.position = 0;
            View startCell = player.path[0];
            animateTokenToPosition(token, startCell);
            Log.d(LOG_TAG, "🏠 Token moved out of home");

        } else if (from >= 0) {
            // Normal movement on board
            animateTokenStep(player, token, from, dice);
            Log.d(LOG_TAG, "🚶 Token moved from " + from + " by " + dice + " steps");
        }
    }

    private void updateTurnFromGame(JSONObject game) {
        try {
            int currentIndex = game.getInt("currentPlayerIndex");

            // Get current player info
            JSONArray playersArray = game.optJSONArray("players");
            if (playersArray != null && currentIndex < playersArray.length()) {
                JSONObject currentPlayer = playersArray.getJSONObject(currentIndex);
                currentTurnPlayerId = currentPlayer.optString("userId");

                Log.d(LOG_TAG, "🎯 Turn: " + currentTurnPlayerId + " | MyTurn: " + currentTurnPlayerId.equals(userId));
            }

            // Update all dice visibility
            updateAllDiceVisibility();

        } catch (Exception e) {
            Log.e(LOG_TAG, "updateTurnFromGame error: " + e.getMessage());
        }
    }

    private void updateAllDiceVisibility() {

        for (PlayerColor color : PlayerColor.values()) {
            Player player = players[color.getIndex()];
            if (player == null) continue;

            boolean isThisPlayerTurn = player.userId != null &&
                    player.userId.equals(currentTurnPlayerId);

            ImageView dice = diceViews[color.getIndex()];

            // Stop any animation
            stopDiceRollingAnimation(dice);

            if (isThisPlayerTurn) {
                // Show dice
                showDiceForTurn(dice, player.userId.equals(userId));
            } else {
                // Hide dice
                hideDice(dice);
            }
        }
    }

    private void showDiceForTurn(ImageView dice, boolean isMyTurn) {
        dice.setVisibility(View.VISIBLE);
        dice.setAlpha(1f);
        dice.setEnabled(true);
        dice.setClickable(true);

        if (isMyTurn) {
            dice.setBackground(getDrawable(R.drawable.token_ring));
            Toast.makeText(this, "Your turn! 🎲", Toast.LENGTH_SHORT).show();
        } else {
            dice.setBackground(null);
        }
    }

    private void hideDice(ImageView dice) {
        dice.setVisibility(View.INVISIBLE);
        dice.setEnabled(false);
        dice.setClickable(false);
        dice.setBackground(null);
    }
    private void initializeSound() {
        if(diceSound == null){
            diceSound = MediaPlayer.create(this,R.raw.dice_roll_mp3);
            diceSound.setLooping(false);

            diceSound.setOnCompletionListener(mp->{
                Log.d(LOG_TAG,"Dice Sound Completed");
            });
        }
    }

    private void initializeScoreTextViews() {
        scoreTextViews[PlayerColor.BLUE.getIndex()] = ludoBinding.blueScore;
        scoreTextViews[PlayerColor.RED.getIndex()] = ludoBinding.redScore;
        scoreTextViews[PlayerColor.GREEN.getIndex()] = ludoBinding.greenScore;
        scoreTextViews[PlayerColor.YELLOW.getIndex()] = ludoBinding.yellowScore;

    }

    private void initializeDice() {
        // Map dice views
        diceViews[PlayerColor.YELLOW.getIndex()] = binding.leftDice;
        diceViews[PlayerColor.GREEN.getIndex()] = binding.topDiceLeft;
        diceViews[PlayerColor.RED.getIndex()] = binding.topDiceRight;
        diceViews[PlayerColor.BLUE.getIndex()] = binding.rightDice;

        // Enable + attach click
        for (PlayerColor color : PlayerColor.values()) {
            ImageView dice = diceViews[color.getIndex()];

            dice.setVisibility(View.INVISIBLE);
            dice.setEnabled(true);      // 🔥 REQUIRED
            dice.setClickable(true);    // 🔥 REQUIRED
            dice.setAlpha(1f);          // 🔥 Make visible

            final PlayerColor playerColor = color;
            dice.setOnClickListener(v -> rollDice(playerColor));
        }
    }

    private void initializePlayers(List<String> playerIds,List<String> playerNames, List<String> playerColors) {
        View[][] paths = {
                getYellowPath(), getGreenPath(), getRedPath(), getBluePath()
        };

        tokenViews = new ImageView[][]{
                {ludoBinding.yellowToken1, ludoBinding.yellowToken2, ludoBinding.yellowToken3, ludoBinding.yellowToken4},
                {ludoBinding.greenToken1, ludoBinding.greenToken2, ludoBinding.greenToken3, ludoBinding.greenToken4},
                {ludoBinding.redToken1, ludoBinding.redToken2, ludoBinding.redToken3, ludoBinding.redToken4},
                {ludoBinding.blueToken1, ludoBinding.blueToken2, ludoBinding.blueToken3, ludoBinding.blueToken4}
        };

        if (playerIds == null || playerColors == null || playerIds.size() != playerColors.size()) {
            Log.e(LOG_TAG, "Invalid player data received from Intent");
            return;
        }

        for (int i = 0; i < playerIds.size(); i++) {
            String id = playerIds.get(i);
            String name = playerNames.get(i);
            String colorString = playerColors.get(i);

            try {
                PlayerColor color = PlayerColor.fromString(colorString);
                int index = color.getIndex();

                String playerId = id;
                String playerName = name;
                ImageView avatarview = avatarViews[index];

                if (id.equals(userId)) {
                    playerName = username;
                    Glide.with(this)
                            .load(photo)
                            .circleCrop()
                            .placeholder(R.drawable.ic_ring)
                            .into(avatarview);
                } else {
                    avatarview.setImageResource(R.drawable.ic_ring);
                }


                players[index] = new Player(color, paths[index]);
                players[index].setPlayerDetails(id, playerName);

                if (id.equals(userId)) {
                    Log.d(LOG_TAG, "aapka ID: " + id + " aur aapka color: " + color.getColorName());
                } else {
                    Log.d(LOG_TAG, "Opponent ID: " + id + " aur opponent ka color: " + color.getColorName());
                }

                if (playerInfoTextViews[index] != null) {
                    playerInfoTextViews[index].setText(playerName);
                }

                for (int j = 0; j < TOKEN_COUNT; j++) {
                    players[index].tokens[j] = new Token(tokenViews[index][j]);
                    final Player player = players[index];
                    final Token token = players[index].tokens[j];
                    final int tokenIndex = j;

                    token.view.setOnClickListener(v -> {
                        Log.d(LOG_TAG, "Token " + tokenIndex + "clicked for " + player.color.getColorName());
                        onTokenClicked(player, token);
                    });
                    token.view.setClickable(true);
                    token.view.setEnabled(true);
                    Log.d(LOG_TAG, "Token " + tokenIndex + " initialized for " + color.getColorName());

                }
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "Error: Unknown player color in Intent data: " + colorString);
            }
        }
    }

    private void hideUnusedPlayers(List<String> playerColors) {
        if (playerColors == null) return;
        boolean[] used = new boolean[4];

        for (String c : playerColors) {
            PlayerColor pc = PlayerColor.fromString(c);
            used[pc.getIndex()] = true;
        }

        for (PlayerColor pc : PlayerColor.values()) {
            if (!used[pc.getIndex()]) {
                avatarViews[pc.getIndex()].setVisibility(View.GONE);
                playerInfoTextViews[pc.getIndex()].setVisibility(View.GONE);
                diceViews[pc.getIndex()].setVisibility(View.GONE);
                scoreTextViews[pc.getIndex()].setVisibility(View.GONE);
                for (int j = 0; j < 4; j++) {
                    tokenViews[pc.getIndex()][j].setVisibility(View.GONE);
                }
            }
        }
    }


    private void initializePlayerInfoTextViews() {
        playerInfoTextViews[PlayerColor.YELLOW.getIndex()] = binding.bottomLeftPlayerName;
        playerInfoTextViews[PlayerColor.GREEN.getIndex()] = binding.leftPlayerName;
        playerInfoTextViews[PlayerColor.RED.getIndex()] = binding.rightPlayerName;
        playerInfoTextViews[PlayerColor.BLUE.getIndex()] = binding.bottomRightPlayerName;
    }

    //    private void rollDice(@NonNull PlayerColor playerColor) {
//        playDiceSound();
//
//        final ImageView diceView = diceViews[playerColor.getIndex()];
//
//        if (diceAnimator != null && diceAnimator.isRunning()) {
//            diceAnimator.cancel();
//        }
//
//        diceView.setClickable(false);
//
//        ObjectAnimator bounceUp =
//                ObjectAnimator.ofFloat(diceView, "translationY", -80f);
//        bounceUp.setDuration(150);
//
//        ObjectAnimator bounceDown =
//                ObjectAnimator.ofFloat(diceView, "translationY", 0f);
//        bounceDown.setDuration(300);
//        bounceDown.setInterpolator(new BounceInterpolator());
//
//        ObjectAnimator rotate =
//                ObjectAnimator.ofFloat(diceView, "rotation", 0f, 360f);
//        rotate.setDuration(600);
//
//        diceAnimator = new AnimatorSet();
//        diceAnimator.playSequentially(bounceUp, bounceDown);
//        diceAnimator.playTogether(rotate);
//
//        diceAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//
//                currentDiceValue = random.nextInt(6) + 1;
//                diceView.setImageResource(
//                        DICE_DRAWABLES[currentDiceValue - 1]
//                );
//
//                Log.d(LOG_TAG, playerColor + " rolled: " + currentDiceValue);
//
//                Player currentPlayer = players[playerColor.getIndex()];
//                if (currentPlayer != null) {
//                    checkForAutoMove(currentPlayer);
//                }
//
//                diceView.setClickable(true);
//            }
//        });
//
//        diceAnimator.start();
//    }

    private void rollDice(@NonNull PlayerColor playerColor) {
        Log.d(LOG_TAG, "🎲 ROLL DICE CLICKED! Color: " + playerColor.getColorName());

        if (gameId == null || gameId.isEmpty()) {
            Toast.makeText(this, "Game ID missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!socket.connected()) {
            Toast.makeText(this, "Connection lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageView diceView = diceViews[playerColor.getIndex()];

        // Disable dice
        diceView.setClickable(false);
        diceView.setEnabled(false);

        // Play sound
        playDiceSound();

        // Start rolling animation
        startDiceRollingAnimation(diceView);


        // Emit to server
        try {
            JSONObject obj = new JSONObject();
            obj.put("gameId", gameId);
            obj.put("userId", userId);
            socket.emit("takeTurn", obj);
            Log.d(LOG_TAG, "📤 Emitting takeTurn");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Socket emit error: " + e.getMessage());
            stopDiceRollingAnimation(diceView);
            diceView.setClickable(true);
            diceView.setEnabled(true);
        }
    }

    private void startDiceRollingAnimation(ImageView diceView) {
        // Cancel any existing animation
        stopDiceRollingAnimation(diceView);

        // Rotate animation (3 rotations only, NOT infinite)
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(diceView, "rotation", 0f, 360f);
        rotateAnimator.setDuration(400);
        rotateAnimator.setRepeatCount(3);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.start();

        // Store animator to cancel later
        diceView.setTag(R.id.dice_rolling_animator, rotateAnimator);

        // Show random faces during rolling
        showRandomFacesDuringRoll(diceView, 5); // 5 random faces
    }

    private void showRandomFacesDuringRoll(ImageView diceView, int count) {
        for (int i = 0; i < count; i++) {
            int delay = i * 80; // 80ms between each face
            diceView.postDelayed(() -> {
                int randomFace = random.nextInt(6) + 1;
                diceView.setImageResource(DICE_DRAWABLES[randomFace - 1]);
            }, delay);
        }
    }

    private void stopDiceRollingAnimation(ImageView diceView) {
        // Cancel ObjectAnimator
        Object tag = diceView.getTag(R.id.dice_rolling_animator);
        if (tag instanceof ObjectAnimator) {
            ((ObjectAnimator) tag).cancel();
            diceView.setTag(R.id.dice_rolling_animator, null);
        }

        // Cancel ViewPropertyAnimator
        diceView.animate().cancel();

        // Reset state
        diceView.setRotation(0f);
        diceView.setTranslationY(0f);
        diceView.setScaleX(1f);
        diceView.setScaleY(1f);
    }
    // Show continuous rolling animation while waiting for server
    private void showDiceRollingAnimation(ImageView diceView) {
        diceView.setClickable(false);

        // Rotate continuously
        ObjectAnimator rotate = ObjectAnimator.ofFloat(diceView, "rotation", 0f, 360f);
        rotate.setDuration(500);
        rotate.setRepeatCount(ObjectAnimator.INFINITE);
        rotate.start();

        // Store animator to cancel later
        diceView.setTag(rotate);

        // Change dice faces randomly
        Runnable diceChanger = new Runnable() {
            @Override
            public void run() {
                if (diceView.getTag() != null) {
                    int randomFace = random.nextInt(6) + 1;
                    diceView.setImageResource(DICE_DRAWABLES[randomFace - 1]);
                    diceView.postDelayed(this, 100);
                }
            }
        };
        diceView.post(diceChanger);
    }

    private void checkForAutoMove(Player currentPlayer) {
        Log.d(LOG_TAG, "═══════════════════════════════════════");
        Log.d(LOG_TAG, "🔄 CHECK FOR AUTO MOVE");
        Log.d(LOG_TAG, "Player: " + currentPlayer.color.getColorName());
        Log.d(LOG_TAG, "Dice Value: " + currentDiceValue);
        Log.d(LOG_TAG, "═══════════════════════════════════════");

        if (currentDiceValue == STARTING_DICE_VALUE) {
            List<Token> homeTokens = getHomeTokens(currentPlayer);
            List<Token> boardTokens = getBoardMovableTokens(currentPlayer);

            Log.d(LOG_TAG, "🏠 Home Tokens: " + homeTokens.size());
            Log.d(LOG_TAG, "🛤️ Board Tokens: " + boardTokens.size());

            // ✅ Case 1: Both home and board tokens available - let user choose
            if (!homeTokens.isEmpty() && !boardTokens.isEmpty()) {
                Log.d(LOG_TAG, "👆 Multiple options - enabling all tokens");
                enableSelectableTokens(currentPlayer, homeTokens, boardTokens);
                Toast.makeText(this, "Choose a token to move", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Case 2: Only home tokens - let user choose which one
            if (!homeTokens.isEmpty()) {
                Log.d(LOG_TAG, "🏠 Only home tokens - enabling for selection");
                enableSelectableTokens(currentPlayer, homeTokens, new ArrayList<>());
                Toast.makeText(this, "Choose a token to move out!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Case 3: Only one board token - auto move
            if (boardTokens.size() == 1) {
                Log.d(LOG_TAG, "🤖 Single board token - auto moving");
                performAutoMove(currentPlayer, boardTokens.get(0));
                return;
            }

            // ✅ Case 4: Multiple board tokens - let user choose
            if (boardTokens.size() > 1) {
                Log.d(LOG_TAG, "👆 Multiple board tokens - enabling for selection");
                enableSelectableTokens(currentPlayer, new ArrayList<>(), boardTokens);
                Toast.makeText(this, "Choose a token to move!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Case 5: No movable tokens
            Log.w(LOG_TAG, "⚠️ No movable tokens!");
            Toast.makeText(this, "No moves available", Toast.LENGTH_SHORT).show();

        } else {
            // Non-6 dice roll
            List<Token> movableTokens = getAllMovableTokens(currentPlayer);
            Log.d(LOG_TAG, "🎲 Non-6 roll - Movable tokens: " + movableTokens.size());

            if (movableTokens.isEmpty()) {
                Log.w(LOG_TAG, "⚠️ No tokens can move!");
                Toast.makeText(this, "No moves possible", Toast.LENGTH_SHORT).show();
                return;
            }

            if (movableTokens.size() == 1) {
                Log.d(LOG_TAG, "🤖 Single movable token - auto moving");
                performAutoMove(currentPlayer, movableTokens.get(0));
            } else {
                Log.d(LOG_TAG, "👆 Multiple movable tokens - enabling for selection");
                enableSelectableTokensForNonSix(currentPlayer, movableTokens);
                Toast.makeText(this, "Choose a token to move", Toast.LENGTH_SHORT).show();
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
        Log.d(LOG_TAG, "═══════════════════════════════════════");
        Log.d(LOG_TAG, "🔓 ENABLING SELECTABLE TOKENS");
        Log.d(LOG_TAG, "Home Tokens: " + homeTokens.size());
        Log.d(LOG_TAG, "Board Tokens: " + boardTokens.size());
        Log.d(LOG_TAG, "═══════════════════════════════════════");

        // ✅ First disable ALL tokens
        for (Token token : player.tokens) {
            if (token != null && token.view != null) {
                token.view.setEnabled(false);
                token.view.setClickable(false);
                token.view.setBackground(null);
            }
        }

        // ✅ Enable home tokens
        for (Token token : homeTokens) {
            token.view.setEnabled(true);
            token.view.setClickable(true);
            token.view.setBackground(getDrawable(R.drawable.token_ring));
            Log.d(LOG_TAG, "✅ HOME Token ENABLED at position: " + token.position);
        }

        // ✅ Enable board tokens
        for (Token token : boardTokens) {
            token.view.setEnabled(true);
            token.view.setClickable(true);
            token.view.setBackground(getDrawable(R.drawable.token_ring));
            Log.d(LOG_TAG, "✅ BOARD Token ENABLED at position: " + token.position);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void enableSelectableTokensForNonSix(Player player, List<Token> movableTokens) {
        Log.d(LOG_TAG, "═══════════════════════════════════════");
        Log.d(LOG_TAG, "🔓 ENABLING TOKENS FOR NON-6 MOVE");
        Log.d(LOG_TAG, "Movable Tokens: " + movableTokens.size());
        Log.d(LOG_TAG, "═══════════════════════════════════════");

        // ✅ First disable ALL tokens
        for (Token token : player.tokens) {
            if (token != null && token.view != null) {
                token.view.setEnabled(false);
                token.view.setClickable(false);
                token.view.setBackground(null);
            }
        }

        // ✅ Enable movable tokens
        for (Token token : movableTokens) {
            token.view.setEnabled(true);
            token.view.setClickable(true);
            token.view.setBackground(getDrawable(R.drawable.token_ring));
            Log.d(LOG_TAG, "✅ Token ENABLED at position: " + token.position);
        }
    }

    private void performAutoMove(Player player, @NonNull Token token) {
        token.view.postDelayed(() -> {
            int oldPos = token.position;
            if (oldPos == -1 && currentDiceValue == 6) {
                token.position = 0;
                View startCell = player.path[0];
                animateTokenToPosition(token, startCell);

                currentDiceValue = 0;
                player.setTokensEnabled(false);
                player.clearTokenHighlights();
                return;
            }
            int newPos = oldPos + currentDiceValue;
            if (newPos >= player.path.length) {
                Toast.makeText(this, "Move not possibe", Toast.LENGTH_SHORT).show();
                return;
            }
            animateTokenStep(player, token, oldPos, currentDiceValue);
            currentDiceValue = 0;
            player.setTokensEnabled(false);
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

    private void onTokenClicked(@NonNull Player player, @NonNull Token token) {
        Log.d(LOG_TAG, "═══════════════════════════════════════");
        Log.d(LOG_TAG, "🖱️ TOKEN CLICKED!");
        Log.d(LOG_TAG, "Player: " + player.color.getColorName());
        Log.d(LOG_TAG, "Token Position: " + token.position);
        Log.d(LOG_TAG, "Current Dice Value: " + currentDiceValue);
        Log.d(LOG_TAG, "Token Enabled: " + token.view.isEnabled());
        Log.d(LOG_TAG, "Token Clickable: " + token.view.isClickable());
        Log.d(LOG_TAG, "═══════════════════════════════════════");

        // ✅ Check if dice was rolled
        if (currentDiceValue == 0) {
            Log.w(LOG_TAG, "⚠️ Dice not rolled!");
            Toast.makeText(this, "Roll dice first", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Check if token can move
        if (!canTokenMove(player, token, currentDiceValue)) {
            Log.w(LOG_TAG, "❌ Token cannot move!");
            Toast.makeText(this, "This token cannot move", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Move the token
        Log.d(LOG_TAG, "✅ Moving token...");
        moveToken(player, token, currentDiceValue);

        // ✅ Reset state
        currentDiceValue = 0;
        player.setTokensEnabled(false);
        player.clearTokenHighlights();
    }

    private void moveToken(@NonNull Player player,
                           @NonNull Token token,
                           int steps) {

        View[] path = player.path;
        int oldPos = token.position;

        // ✅ Token in HOME → only move on 6
        if (oldPos == -1) {
            if (steps == 6) {
                token.position = 0;
                animateTokenToPosition(token, path[0]);
            }
            return;
        }

        // ✅ Token already on board (OPTIONAL: allow only 6)
        int newPos = oldPos + steps;
        if (newPos >= path.length) {
            Toast.makeText(this, "Move not possible", Toast.LENGTH_SHORT).show();
            return;
        }

        animateTokenStep(player, token, oldPos, steps);
    }

    private void animateTokenToPosition(Token token, View targetCell) {
        ensureTokenParenting(token);

        float[] coordinates = calculateTokenPosition(token, targetCell, 0, 1);

        token.view.animate()
                .x(coordinates[0])
                .y(coordinates[1])
                .setDuration(DICE_ANIMATION_DURATION * 2) // Slightly longer animation
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

        // Offset kam rakha for tight grouping
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
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Log.d(LOG_TAG, "🔊 playDiceSound() called from:");
        for (int i = 3; i < Math.min(6, stackTrace.length); i++) {
            Log.d(LOG_TAG, "   " + stackTrace[i].getMethodName() + "()");
        }
        if (diceSound != null) {
            if(diceSound.isPlaying()){
                diceSound.stop();
                try{
                    diceSound.prepare();
                }catch(Exception e){
                    Log.e(LOG_TAG,"Sound prepare error: "+e.getMessage());
                }
            }
            diceSound.seekTo(0);
            diceSound.start();
            Log.d(LOG_TAG,"Dice Sound Played");
        }
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

    }
}
