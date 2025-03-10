package com.catan;

import com.catan.util.DataTransferObject;
import org.json.JSONObject;

public class GameState implements DataTransferObject {
    private long gameId;
    private long turnNumber;
    private JSONObject boardState;
    private Long winnerId;
    private JSONObject robberLocation;
    private boolean isGameOver;

    @Override
    public long getId() {
        return gameId;
    }

    public long getGameId() { return gameId; }
    public void setGameId(long gameId) { this.gameId = gameId; }

    public long getTurnNumber() { return turnNumber; }
    public void setTurnNumber(long turnNumber) { this.turnNumber = turnNumber; }

    public JSONObject getBoardState() { return boardState; }
    public void setBoardState(JSONObject boardState) { this.boardState = boardState; }

    public Long getWinnerId() { return winnerId; }
    public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }

    public JSONObject getRobberLocation() { return robberLocation; }
    public void setRobberLocation(JSONObject robberLocation) { this.robberLocation = robberLocation; }
    
    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }

    
} 