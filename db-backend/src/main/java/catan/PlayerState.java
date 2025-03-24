package catan;

import catan.util.DataTransferObject;

public class PlayerState implements DataTransferObject {
    private long accountId;
    private long gameId;
    private long turnNumber;
    private long handOre;
    private long handSheep;
    private long handWheat;
    private long handWood;
    private long handBrick;
    private long handVictoryPoint;
    private long handKnight;
    private long handMonopoly;
    private long handYearOfPlenty;
    private long handRoadBuilding;
    private long numSettlements;
    private long numRoads;
    private long numCities;
    private long numLongestContinuousRoad;
    private boolean largestArmy;
    private boolean longestRoad;

    public PlayerState() {
        this.handOre = 0;
        this.handSheep = 0;
        this.handWheat = 0;
        this.handWood = 0;
        this.handBrick = 0;
        this.handVictoryPoint = 0;
        this.handKnight = 0;
        this.handMonopoly = 0;
        this.handYearOfPlenty = 0;
        this.handRoadBuilding = 0;
        this.numSettlements = 0;
        this.numRoads = 0;
        this.numCities = 0;
        this.numLongestContinuousRoad = 0;
        this.largestArmy = false;
        this.longestRoad = false;
    }

    @Override
    public long getId() {
        return accountId; 
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(long turnNumber) {
        this.turnNumber = turnNumber;
    }

    public long getHandOre() {
        return handOre;
    }

    public void setHandOre(long handOre) {
        this.handOre = handOre;
    }

    public long getHandSheep() {
        return handSheep;
    }

    public void setHandSheep(long handSheep) {
        this.handSheep = handSheep;
    }

    public long getHandWheat() {
        return handWheat;
    }

    public void setHandWheat(long handWheat) {
        this.handWheat = handWheat;
    }

    public long getHandWood() {
        return handWood;
    }

    public void setHandWood(long handWood) {
        this.handWood = handWood;
    }

    public long getHandBrick() {
        return handBrick;
    }

    public void setHandBrick(long handBrick) {
        this.handBrick = handBrick;
    }

    public long getHandVictoryPoint() {
        return handVictoryPoint;
    }

    public void setHandVictoryPoint(long handVictoryPoint) {
        this.handVictoryPoint = handVictoryPoint;
    }

    public long getHandKnight() {
        return handKnight;
    }

    public void setHandKnight(long handKnight) {
        this.handKnight = handKnight;
    }

    public long getHandMonopoly() {
        return handMonopoly;
    }

    public void setHandMonopoly(long handMonopoly) {
        this.handMonopoly = handMonopoly;
    }

    public long getHandYearOfPlenty() {
        return handYearOfPlenty;
    }

    public void setHandYearOfPlenty(long handYearOfPlenty) {
        this.handYearOfPlenty = handYearOfPlenty;
    }

    public long getHandRoadBuilding() {
        return handRoadBuilding;
    }

    public void setHandRoadBuilding(long handRoadBuilding) {
        this.handRoadBuilding = handRoadBuilding;
    }

    public long getNumSettlements() {
        return numSettlements;
    }

    public void setNumSettlements(long numSettlements) {
        this.numSettlements = numSettlements;
    }

    public long getNumRoads() {
        return numRoads;
    }

    public void setNumRoads(long numRoads) {
        this.numRoads = numRoads;
    }

    public long getNumCities() {
        return numCities;
    }

    public void setNumCities(long numCities) {
        this.numCities = numCities;
    }

    public long getNumLongestContinuousRoad() {
        return numLongestContinuousRoad;
    }

    public void setNumLongestContinuousRoad(long numLongestContinuousRoad) {
        this.numLongestContinuousRoad = numLongestContinuousRoad;
    }

    public boolean isLargestArmy() {
        return largestArmy;
    }

    public void setLargestArmy(boolean largestArmy) {
        this.largestArmy = largestArmy;
    }

    public boolean isLongestRoad() {
        return longestRoad;
    }

    public void setLongestRoad(boolean longestRoad) {
        this.longestRoad = longestRoad;
    }
} 