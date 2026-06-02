package com.example.moodtradesimulator.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodtradesimulator.R;
import com.example.moodtradesimulator.data.remote.StockRepository;
import com.example.moodtradesimulator.model.Stock;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MarketsActivity extends AppCompatActivity {

    // Replace with your real Twelve Data API key
    private static final String API_KEY = "9c054d8682504c0c925a8038a0037cc1";

    private StockAdapter adapter;
    private EditText searchInput;
    private Spinner sortSpinner;
    private TextView lastRefreshedText;
    private StockRepository stockRepository;
    private List<Stock> originalStocks;
    private List<Stock> filteredStocks;
    private boolean showRefreshToast;
    private final String[] sortOptions = {
            "Symbol A-Z",
            "Price Low to High",
            "Price High to Low"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markets);

        searchInput = findViewById(R.id.searchInput);
        sortSpinner = findViewById(R.id.sortSpinner);
        lastRefreshedText = findViewById(R.id.lastRefreshedText);
        Button refreshPricesButton = findViewById(R.id.refreshPricesButton);
        RecyclerView marketsRecycler = findViewById(R.id.marketsRecycler);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        marketsRecycler.setLayoutManager(new LinearLayoutManager(this));

        stockRepository = new StockRepository(API_KEY);
        originalStocks = new ArrayList<>();
        filteredStocks = new ArrayList<>();
        adapter = new StockAdapter(filteredStocks);
        marketsRecycler.setAdapter(adapter);
        MainBottomNavHelper.setup(this, bottomNavigationView, R.id.navigation_markets);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                sortOptions
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSort(searchInput.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndSort(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        refreshPricesButton.setOnClickListener(v -> {
            showRefreshToast = true;
            loadStocks();
        });

        lastRefreshedText.setText("Last refreshed: Not yet");
        loadStocks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        filterAndSort(searchInput.getText().toString());
    }

    private void filterAndSort(String query) {
        String lowerQuery = query == null ? "" : query.trim().toLowerCase(Locale.US);

        filteredStocks.clear();
        for (Stock stock : originalStocks) {
            if (stock == null) {
                continue;
            }

            String symbol = safeText(stock.symbol).toLowerCase(Locale.US);
            String name = safeText(stock.name).toLowerCase(Locale.US);

            if (lowerQuery.isEmpty() || symbol.contains(lowerQuery) || name.contains(lowerQuery)) {
                filteredStocks.add(stock);
            }
        }

        int selectedSort = sortSpinner.getSelectedItemPosition();
        if (selectedSort == 0) {
            filteredStocks.sort((a, b) -> safeText(a.symbol).compareToIgnoreCase(safeText(b.symbol)));
        } else if (selectedSort == 1) {
            filteredStocks.sort((a, b) -> Double.compare(a.price, b.price));
        } else if (selectedSort == 2) {
            filteredStocks.sort((a, b) -> Double.compare(b.price, a.price));
        }

        adapter.notifyDataSetChanged();
    }

    private void loadStocks() {
        stockRepository.getStocks(new StockRepository.StocksCallback() {
            @Override
            public void onSuccess(List<Stock> stocks) {
                originalStocks.clear();
                originalStocks.addAll(stocks);
                filterAndSort(searchInput.getText().toString());

                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                lastRefreshedText.setText("Last refreshed: " + time);

                if (showRefreshToast) {
                    android.widget.Toast.makeText(
                            MarketsActivity.this,
                            "Stocks refreshed",
                            android.widget.Toast.LENGTH_SHORT
                    ).show();
                    showRefreshToast = false;
                }
            }

            @Override
            public void onError() {
                showRefreshToast = false;
                android.widget.Toast.makeText(
                        MarketsActivity.this,
                        "Failed to load stocks",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
