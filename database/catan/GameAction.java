package catan;

import catan.util.DataTransferObject;

public class GameAction implements DataTransferObject {
    private long gameid;
    private long turn;
    private String actionType;

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
}