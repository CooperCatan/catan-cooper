package com.catan.dao.dto;

import com.catan.dao.util.DataTransferObject;

public class Trade implements DataTransferObject {
    private long tradeId;
    private long gameId;
    private long turnNumber;
    private long fromPlayerId;
    private long toPlayerId;
    private String givenResource;
    private long givenAmount;
    private String receivedResource;
    private long receivedAmount;
    private boolean isAccepted;

    @Override
    public long getId() {
        return tradeId;
    }

    // Getters and Setters
    // ... (implement all getters and setters)
} 