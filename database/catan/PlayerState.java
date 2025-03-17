package catan;

import catan.util.DataTransferObject;

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
    private long knightUsed;
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

    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    public long getGameId() { return gameId; }
    public void setGameId(long gameId) { this.gameId = gameId; }

    public long getTurnNumber() { return turnNumber; }
    public void setTurnNumber(long turnNumber) { this.turnNumber = turnNumber; }

    public long getOre() { return ore; }
    public void setOre(long ore) { this.ore = ore; }

    public long getSheep() { return sheep; }
    public void setSheep(long sheep) { this.sheep = sheep; }

    public long getWheat() { return wheat; }
    public void setWheat(long wheat) { this.wheat = wheat; }

    public long getWood() { return wood; }
    public void setWood(long wood) { this.wood = wood; }

    public long getBrick() { return brick; }
    public void setBrick(long brick) { this.brick = brick; }

    public long getVictoryPoint() { return victoryPoint; }
    public void setVictoryPoint(long victoryPoint) { this.victoryPoint = victoryPoint; }

    public long getKnight() { return knight; }
    public void setKnight(long knight) { this.knight = knight; }

    public long getMonopoly() { return monopoly; }
    public void setMonopoly(long monopoly) { this.monopoly = monopoly; }

    public long getYearOfPlenty() { return yearOfPlenty; }
    public void setYearOfPlenty(long yearOfPlenty) { this.yearOfPlenty = yearOfPlenty; }

    public long getRoadBuilding() { return roadBuilding; }
    public void setRoadBuilding(long roadBuilding) { this.roadBuilding = roadBuilding; }

    public long getNumSettlements() { return numSettlements; }
    public void setNumSettlements(long numSettlements) { this.numSettlements = numSettlements; }

    public long getNumRoads() { return numRoads; }
    public void setNumRoads(long numRoads) { this.numRoads = numRoads; }

    public long getNumCities() { return numCities; }
    public void setNumCities(long numCities) { this.numCities = numCities; }

    public long getNumLongestContinuousRoad() { return numLongestContinuousRoad; }
    public void setNumLongestContinuousRoad(long numLongestContinuousRoad) { this.numLongestContinuousRoad = numLongestContinuousRoad; }

    public boolean isLargestArmy() { return largestArmy; }
    public void setLargestArmy(boolean largestArmy) { this.largestArmy = largestArmy; }

    public boolean isLongestRoad() { return longestRoad; }
    public void setLongestRoad(boolean longestRoad) { this.longestRoad = longestRoad; }


} 