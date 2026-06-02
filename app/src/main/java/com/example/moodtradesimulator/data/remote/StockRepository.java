package com.example.moodtradesimulator.data.remote;

import android.util.Log;

import com.example.moodtradesimulator.model.Stock;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockRepository {

    private static final String TAG = "StockRepository";
    private static final String[] DEFAULT_SYMBOLS = {
            "AAPL", "TSLA", "MSFT", "AMZN",
            "GOOGL", "META", "NVDA", "NFLX"
    };
    private static final Map<String, Double> lastValidPrices = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Stock> lastValidStocks = Collections.synchronizedMap(new HashMap<>());
    private static final Gson GSON = new Gson();

    private final StockApiService apiService;
    private final String apiKey;

    public interface StocksCallback {
        void onSuccess(List<Stock> stocks);

        void onError();
    }

    public StockRepository(String apiKey) {
        this.apiService = RetrofitClient.getStockApiService();
        this.apiKey = apiKey;
    }

    public void getStocks(StocksCallback callback) {
        String symbolList = String.join(",", DEFAULT_SYMBOLS);

        apiService.getQuotes(symbolList, apiKey).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "Batch quote response code: " + response.code());

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(
                            TAG,
                            "Failed to fetch batch quotes: code="
                                    + response.code()
                                    + ", errorBody="
                                    + readErrorBody(response)
                    );
                    handleBatchFailure(callback);
                    return;
                }

                JsonObject responseBody = response.body();
                Log.d(TAG, "Batch quote response body: " + responseBody.toString());

                boolean batchError = isBatchError(responseBody);
                Log.d(TAG, "Batch API error detected: " + batchError);
                if (batchError) {
                    Log.e(TAG, "Batch API error: " + responseBody.toString());
                    handleBatchFailure(callback);
                    return;
                }

                List<Stock> stocks = new ArrayList<>();
                List<String> parsedSymbols = new ArrayList<>();
                List<String> failedSymbols = new ArrayList<>();
                for (String requestedSymbol : DEFAULT_SYMBOLS) {
                    Stock stock = parseBatchStock(responseBody, requestedSymbol);
                    if (stock != null) {
                        stocks.add(stock);
                        parsedSymbols.add(requestedSymbol);
                    } else {
                        failedSymbols.add(requestedSymbol);
                        Stock cachedStock = lastValidStocks.get(requestedSymbol);
                        if (cachedStock != null) {
                            Log.w(TAG, "Using cached stock for " + requestedSymbol + " because batch quote was invalid");
                            stocks.add(cachedStock);
                        }
                    }
                }

                Log.d(TAG, "Parsed symbols: " + parsedSymbols);
                Log.d(TAG, "Failed symbols: " + failedSymbols);

                if (stocks.isEmpty()) {
                    handleBatchFailure(callback);
                } else {
                    callback.onSuccess(stocks);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Network error while fetching batch quotes", t);
                handleBatchFailure(callback);
            }
        });
    }

    private void handleBatchFailure(StocksCallback callback) {
        List<Stock> cachedStocks = new ArrayList<>();
        for (String symbol : DEFAULT_SYMBOLS) {
            Stock cachedStock = lastValidStocks.get(symbol);
            if (cachedStock != null) {
                cachedStocks.add(cachedStock);
            }
        }

        if (cachedStocks.isEmpty()) {
            callback.onError();
        } else {
            callback.onSuccess(cachedStocks);
        }
    }

    private Stock parseBatchStock(JsonObject responseBody, String requestedSymbol) {
        JsonElement stockElement = responseBody.get(requestedSymbol);
        if (stockElement == null || !stockElement.isJsonObject()) {
            Log.w(TAG, "Failed to parse symbol " + requestedSymbol + ": no batch quote returned");
            return null;
        }

        StockDto dto = GSON.fromJson(stockElement, StockDto.class);

        if (isApiError(dto)) {
            Log.e(
                    TAG,
                    "Failed to parse symbol "
                            + requestedSymbol
                            + ": API returned error payload"
                            + ", symbol="
                            + dto.symbol
                            + ": status="
                            + dto.status
                            + ", code="
                            + dto.code
                            + ", message="
                            + dto.message
            );
            return null;
        }

        String safeSymbol = resolveSymbol(dto, requestedSymbol);
        String safeName = resolveName(dto, safeSymbol);

        Double parsedPrice = parsePrice(dto.close);
        boolean usedFallback = false;

        if (parsedPrice == null) {
            Double cachedPrice = lastValidPrices.get(safeSymbol);
            if (cachedPrice != null) {
                parsedPrice = cachedPrice;
                usedFallback = true;
            }
        }

        if (parsedPrice == null) {
            Log.w(TAG, "Failed to parse symbol " + requestedSymbol + ": invalid close value " + dto.close);
            return null;
        }

        Stock stock = new Stock(safeSymbol, safeName, parsedPrice);
        lastValidPrices.put(safeSymbol, parsedPrice);
        lastValidStocks.put(safeSymbol, stock);
        Log.d(TAG, "Parsed symbol " + requestedSymbol + " successfully with price " + stock.price);
        return stock;
    }

    private double parseDouble(String value) {
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid number: " + value, e);
            return 0.0;
        }
    }

    private Double parsePrice(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return null;
        }

        String normalizedValue = trimmedValue.replace(",", "");

        try {
            return Double.parseDouble(normalizedValue);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid close price: " + value, e);
            return null;
        }
    }

    private String resolveSymbol(StockDto dto, String fallbackSymbol) {
        String dtoSymbol = sanitizeText(dto == null ? null : dto.symbol);
        if (!dtoSymbol.isEmpty()) {
            return dtoSymbol;
        }

        String safeFallback = sanitizeText(fallbackSymbol);
        if (!safeFallback.isEmpty()) {
            return safeFallback;
        }

        return "UNKNOWN";
    }

    private String resolveName(StockDto dto, String safeSymbol) {
        String dtoName = sanitizeText(dto == null ? null : dto.name);
        if (!dtoName.isEmpty()) {
            return dtoName;
        }
        return getCompanyName(safeSymbol);
    }

    private String sanitizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String readErrorBody(Response<?> response) {
        if (response.errorBody() == null) {
            return "null";
        }

        try {
            return response.errorBody().string();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read error body", e);
            return "unreadable";
        }
    }

    private boolean isBatchError(JsonObject responseBody) {
        return responseBody.has("status")
                && responseBody.has("message")
                && !responseBody.has(DEFAULT_SYMBOLS[0]);
    }

    private String getCompanyName(String symbol) {
        if ("AAPL".equalsIgnoreCase(symbol)) {
            return "Apple Inc.";
        }
        if ("TSLA".equalsIgnoreCase(symbol)) {
            return "Tesla";
        }
        if ("MSFT".equalsIgnoreCase(symbol)) {
            return "Microsoft";
        }
        if ("AMZN".equalsIgnoreCase(symbol)) {
            return "Amazon";
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            return "UNKNOWN";
        }
        return symbol;
    }

    private boolean isApiError(StockDto dto) {
        if (dto == null) {
            return false;
        }

        if (dto.code != null) {
            return true;
        }

        return dto.status != null
                && !"ok".equalsIgnoreCase(dto.status)
                && !"success".equalsIgnoreCase(dto.status);
    }
}
