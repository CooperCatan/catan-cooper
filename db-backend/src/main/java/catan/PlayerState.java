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
    private long largestArmy;
    private long longestRoad;

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

    public long getKnightUsed() { return knightUsed; }
    public void setKnightUsed(long knightUsed) { this.knightUsed = knightUsed; }

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

    public long isLargestArmy() { return largestArmy; }
    public void setLargestArmy(long largestArmy) { this.largestArmy = largestArmy; }

    public long isLongestRoad() { return longestRoad; }
    public void setLongestRoad(long longestRoad) { this.longestRoad = longestRoad; }

    public boolean checkSettlement() {
        //Check if the resources for a Settlement can be subtracted
        return this.getBrick() >= 1 && this.getSheep() >= 1 && this.getWheat() >= 1 && this.getWood() >= 1;
    }

    public void paySettlement() {
        //On valid Settlement placement
        this.setWheat(this.getWheat() - 1);
        this.setBrick(this.getBrick() - 1);
        this.setSheep(this.getSheep() - 1);
        this.setWood(this.getWood() - 1);
    }

    public boolean checkCity() {
        //Check if the resources for a City can be subtracted
        return this.getOre() >= 3 && this.getWheat() >= 2;
    }

    public void payCity() {
        //On valid City placement
        this.setOre(this.getOre() - 3);
        this.setWheat(this.getWheat() - 2);
    }

    public boolean checkRoad() {
        //Check if the resources for a Road can be subtracted
        return this.getBrick() >= 1 && this.getWood() >= 1;
    }

    public void payRoad() {
        //On valid road placement
        this.setBrick(this.getBrick() - 1);
        this.setWood(this.getWood() - 1);
    }

    public boolean checkCard() {
        //Check if the resources for a card can be subtracted
        return this.getOre() >= 1 && this.getSheep() >= 1 && this.getWheat() >= 1;
    }

    public void payCard() {
        //This function is called when card draw is valid
        this.setWheat(this.getWheat() - 1);
        this.setOre(this.getOre() - 1);
        this.setSheep(this.getSheep() - 1);
    }

    public int useCard() {
        return 1;
    }
}