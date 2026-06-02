package com.example.moodtradesimulator.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodtradesimulator.R;
import com.example.moodtradesimulator.model.HoldingItem;

import java.util.List;
import java.util.Locale;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.HoldingViewHolder> {

    private final List<HoldingItem> holdings;

    public PortfolioAdapter(List<HoldingItem> holdings) {
        this.holdings = holdings;
    }

    @NonNull
    @Override
    public HoldingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_holding, parent, false);
        return new HoldingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoldingViewHolder holder, int position) {
        HoldingItem item = holdings.get(position);
        holder.symbolText.setText(item.symbol);
        holder.quantityText.setText("Qty: " + item.quantity);
        holder.averageBuyPriceText.setText("Avg Buy: " + String.format(Locale.US, "$%.2f", item.averageBuyPrice));
        holder.priceText.setText("Current Price: " + String.format(Locale.US, "$%.2f", item.currentPrice));
        holder.valueText.setText("Value: " + String.format(Locale.US, "$%.2f", item.holdingValue));

        String profitLossText;
        if (item.profitLoss > 0) {
            profitLossText = "+" + String.format(Locale.US, "$%.2f", item.profitLoss);
            holder.profitLossText.setTextColor(Color.GREEN);
        } else if (item.profitLoss < 0) {
            profitLossText = "-" + String.format(Locale.US, "$%.2f", Math.abs(item.profitLoss));
            holder.profitLossText.setTextColor(Color.RED);
        } else {
            profitLossText = String.format(Locale.US, "$%.2f", 0.0);
            holder.profitLossText.setTextColor(Color.BLACK);
        }
        holder.profitLossText.setText("P/L: " + profitLossText);
    }

    @Override
    public int getItemCount() {
        return holdings.size();
    }

    static class HoldingViewHolder extends RecyclerView.ViewHolder {
        TextView symbolText;
        TextView quantityText;
        TextView averageBuyPriceText;
        TextView priceText;
        TextView valueText;
        TextView profitLossText;

        HoldingViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolText = itemView.findViewById(R.id.holdingSymbolText);
            quantityText = itemView.findViewById(R.id.holdingQuantityText);
            averageBuyPriceText = itemView.findViewById(R.id.holdingAverageBuyPriceText);
            priceText = itemView.findViewById(R.id.holdingPriceText);
            valueText = itemView.findViewById(R.id.holdingValueText);
            profitLossText = itemView.findViewById(R.id.holdingProfitLossText);
        }
    }
}
