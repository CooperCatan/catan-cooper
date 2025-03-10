package catan;

import catan.util.DataTransferObject;

public class BankCardsRemaining implements DataTransferObject {

    private long gameId;
    private long turnNumber;
    private long brick;
    private long ore;
    private long sheep;
    private long wheat;
    private long wood;
    private long yearOfPlenty;
    private long monopoly;
    private long roadBuilding;
    private long victoryPoint;
    private long knight;


    @Override
    public long getId() {
        return bankId;
    }

    public long getGameId() { return gameId; }
    public void setGameId(long gameId) { this.gameId = gameId; }

    public long getTurnNumber() { return turnNumber; }
    public void setTurnNumber(long turnNumber) { this.turnNumber = turnNumber; }

    public long getBrick() { return brick; }
    public void setBrick(long brick) { this.brick = brick; }

    public long getOre() { return ore; }
    public void setOre(long ore) { this.ore = ore; }
    
    public long getSheep() { return sheep; }
    public void setSheep(long sheep) { this.sheep = sheep; }

    public long getWheat() { return wheat; }
    public void setWheat(long wheat) { this.wheat = wheat; }
    
    public long getWood() { return wood; }
    public void setWood(long wood) { this.wood = wood; }

    public long getYearOfPlenty() { return yearOfPlenty; }
    public void setYearOfPlenty(long yearOfPlenty) { this.yearOfPlenty = yearOfPlenty; }

    public long getMonopoly() { return monopoly; }
    public void setMonopoly(long monopoly) { this.monopoly = monopoly; }

    public long getRoadBuilding() { return roadBuilding; }
    public void setRoadBuilding(long roadBuilding) { this.roadBuilding = roadBuilding; }

    public long getVictoryPoint() { return victoryPoint; }
    public void setVictoryPoint(long victoryPoint) { this.victoryPoint = victoryPoint; }

    public long getKnight() { return knight; }
    public void setKnight(long knight) { this.knight = knight; }

}