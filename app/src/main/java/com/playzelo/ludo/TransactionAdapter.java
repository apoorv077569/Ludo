package com.playzelo.ludo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<TransactionModel> transactionList;

    public TransactionAdapter(List<TransactionModel> list) {
        this.transactionList = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvAmount;

        public ViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_type);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }

    @Override
    public TransactionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TransactionModel t = transactionList.get(position);
        holder.tvType.setText(t.getType());       // example: "Success", "Failed", etc.
        holder.tvDate.setText(t.getDate());       // example: "2025-07-26"
        holder.tvAmount.setText("₹ " + t.getAmount()); // example: ₹ 100
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}
