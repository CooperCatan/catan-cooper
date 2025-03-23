package catan;

import catan.util.DataTransferObject;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameState implements DataTransferObject {
    private long gameId;
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

    public long getBankBrick() { return bankBrick; }
    public void setBankBrick(long bankBrick) { this.bankBrick = bankBrick; }

    public long getBankOre() { return bankOre; }
    public void setBankOre(long bankOre) { this.bankOre = bankOre; }

    public long getBankSheep() { return bankSheep; }
    public void setBankSheep(long bankSheep) { this.bankSheep = bankSheep; }

    public long getBankWheat() { return bankWheat; }
    public void setBankWheat(long bankWheat) { this.bankWheat = bankWheat; }

    public long getBankWood() { return bankWood; }
    public void setBankWood(long bankWood) { this.bankWood = bankWood; }

    public long getBankYearOfPlenty() { return bankYearOfPlenty; }
    public void setBankYearOfPlenty(long bankYearOfPlenty) { this.bankYearOfPlenty = bankYearOfPlenty; }

    public long getBankMonopoly() { return bankMonopoly; }
    public void setBankMonopoly(long bankMonopoly) { this.bankMonopoly = bankMonopoly; }

    public long getBankRoadBuilding() { return bankRoadBuilding; }
    public void setBankRoadBuilding(long bankRoadBuilding) { this.bankRoadBuilding = bankRoadBuilding; }

    public long getBankVictoryPoint() { return bankVictoryPoint; }
    public void setBankVictoryPoint(long bankVictoryPoint) { this.bankVictoryPoint = bankVictoryPoint; }

    public long getBankKnight() { return bankKnight; }
    public void setBankKnight(long bankKnight) { this.bankKnight = bankKnight; }


    public int getCard() {
        //Reseed on getCard
        Random random = new Random();
        List<Integer> weightedDeck = new ArrayList<>();

        //Add each card to the list based on its remaining count
        //I don't like using one line statements or switches but if there's ever a time to do so it's here
        for (int i = 0; i < this.bankKnight; i++) weightedDeck.add(1);
        for (int i = 0; i < this.bankMonopoly; i++) weightedDeck.add(2);
        for (int i = 0; i < this.bankYearOfPlenty; i++) weightedDeck.add(3);
        for (int i = 0; i < this.bankVictoryPoint; i++) weightedDeck.add(4);
        for (int i = 0; i < this.bankRoadBuilding; i++) weightedDeck.add(5);

        if (weightedDeck.isEmpty()) {
            //No Dev cards
            return 0;
        }

        //Randomly select a card from the weighted list
        int selectedCard = weightedDeck.get(this.random.nextInt(weightedDeck.size()));

        //Reduce the count of the selected card
        switch (selectedCard) {
            case 1 -> this.bankKnight--;
            case 2 -> this.bankMonopoly--;
            case 3 -> this.bankYearOfPlenty--;
            case 4 -> this.bankVictoryPoint--;
            case 5 -> this.bankRoadBuilding--;
        }

        return selectedCard;
    }
} 