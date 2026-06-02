package com.example.moodtradesimulator.ui;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodtradesimulator.R;
import com.example.moodtradesimulator.data.TradingRepository;
import com.example.moodtradesimulator.model.Stock;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final List<Stock> stocks;

    public StockAdapter(List<Stock> stocks) {
        this.stocks = stocks;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stocks.get(position);
        holder.symbolText.setText(stock.symbol);
        holder.nameText.setText(stock.name);
        holder.priceText.setText(String.format(Locale.US, "$%.2f", stock.price));
        int ownedShares = TradingRepository.getEngine()
                .getPortfolio()
                .holdings
                .getOrDefault(stock.symbol, 0);
        holder.ownedSharesText.setText("Owned: " + ownedShares);

        if (isUpwardTrend(stock.symbol)) {
            holder.priceText.setTextColor(Color.GREEN);
        } else {
            holder.priceText.setTextColor(Color.RED);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), StockDetailActivity.class);
            intent.putExtra("symbol", stock.symbol);
            intent.putExtra("name", stock.name);
            intent.putExtra("price", stock.price);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    private boolean isUpwardTrend(String symbol) {
        Random random = new Random(symbol == null ? 0 : symbol.hashCode());

        float first = 100f;
        float current = first;

        for (int i = 0; i < 6; i++) {
            float move = (random.nextFloat() * 0.04f) - 0.02f; // -2% to +2%
            current = current * (1f + move);
        }

        return current >= first;
    }

    static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView symbolText;
        TextView nameText;
        TextView priceText;
        TextView ownedSharesText;

        StockViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolText = itemView.findViewById(R.id.symbolText);
            nameText = itemView.findViewById(R.id.nameText);
            priceText = itemView.findViewById(R.id.priceText);
            ownedSharesText = itemView.findViewById(R.id.ownedSharesText);
        }
    }
}
