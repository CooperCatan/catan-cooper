package catan;

import catan.util.DataTransferObject;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

public class Game implements DataTransferObject {
    private long gameId;
    private List<Long> playerList;
    private Long winnerId;
    private boolean isGameOver;
    private boolean inProgress;
    private Timestamp createdAt;
    private String gameName;
    private String jsonHexes;
    private String jsonVertices;
    private String jsonEdges;
    private String jsonPlayers;
    private Integer currentDiceRoll;
    private Integer robberLocation;
    private Integer bankBrick;
    private Integer bankOre;
    private Integer bankSheep;
    private Integer bankWheat;
    private Integer bankWood;
    private Integer bankYearOfPlenty;
    private Integer bankMonopoly;
    private Integer bankRoadBuilding;
    private Integer bankVictoryPoint;
    private Integer bankKnight;
    private transient List<Account> players;

    public Game() {
        this.playerList = new ArrayList<>();
        this.players = new ArrayList<>();
        this.isGameOver = false;
        this.inProgress = false;
        this.gameName = "Untitled Game";
        
        // Initialize game state with empty/default values
        this.jsonHexes = "[]";
        this.jsonVertices = "[]";
        this.jsonEdges = "[]";
        this.jsonPlayers = "[]";
        this.currentDiceRoll = null;
        this.robberLocation = null;
        this.bankBrick = 19;    // Standard Catan starting values
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

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public List<Long> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Long> playerList) {
        this.playerList = playerList;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setIsGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getJsonHexes() { return jsonHexes; }
    public void setJsonHexes(String jsonHexes) { this.jsonHexes = jsonHexes; }

    public String getJsonVertices() { return jsonVertices; }
    public void setJsonVertices(String jsonVertices) { this.jsonVertices = jsonVertices; }

    public String getJsonEdges() { return jsonEdges; }
    public void setJsonEdges(String jsonEdges) { this.jsonEdges = jsonEdges; }

    public String getJsonPlayers() { return jsonPlayers; }
    public void setJsonPlayers(String jsonPlayers) { this.jsonPlayers = jsonPlayers; }

    public Integer getCurrentDiceRoll() { return currentDiceRoll; }
    public void setCurrentDiceRoll(Integer currentDiceRoll) { this.currentDiceRoll = currentDiceRoll; }

    public Integer getRobberLocation() { return robberLocation; }
    public void setRobberLocation(Integer robberLocation) { this.robberLocation = robberLocation; }

    public Integer getBankBrick() { return bankBrick; }
    public void setBankBrick(Integer bankBrick) { this.bankBrick = bankBrick; }

    public Integer getBankOre() { return bankOre; }
    public void setBankOre(Integer bankOre) { this.bankOre = bankOre; }

    public Integer getBankSheep() { return bankSheep; }
    public void setBankSheep(Integer bankSheep) { this.bankSheep = bankSheep; }

    public Integer getBankWheat() { return bankWheat; }
    public void setBankWheat(Integer bankWheat) { this.bankWheat = bankWheat; }

    public Integer getBankWood() { return bankWood; }
    public void setBankWood(Integer bankWood) { this.bankWood = bankWood; }

    public Integer getBankYearOfPlenty() { return bankYearOfPlenty; }
    public void setBankYearOfPlenty(Integer bankYearOfPlenty) { this.bankYearOfPlenty = bankYearOfPlenty; }

    public Integer getBankMonopoly() { return bankMonopoly; }
    public void setBankMonopoly(Integer bankMonopoly) { this.bankMonopoly = bankMonopoly; }

    public Integer getBankRoadBuilding() { return bankRoadBuilding; }
    public void setBankRoadBuilding(Integer bankRoadBuilding) { this.bankRoadBuilding = bankRoadBuilding; }

    public Integer getBankVictoryPoint() { return bankVictoryPoint; }
    public void setBankVictoryPoint(Integer bankVictoryPoint) { this.bankVictoryPoint = bankVictoryPoint; }

    public Integer getBankKnight() { return bankKnight; }
    public void setBankKnight(Integer bankKnight) { this.bankKnight = bankKnight; }

    public List<Account> getPlayers() {
        return players;
    }

    public void setPlayers(List<Account> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameId=" + gameId +
                ", playerList=" + playerList +
                ", winnerId=" + winnerId +
                ", isGameOver=" + isGameOver +
                ", inProgress=" + inProgress +
                ", createdAt=" + createdAt +
                ", gameName='" + gameName + '\'' +
                '}';
    }
}