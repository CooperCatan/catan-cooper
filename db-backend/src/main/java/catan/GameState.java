package catan;

import catan.util.DataTransferObject;
import com.fasterxml.jackson.databind.JsonNode;

public class GameState implements DataTransferObject {
    private long gameId;
    private long turnNumber;
    private JsonNode boardState;
    private Long winnerId;  
    private JsonNode robberLocation;
    private boolean isGameOver;
    private long bankBrick;
    private long bankOre;
    private long bankSheep;
    private long bankWheat;
    private long bankWood;
    private long bankYearOfPlenty;
    private long bankMonopoly;
    private long bankRoadBuilding;
    private long bankVictoryPoint;
    private long bankKnight;

    public GameState() {
        this.boardState = null;  
        this.robberLocation = null;  
        this.isGameOver = false;
        this.bankBrick = 19;
        this.bankOre = 19;
        this.bankSheep = 19;
        this.bankWheat = 19;
        this.bankWood = 19;
        this.bankYearOfPlenty = 2;
        this.bankMonopoly = 2;
        this.bankRoadBuilding = 2;
        this.bankVictoryPoint = 5;
        this.bankKnight = 14;
    }

    @Override
    public long getId() {
        return gameId;
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

    public JsonNode getBoardState() {
        return boardState;
    }

    public void setBoardState(JsonNode boardState) {
        this.boardState = boardState;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public JsonNode getRobberLocation() {
        return robberLocation;
    }

    public void setRobberLocation(JsonNode robberLocation) {
        this.robberLocation = robberLocation;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public long getBankBrick() {
        return bankBrick;
    }

    public void setBankBrick(long bankBrick) {
        this.bankBrick = bankBrick;
    }

    public long getBankOre() {
        return bankOre;
    }

    public void setBankOre(long bankOre) {
        this.bankOre = bankOre;
    }

    public long getBankSheep() {
        return bankSheep;
    }

    public void setBankSheep(long bankSheep) {
        this.bankSheep = bankSheep;
    }

    public long getBankWheat() {
        return bankWheat;
    }

    public void setBankWheat(long bankWheat) {
        this.bankWheat = bankWheat;
    }

    public long getBankWood() {
        return bankWood;
    }

    public void setBankWood(long bankWood) {
        this.bankWood = bankWood;
    }

    public long getBankYearOfPlenty() {
        return bankYearOfPlenty;
    }

    public void setBankYearOfPlenty(long bankYearOfPlenty) {
        this.bankYearOfPlenty = bankYearOfPlenty;
    }

    public long getBankMonopoly() {
        return bankMonopoly;
    }

    public void setBankMonopoly(long bankMonopoly) {
        this.bankMonopoly = bankMonopoly;
    }

    public long getBankRoadBuilding() {
        return bankRoadBuilding;
    }

    public void setBankRoadBuilding(long bankRoadBuilding) {
        this.bankRoadBuilding = bankRoadBuilding;
    }

    public long getBankVictoryPoint() {
        return bankVictoryPoint;
    }

    public void setBankVictoryPoint(long bankVictoryPoint) {
        this.bankVictoryPoint = bankVictoryPoint;
    }

    public long getBankKnight() {
        return bankKnight;
    }

    public void setBankKnight(long bankKnight) {
        this.bankKnight = bankKnight;
    }
} 