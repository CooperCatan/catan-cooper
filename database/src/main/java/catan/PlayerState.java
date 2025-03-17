package catan;

import catan.util.DataTransferObject;

public class PlayerState implements DataTransferObject {
    private long id;
    private long gameId;
    private long playerId;
    private String resource;
    private int amount;

    public PlayerState() {}

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public long getAccountId() { return id; }
    public void setAccountId(long accountId) { this.id = accountId; }

    public long getTurnNumber() { return playerId; }
    public void setTurnNumber(long turnNumber) { this.playerId = turnNumber; }

    public long getOre() { return amount; }
    public void setOre(long ore) { this.amount = (int) ore; }

    public long getSheep() { return amount; }
    public void setSheep(long sheep) { this.amount = (int) sheep; }

    public long getWheat() { return amount; }
    public void setWheat(long wheat) { this.amount = (int) wheat; }

    public long getWood() { return amount; }
    public void setWood(long wood) { this.amount = (int) wood; }

    public long getBrick() { return amount; }
    public void setBrick(long brick) { this.amount = (int) brick; }

    public long getVictoryPoint() { return amount; }
    public void setVictoryPoint(long victoryPoint) { this.amount = (int) victoryPoint; }

    public long getKnight() { return amount; }
    public void setKnight(long knight) { this.amount = (int) knight; }

    public long getMonopoly() { return amount; }
    public void setMonopoly(long monopoly) { this.amount = (int) monopoly; }

    public long getYearOfPlenty() { return amount; }
    public void setYearOfPlenty(long yearOfPlenty) { this.amount = (int) yearOfPlenty; }

    public long getRoadBuilding() { return amount; }
    public void setRoadBuilding(long roadBuilding) { this.amount = (int) roadBuilding; }

    public long getNumSettlements() { return amount; }
    public void setNumSettlements(long numSettlements) { this.amount = (int) numSettlements; }

    public long getNumRoads() { return amount; }
    public void setNumRoads(long numRoads) { this.amount = (int) numRoads; }

    public long getNumCities() { return amount; }
    public void setNumCities(long numCities) { this.amount = (int) numCities; }

    public long getNumLongestContinuousRoad() { return amount; }
    public void setNumLongestContinuousRoad(long numLongestContinuousRoad) { this.amount = (int) numLongestContinuousRoad; }

    public boolean isLargestArmy() { return amount == 1; }
    public void setLargestArmy(boolean largestArmy) { this.amount = largestArmy ? 1 : 0; }

    public boolean isLongestRoad() { return amount == 1; }
    public void setLongestRoad(boolean longestRoad) { this.amount = longestRoad ? 1 : 0; }
} 