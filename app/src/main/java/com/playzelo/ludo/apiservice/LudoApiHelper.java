package com.playzelo.ludo.apiservice;

import android.text.TextUtils;
import android.util.Log;

import com.playzelo.ludo.models.PlayerDto;
import com.playzelo.ludo.models.RoomDto;

import java.util.List;

public class LudoApiHelper {
    public void logRoomPlayers(String tagPrefix, RoomDto room, String currentUserId) {
        if (room == null) {
            Log.d(tagPrefix, "room is full");
            return;
        }
        Log.d(tagPrefix,
                "roomId=" + room.getRoomId()
                        + ", type=" + room.getType()
                        + ", status=" + room.getStatus()
        );
        List<PlayerDto> players = room.getPlayers();
        if (players == null || players.isEmpty()) {
            Log.d(tagPrefix, "No Players in room");
            return;
        }
        StringBuilder oppNames = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            PlayerDto p = players.get(i);
            boolean isYou = TextUtils.equals(p.getUserId(), currentUserId);
            String role = isYou ? "You" : "Opponent";
            Log.d(tagPrefix + "_PLAYERS",
                    "index=" + i +
                            ", role=" + role +
                            ", userId=" + p.getUserId() +
                            ", username=" + p.getUsername());

            if (!isYou) {
                if (oppNames.length() > 0) oppNames.append(", ");
                oppNames.append(p.getUsername())
                        .append(" (").append(p.getUserId()).append(")");
            }
        }

        Log.d(tagPrefix + "_OPPONENTS", "Opponents: " + oppNames);
    }

}

