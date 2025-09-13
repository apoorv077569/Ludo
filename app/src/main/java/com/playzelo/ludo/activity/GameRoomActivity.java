package com.playzelo.ludo.activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.playzelo.ludo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameRoomActivity extends AppCompatActivity {

    TextView timerText, prizePoolText;
    GridLayout ludoGrid;
    ImageView topDiceLeft, topDiceRight, bottomDiceLeft, bottomDiceRight;
    MediaPlayer diceSound;
    Random random = new Random();

    ImageView[] redTokens, greenTokens, yellowTokens, blueTokens;
    int[] redPositions, greenPositions, yellowPositions, bluePositions;
    int lastRolledNumber = -1;
    String currentZone = "";
    int currentTurn = 0;

    int cellSize = 60;
    List<int[]> redPath = new ArrayList<>();
    List<int[]> greenPath = new ArrayList<>();
    List<int[]> yellowPath = new ArrayList<>();
    List<int[]> bluePath = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ludo_game_room);

        timerText = findViewById(R.id.timerText);
        prizePoolText = findViewById(R.id.prizePool);
        ludoGrid = findViewById(R.id.ludoGrid);

        topDiceLeft = findViewById(R.id.topDiceLeft);
        topDiceRight = findViewById(R.id.topDiceRight);
        bottomDiceLeft = findViewById(R.id.leftDice);
        bottomDiceRight = findViewById(R.id.rightDice);

        prizePoolText.setText("\u20B95.1");
        diceSound = MediaPlayer.create(this, com.playzelo.ludomodule.R.raw.dice_roll_mp3);

        redTokens = new ImageView[]{findViewById(R.id.red_goti_1), findViewById(R.id.red_goti_2), findViewById(R.id.red_goti_3), findViewById(R.id.red_goti_4)};
        greenTokens = new ImageView[]{findViewById(R.id.green_goti_1), findViewById(R.id.green_goti_2), findViewById(R.id.green_goti_3), findViewById(R.id.green_goti_4)};
        yellowTokens = new ImageView[]{findViewById(R.id.yellow_goti_1), findViewById(R.id.yellow_goti_2), findViewById(R.id.yellow_goti_3), findViewById(R.id.yellow_goti_4)};
        blueTokens = new ImageView[]{findViewById(R.id.blue_goti_1), findViewById(R.id.blue_goti_2), findViewById(R.id.blue_goti_3), findViewById(R.id.blue_goti_4)};

        redPositions = new int[4];
        greenPositions = new int[4];
        yellowPositions = new int[4];
        bluePositions = new int[4];

        generateLudoBoard();
        generatePaths();
        addTokensToGrid();
        setDiceClickListeners();
        startGameTimer();

        ConstraintLayout rootLayout = findViewById(R.id.rootLayout);
        if (rootLayout != null) {
            rootLayout.post(this::addZoneScoreLabels);
        }
    }

    private void generatePaths() {
        for (int i = 1; i <= 6; i++) greenPath.add(new int[]{6, i});
        for (int i = 2; i <= 7; i++) redPath.add(new int[]{i, 8});
        for (int i = 13; i >= 8; i--) yellowPath.add(new int[]{i, 6});
        for (int i = 13; i >= 8; i--) bluePath.add(new int[]{8, i});
    }

    private void moveTokenAlongPath(ImageView token, int[] positionArray, int index, List<int[]> pathList, int steps) {
        disableAllDice();
        int oldPos = positionArray[index];
        int newPos = oldPos + steps;
        if (newPos >= pathList.size()) return;

        positionArray[index] = newPos;
        int[] cell = pathList.get(newPos);
        int cellSizePx = ludoGrid.getWidth() / 15;
        float tx = cell[1] * cellSizePx;
        float ty = cell[0] * cellSizePx;
        token.animate().translationX(tx).translationY(ty).setDuration(300).start();
    }

    private void disableAllDice() {
        topDiceLeft.setEnabled(false);
        topDiceRight.setEnabled(false);
        bottomDiceLeft.setEnabled(false);
        bottomDiceRight.setEnabled(false);
    }

    private void setDiceClickListeners() {
        ImageView[] diceArray = {topDiceLeft, topDiceRight, bottomDiceLeft, bottomDiceRight};
        for (ImageView dice : diceArray) {
            dice.setOnClickListener(v -> rollDice(dice));
        }
    }

    private void rollDice(ImageView diceView) {
        diceView.setEnabled(false);
        if (diceSound != null) diceSound.start();
        diceView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dice_bounce));

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(100);
            }
        }

        new Handler().postDelayed(() -> {
            lastRolledNumber = random.nextInt(6) + 1;
            int resId = getResources().getIdentifier("dice_" + lastRolledNumber, "drawable", getPackageName());
            diceView.setImageResource(resId != 0 ? resId : R.drawable.ic_dice);
            diceView.setEnabled(true);

            if (diceView == topDiceLeft) {
                currentZone = "green";
                enableTokenClicks(greenTokens, greenPositions, greenPath);
            } else if (diceView == topDiceRight) {
                currentZone = "yellow";
                enableTokenClicks(yellowTokens, yellowPositions, yellowPath);
            } else if (diceView == bottomDiceLeft) {
                currentZone = "red";
                enableTokenClicks(redTokens, redPositions, redPath);
            } else if (diceView == bottomDiceRight) {
                currentZone = "blue";
                enableTokenClicks(blueTokens, bluePositions, bluePath);
            }

        }, 500);
    }

    private void enableTokenClicks(ImageView[] tokens, int[] positions, List<int[]> path) {
        for (int i = 0; i < tokens.length; i++) {
            int index = i;
            tokens[i].setOnClickListener(v -> {
                for (ImageView token : tokens) token.setBackground(null);
                tokens[index].setBackgroundResource(R.drawable.token_border_yellow);
                moveTokenAlongPath(tokens[index], positions, index, path, lastRolledNumber);
            });
        }
    }

    private void setDiceAvailabilityByTurn() {
        disableAllDice();
        switch (currentTurn) {
            case 0: topDiceLeft.setEnabled(true); break;
            case 1: topDiceRight.setEnabled(true); break;
            case 2: bottomDiceLeft.setEnabled(true); break;
            case 3: bottomDiceRight.setEnabled(true); break;
        }
    }

    private void addTokensToGrid() {
        addTokenToGrid(1, 6, R.drawable.green_token);
        addTokenToGrid(6, 13, R.drawable.red_token);
        addTokenToGrid(8, 1, R.drawable.yellow_token);
        addTokenToGrid(13, 8, R.drawable.blue_token);
    }

    private void addTokenToGrid(int row, int col, int drawableRes) {
        GridLayout.LayoutParams tokenParams = new GridLayout.LayoutParams();
        tokenParams.rowSpec = GridLayout.spec(row, 1f);
        tokenParams.columnSpec = GridLayout.spec(col, 1f);
        tokenParams.width = 0;
        tokenParams.height = 0;
        tokenParams.setGravity(Gravity.CENTER);

        ImageView token = new ImageView(this);
        token.setImageResource(drawableRes);
        token.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        token.setLayoutParams(tokenParams);

        ludoGrid.addView(token);
    }

    private void disableAllTokenClicks() {
        ImageView[][] allTokens = {redTokens, greenTokens, yellowTokens, blueTokens};
        for (ImageView[] tokenSet : allTokens) {
            for (ImageView token : tokenSet) {
                token.setOnClickListener(null);
            }
        }
    }

    private void startGameTimer() {
        new CountDownTimer(8 * 60 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                long mins = millisUntilFinished / 60000;
                long secs = (millisUntilFinished % 60000) / 1000;
                timerText.setText(String.format("%02d:%02d", mins, secs));
            }

            public void onFinish() {
                timerText.setText("00:00");
            }
        }.start();
    }
    private void generateLudoBoard() {
        int totalRows = 15;
        int totalCols = 15;
        ludoGrid.setRowCount(totalRows);
        ludoGrid.setColumnCount(totalCols);

        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalCols; col++) {
                View cell;
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.rowSpec = GridLayout.spec(row, 1f);
                params.columnSpec = GridLayout.spec(col, 1f);
                params.setGravity(Gravity.FILL);

                boolean isInsideHomeZone = (row < 6 && col < 6) || (row < 6 && col > 8)
                        || (row > 8 && col < 6) || (row > 8 && col > 8);

                boolean isSafeBlock = (row == 2 && col == 6) || (row == 6 && col == 12)
                        || (row == 8 && col == 2) || (row == 12 && col == 8);

                boolean isEntryBlock = (row == 6 && col == 1) || (row == 1 && col == 8)
                        || (row == 13 && col == 6) || (row == 8 && col == 13);

                boolean isPreEntryBlock = (row == 6 && col == 5) || (row == 5 && col == 8)
                        || (row == 8 && col == 9) || (row == 9 && col == 6);

                if (isInsideHomeZone) {
                    params.setMargins(0, 0, 0, 0);
                } else {
                    params.setMargins(1, 1, 1, 1);
                }

                cell = new View(this);
                cell.setLayoutParams(params);

                if (isSafeBlock) {
                    ImageView imageCell = new ImageView(this);
                    imageCell.setLayoutParams(params);
                    imageCell.setImageResource(R.drawable.ic_star);
                    imageCell.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    imageCell.setBackgroundColor(Color.WHITE);
                    cell = imageCell;

                } else if (isEntryBlock) {
                    if (row == 6 && col == 1)
                        cell.setBackgroundColor(Color.parseColor("#1E90FF"));
                    else if (row == 1 && col == 8)
                        cell.setBackgroundColor(Color.parseColor("#FF3030"));
                    else if (row == 13 && col == 6)
                        cell.setBackgroundColor(Color.parseColor("#FFD700"));
                    else if (row == 8 && col == 13)
                        cell.setBackgroundColor(Color.parseColor("#32CD32"));

                } else if (isPreEntryBlock) {
                    cell.setBackgroundColor(Color.WHITE);

                } else if ((row < 6 && col < 6)) {
                    cell.setBackgroundColor(Color.parseColor("#1E90FF"));
                } else if ((row < 6 && col > 8)) {
                    cell.setBackgroundColor(Color.parseColor("#FF3030"));
                } else if ((row > 8 && col < 6)) {
                    cell.setBackgroundColor(Color.parseColor("#FFD700"));
                } else if ((row > 8 && col > 8)) {
                    cell.setBackgroundColor(Color.parseColor("#32CD32"));
                } else if (col == 7 && row >= 1 && row <= 5) {
                    cell.setBackgroundColor(Color.parseColor("#1E90FF"));
                } else if (row == 7 && col >= 9 && col <= 13) {
                    cell.setBackgroundColor(Color.parseColor("#FF3030"));
                } else if (row == 7 && col >= 1 && col <= 5) {
                    cell.setBackgroundColor(Color.parseColor("#FFD700"));
                } else if (col == 7 && row >= 9 && row <= 13) {
                    cell.setBackgroundColor(Color.parseColor("#32CD32"));
                } else if (row >= 6 && row <= 8 && col >= 6 && col <= 8) {
                    cell.setBackgroundColor(Color.WHITE);
                } else if ((col == 6 || col == 8) && row >= 0 && row <= 14) {
                    cell.setBackgroundColor(Color.WHITE);
                } else if ((row == 6 || row == 8) && col >= 0 && col <= 14) {
                    cell.setBackgroundColor(Color.WHITE);
                } else {
                    cell.setBackgroundColor(Color.WHITE);
                }

                ludoGrid.addView(cell);
            }
        }
    }

    private void addZoneScoreLabels() {
        addScoreCircleToZone(findViewById(R.id.zoneRed), "15");
        addScoreCircleToZone(findViewById(R.id.zoneBlue), "8");
        addScoreCircleToZone(findViewById(R.id.zoneGreen), "12");
        addScoreCircleToZone(findViewById(R.id.zoneYellow), "20");
    }

    private void addScoreCircleToZone(View parentView, String score) {
        if (parentView instanceof ViewGroup) {
            FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(dpToPx(70), dpToPx(70));
            circleParams.gravity = Gravity.CENTER;

            TextView scoreText = new TextView(this);
            scoreText.setLayoutParams(circleParams);
            scoreText.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_white_bg));
            scoreText.setText(score);
            scoreText.setTextColor(Color.BLACK);
            scoreText.setGravity(Gravity.CENTER);
            scoreText.setTextSize(18);
            scoreText.setTypeface(Typeface.DEFAULT_BOLD);

            ((ViewGroup) parentView).addView(scoreText);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
