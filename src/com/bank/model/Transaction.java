package com.bank.model;

import java.sql.Timestamp;

public class Transaction {
    public static final String DEPOSIT      = "DEPOSIT";
    public static final String WITHDRAW     = "WITHDRAW";
    public static final String TRANSFER_IN  = "TRANSFER_IN";
    public static final String TRANSFER_OUT = "TRANSFER_OUT";

    private int txnId;
    private final int accNo;
    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final Integer relatedAcc;   // null unless the row is part of a transfer
    private Timestamp txnTime;

    /** For a new row (txnId and txnTime are filled in by the database). */
    public Transaction(int accNo, String type, double amount, double balanceAfter, Integer relatedAcc) {
        this.accNo = accNo;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.relatedAcc = relatedAcc;
    }

    /** For a row read from the database. */
    public Transaction(int txnId, int accNo, String type, double amount,
                       double balanceAfter, Integer relatedAcc, Timestamp txnTime) {
        this(accNo, type, amount, balanceAfter, relatedAcc);
        this.txnId = txnId;
        this.txnTime = txnTime;
    }

    public int getTxnId()           { return txnId; }
    public int getAccNo()           { return accNo; }
    public String getType()         { return type; }
    public double getAmount()       { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public Integer getRelatedAcc()  { return relatedAcc; }
    public Timestamp getTxnTime()   { return txnTime; }

    @Override
    public String toString() {
        return String.format("#%-5d %-13s %10.2f %12.2f  %-6s %s",
                txnId, type, amount, balanceAfter,
                relatedAcc == null ? "-" : relatedAcc.toString(), txnTime);
    }
}
