package com.playzelo.ludo;

public class TransactionModel {

    private String amount;
    private String date;
    private String type;

    public TransactionModel(String amount, String date, String type) {
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }
}
