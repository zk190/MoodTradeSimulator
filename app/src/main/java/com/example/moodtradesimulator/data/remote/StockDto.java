package com.example.moodtradesimulator.data.remote;

import com.google.gson.annotations.SerializedName;

public class StockDto {
    @SerializedName("symbol")
    public String symbol;

    @SerializedName("name")
    public String name;

    @SerializedName("close")
    public String close;

    @SerializedName("percent_change")
    public String percent_change;

    @SerializedName("code")
    public Integer code;

    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;
}
