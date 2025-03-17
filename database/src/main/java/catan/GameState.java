package catan;

import catan.util.DataTransferObject;
import org.json.JSONObject;

public class GameState implements DataTransferObject {
    private long id;
    private long gameId;
    private long turn;
    private String actionType;
    private String stateData;
    private long turnNumber;
    private JSONObject boardState;
    private Long winnerId;
    private JSONObject robberLocation;
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

    public GameState() {}

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

    public long getTurn() {
        return turn;
    }

    public void setTurn(long turn) {
        this.turn = turn;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getStateData() {
        return stateData;
    }

    public void setStateData(String stateData) {
        this.stateData = stateData;
    }

    public long getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(long turnNumber) {
        this.turnNumber = turnNumber;
    }

    public JSONObject getBoardState() {
        return boardState;
    }

    public void setBoardState(JSONObject boardState) {
        this.boardState = boardState;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public JSONObject getRobberLocation() {
        return robberLocation;
    }

    public void setRobberLocation(JSONObject robberLocation) {
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