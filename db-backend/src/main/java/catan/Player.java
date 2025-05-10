package catan;

import java.util.*;

public class Player {
    private long accountId;
    private int ore;
    private int sheep;
    private int wheat;
    private int wood;
    private int brick;
    private int victoryPoints;
    private int numSettlements;
    private int numCities;
    private int numRoads;
    private int knightCards;
    private int knightUsed;
    private int yearOfPlentyCards;
    private int monopolyCards;
    private int roadBuildingCards;
    private int victoryPointCards;
    private boolean hasLargestArmy;
    private boolean hasLongestRoad;

    public Player(long accountId) {
        this.accountId = accountId;
        this.victoryPoints = 0;
        this.numSettlements = 0;
        this.numCities = 0;
        this.numRoads = 0;
        this.hasLargestArmy = false;
        this.hasLongestRoad = false;
    }

    // Resource Management
    public boolean hasResources(int brick, int ore, int sheep, int wheat, int wood) {
        return this.brick >= brick &&
               this.ore >= ore &&
               this.sheep >= sheep &&
               this.wheat >= wheat &&
               this.wood >= wood;
    }

    public void deductResources(int brick, int ore, int sheep, int wheat, int wood) {
        this.brick -= brick;
        this.ore -= ore;
        this.sheep -= sheep;
        this.wheat -= wheat;
        this.wood -= wood;
    }

    public void addResource(String resource, int amount) {
        switch (resource.toLowerCase()) {
            case "brick": brick += amount; break;
            case "ore": ore += amount; break;
            case "sheep": sheep += amount; break;
            case "wheat": wheat += amount; break;
            case "wood": wood += amount; break;
        }
    }

    public void deductResource(String resource, int amount) {
        switch (resource.toLowerCase()) {
            case "brick": brick -= amount; break;
            case "ore": ore -= amount; break;
            case "sheep": sheep -= amount; break;
            case "wheat": wheat -= amount; break;
            case "wood": wood -= amount; break;
        }
    }

    public int getResourceAmount(String resource) {
        switch (resource.toLowerCase()) {
            case "brick": return brick;
            case "ore": return ore;
            case "sheep": return sheep;
            case "wheat": return wheat;
            case "wood": return wood;
            default: return 0;
        }
    }

    public String getRandomResource() {
        List<String> availableResources = new ArrayList<>();
        if (brick > 0) availableResources.add("brick");
        if (ore > 0) availableResources.add("ore");
        if (sheep > 0) availableResources.add("sheep");
        if (wheat > 0) availableResources.add("wheat");
        if (wood > 0) availableResources.add("wood");

        if (availableResources.isEmpty()) {
            return null;
        }

        return availableResources.get(new Random().nextInt(availableResources.size()));
    }

    public int getTotalResources() {
        return brick + ore + sheep + wheat + wood;
    }

    // Development Card Management
    public void addDevelopmentCard(String cardType) {
        switch (cardType) {
            case "knight": knightCards++; break;
            case "yearOfPlenty": yearOfPlentyCards++; break;
            case "monopoly": monopolyCards++; break;
            case "roadBuilding": roadBuildingCards++; break;
            case "victoryPoint": 
                victoryPointCards++;
                victoryPoints++;
                break;
        }
    }

    public boolean hasKnight() { return knightCards > 0; }
    public void useKnight() {
        if (hasKnight()) {
            knightCards--;
            knightUsed++;
        }
    }

    public boolean hasYearOfPlenty() { return yearOfPlentyCards > 0; }
    public void useYearOfPlenty() {
        if (hasYearOfPlenty()) {
            yearOfPlentyCards--;
        }
    }

    public boolean hasMonopoly() { return monopolyCards > 0; }
    public void useMonopoly() {
        if (hasMonopoly()) {
            monopolyCards--;
        }
    }

    public boolean hasRoadBuilding() { return roadBuildingCards > 0; }
    public void useRoadBuilding() {
        if (hasRoadBuilding()) {
            roadBuildingCards--;
        }
    }

    // Building Management
    public void incrementSettlements() {
        numSettlements++;
    }

    public void decrementSettlements() {
        numSettlements--;
    }

    public void incrementCities() {
        numCities++;
    }

    public void decrementCities() {
        numCities--;
    }

    public void incrementRoads() {
        numRoads++;
    }

    public void decrementRoads() {
        numRoads--;
    }

    // Victory Point Management
    public void addVictoryPoint() {
        victoryPoints++;
    }

    public void addVictoryPoints(int points) {
        victoryPoints += points;
    }

    public void deductVictoryPoints(int points) {
        victoryPoints -= points;
    }

    public long getAccountId() { return accountId; }

    public int getOre() { return ore; }
    public void setOre(int ore) { this.ore = ore; }

    public int getSheep() { return sheep; }
    public void setSheep(int sheep) { this.sheep = sheep; }

    public int getWheat() { return wheat; }
    public void setWheat(int wheat) { this.wheat = wheat; }

    public int getWood() { return wood; }
    public void setWood(int wood) { this.wood = wood; }

    public int getBrick() { return brick; }
    public void setBrick(int brick) { this.brick = brick; }

    public int getVictoryPoints() { return victoryPoints; }
    public void setVictoryPoints(int victoryPoints) { this.victoryPoints = victoryPoints; }

    public int getNumSettlements() { return numSettlements; }
    public void setNumSettlements(int numSettlements) { this.numSettlements = numSettlements; }

    public int getNumCities() { return numCities; }
    public void setNumCities(int numCities) { this.numCities = numCities; }

    public int getNumRoads() { return numRoads; }
    public void setNumRoads(int numRoads) { this.numRoads = numRoads; }

    public int getKnightCards() { return knightCards; }
    public void setKnightCards(int knightCards) { this.knightCards = knightCards; }

    public int getKnightUsed() { return knightUsed; }
    public void setKnightUsed(int knightUsed) { this.knightUsed = knightUsed; }

    public int getYearOfPlentyCards() { return yearOfPlentyCards; }
    public void setYearOfPlentyCards(int yearOfPlentyCards) { this.yearOfPlentyCards = yearOfPlentyCards; }

    public int getMonopolyCards() { return monopolyCards; }
    public void setMonopolyCards(int monopolyCards) { this.monopolyCards = monopolyCards; }

    public int getRoadBuildingCards() { return roadBuildingCards; }
    public void setRoadBuildingCards(int roadBuildingCards) { this.roadBuildingCards = roadBuildingCards; }

    public int getVictoryPointCards() { return victoryPointCards; }
    public void setVictoryPointCards(int victoryPointCards) { this.victoryPointCards = victoryPointCards; }

    public boolean hasLargestArmy() { return hasLargestArmy; }
    public void setLargestArmy(boolean hasLargestArmy) { this.hasLargestArmy = hasLargestArmy; }

    public boolean hasLongestRoad() { return hasLongestRoad; }
    public void setLongestRoad(boolean hasLongestRoad) { this.hasLongestRoad = hasLongestRoad; }
} 