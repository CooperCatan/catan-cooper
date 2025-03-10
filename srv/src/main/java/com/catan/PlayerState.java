package com.catan.dao.dto;

import com.catan.dao.util.DataTransferObject;

public class PlayerState implements DataTransferObject {
    private long accountId;
    private long gameId;
    private long turnNumber;
    private long ore;
    private long sheep;
    private long wheat;
    private long wood;
    private long brick;
    private long victoryPoint;
    private long knight;
    private long monopoly;
    private long yearOfPlenty;
    private long roadBuilding;
    private long numSettlements;
    private long numRoads;
    private long numCities;
    private long numLongestContinuousRoad;
    private boolean largestArmy;
    private boolean longestRoad;

    @Override
    public long getId() {
        return accountId;
    }

    // Getters and Setters for all fields
    // ... (implement all getters and setters)
} 