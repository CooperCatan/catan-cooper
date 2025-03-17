package catan;

import catan.util.DataTransferObject;

public class Trade implements DataTransferObject {
    private long tradeId;
    private long gameId;
    private long turnNumber;
    private long fromPlayerId;
    private long toPlayerId;
    private String givenResource;
    private long givenAmount;
    private String receivedResource;
    private long receivedAmount;
    private boolean isAccepted;

    public Trade() {}

    @Override
    public long getId() {
        return tradeId;
    }

    public void setId(long id) {
        this.tradeId = id;
    }

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
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

    public long getFromPlayerId() {
        return fromPlayerId;
    }

    public void setFromPlayerId(long fromPlayerId) {
        this.fromPlayerId = fromPlayerId;
    }

    public long getToPlayerId() {
        return toPlayerId;
    }

    public void setToPlayerId(long toPlayerId) {
        this.toPlayerId = toPlayerId;
    }

    public String getGivenResource() {
        return givenResource;
    }

    public void setGivenResource(String givenResource) {
        this.givenResource = givenResource;
    }

    public long getGivenAmount() {
        return givenAmount;
    }

    public void setGivenAmount(long givenAmount) {
        this.givenAmount = givenAmount;
    }

    public String getReceivedResource() {
        return receivedResource;
    }

    public void setReceivedResource(String receivedResource) {
        this.receivedResource = receivedResource;
    }

    public long getReceivedAmount() {
        return receivedAmount;
    }

    public void setReceivedAmount(long receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }
} 
    
