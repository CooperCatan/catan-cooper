package catan;

import catan.util.DataTransferObject;

public class GameAction implements DataTransferObject {
    private long id;
    private long gameId;
    private String actionType;
    private String actionData;

    public GameAction() {}

    public GameAction(long id, long gameId, String actionType, String actionData) {
        this.id = id;
        this.gameId = gameId;
        this.actionType = actionType;
        this.actionData = actionData;
    }

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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }
}