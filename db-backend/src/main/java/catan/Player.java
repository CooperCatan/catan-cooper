package catan;

import java.util.*;

public class Player {
    private long accountId;
    private String color;
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
    private int knightsPlayed;
    private boolean longestRoad;
    private boolean largestArmy;
    private Map<String, Integer> resources;
    private Map<String, Integer> developmentCards;

    public Player() {
        // default constructor for json deserialization
        this.resources = new HashMap<>();
        this.developmentCards = new HashMap<>();
        // initialize individual resource counts to 0, matching the parameterized constructor
        this.wood = 0;
        this.brick = 0;
        this.sheep = 0;
        this.wheat = 0;
        this.ore = 0;
        // initialize resource map entries to 0 as well for consistency, 
        // though Jackson might overwrite these if present in JSON.
        // it's good practice for the object to be in a consistent state after default construction.
        this.resources.put("wood", 0);
        this.resources.put("brick", 0);
        this.resources.put("sheep", 0);
        this.resources.put("wheat", 0);
        this.resources.put("ore", 0);

        this.developmentCards.put("knight", 0);
        this.developmentCards.put("victoryPoint", 0);
        this.developmentCards.put("roadBuilding", 0);
        this.developmentCards.put("yearOfPlenty", 0);
        this.developmentCards.put("monopoly", 0);
        
        // Initialize other fields to sensible defaults if not handled by deserialization explicitly
        this.victoryPoints = 0;
        this.numSettlements = 0;
        this.numCities = 0;
        this.numRoads = 0;
        this.knightsPlayed = 0;
        this.longestRoad = false;
        this.largestArmy = false;
        this.hasLargestArmy = false; // Ensure these are also initialized
        this.hasLongestRoad = false; // Ensure these are also initialized
    }

    public Player(long accountId) {
        this.accountId = accountId;
        this.color = null;
        this.victoryPoints = 0;
        this.numSettlements = 0;
        this.numCities = 0;
        this.numRoads = 0;
        this.knightsPlayed = 0;
        this.longestRoad = false;
        this.largestArmy = false;
        this.resources = new HashMap<>();
        this.developmentCards = new HashMap<>();
        // initialize resources to 0
        this.resources.put("wood", 0);
        this.resources.put("brick", 0);
        this.resources.put("sheep", 0);
        this.resources.put("wheat", 0);
        this.resources.put("ore", 0);
        // initialize individual resource counts for direct access if needed
        this.wood = 0;
        this.brick = 0;
        this.sheep = 0;
        this.wheat = 0;
        this.ore = 0;
        // initialize dev cards to 0
        this.developmentCards.put("knight", 0);
        this.developmentCards.put("victoryPoint", 0);
        this.developmentCards.put("roadBuilding", 0);
        this.developmentCards.put("yearOfPlenty", 0);
        this.developmentCards.put("monopoly", 0);
        this.hasLargestArmy = false;
        this.hasLongestRoad = false;
    }

    // resource management
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

    public void addResource(String resourceType, int amount) {
        resourceType = resourceType.toLowerCase();
        this.resources.put(resourceType, this.resources.getOrDefault(resourceType, 0) + amount);
        // update individual counts
        switch (resourceType) {
            case "wood": this.wood += amount; break;
            case "brick": this.brick += amount; break;
            case "sheep": this.sheep += amount; break;
            case "wheat": this.wheat += amount; break;
            case "ore": this.ore += amount; break;
        }
    }

    public void deductResource(String resourceType, int amount) {
        resourceType = resourceType.toLowerCase();
        int currentAmount = this.resources.getOrDefault(resourceType, 0);
        this.resources.put(resourceType, Math.max(0, currentAmount - amount));
        // Update individual counts
        switch (resourceType) {
            case "wood": this.wood = Math.max(0, this.wood - amount); break;
            case "brick": this.brick = Math.max(0, this.brick - amount); break;
            case "sheep": this.sheep = Math.max(0, this.sheep - amount); break;
            case "wheat": this.wheat = Math.max(0, this.wheat - amount); break;
            case "ore": this.ore = Math.max(0, this.ore - amount); break;
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

    // dev card management
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

    // building management
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

    // vp
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

    public void addInitialResourcesFromSettlement(List<Hex> adjacentHexes) {
        if (adjacentHexes == null) {
            return;
        }
        System.out.println("[Player " + this.accountId + "] Granting initial resources from settlement. Adjacent hexes count: " + adjacentHexes.size());
        for (Hex hex : adjacentHexes) {
            if (hex != null && !"desert".equalsIgnoreCase(hex.getType())) {
                String resourceType = hex.getType().toLowerCase();
                addResource(resourceType, 1); // Add 1 of the hex's resource type
                System.out.println("[Player " + this.accountId + "] Received 1 " + resourceType + " from hex " + hex.getId());
            }
        }
    }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
} 