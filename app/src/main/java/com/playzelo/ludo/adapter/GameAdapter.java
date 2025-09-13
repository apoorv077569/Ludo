package com.playzelo.ludo.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.playzelo.ludo.models.GameModel;
import com.playzelo.ludo.R;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<GameModel> gameList;

    public GameAdapter(List<GameModel> gameList) {
        this.gameList = gameList;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_ludo_item, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameModel game = gameList.get(position);

        holder.txtTitle.setText(game.getTitle());
        holder.txtPrizePool.setText("Prize Pool: " + game.getPrizePool());
        holder.txtJoined.setText(game.getJoinedPlayers());
        holder.btnJoin.setText("Join for " + game.getEntryFee());

        holder.btnJoin.setOnClickListener(v ->
                // handle join logic
                Toast.makeText(holder.itemView.getContext(), "Joined " + game.getTitle(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtPrizePool, txtJoined;
        Button btnJoin;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtPrizePool = itemView.findViewById(R.id.txt_prize_pool);
            txtJoined = itemView.findViewById(R.id.txt_joined);
            btnJoin = itemView.findViewById(R.id.btn_join);
        }
    }
}
