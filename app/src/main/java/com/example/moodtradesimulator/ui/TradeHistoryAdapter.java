package com.example.moodtradesimulator.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodtradesimulator.R;
import com.example.moodtradesimulator.model.Trade;

import java.util.List;

public class TradeHistoryAdapter extends RecyclerView.Adapter<TradeHistoryAdapter.TradeViewHolder> {

    private final List<Trade> trades;

    public TradeHistoryAdapter(List<Trade> trades) {
        this.trades = trades;
    }

    @NonNull
    @Override
    public TradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trade, parent, false);
        return new TradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TradeViewHolder holder, int position) {
        Trade trade = trades.get(position);
        holder.symbolText.setText(trade.getSymbol());
        holder.typeText.setText(trade.getType());
        holder.quantityText.setText("Qty: " + trade.getQuantity());
    }

    @Override
    public int getItemCount() {
        return trades.size();
    }

    static class TradeViewHolder extends RecyclerView.ViewHolder {
        TextView symbolText;
        TextView typeText;
        TextView quantityText;

        TradeViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolText = itemView.findViewById(R.id.tradeSymbolText);
            typeText = itemView.findViewById(R.id.tradeTypeText);
            quantityText = itemView.findViewById(R.id.tradeQuantityText);
        }
    }
}
