package catan;

import catan.util.DataTransferObject;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameState implements DataTransferObject {
    private long gameId;
    private long turnNumber;
    private List<Hex> hexes;
    private Vertex[] vertices;
    private Long winnerId;
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

    public void newGame() {
        this.vertices = new Vertex[54];
        //Make all the vertices on the board
        for (int i = 0; i < 54; i++) {
            this.vertices[i] = new Vertex(i+1);
        }
        //Make a list of hexes
        this.hexes = new ArrayList<>();

        //Make the lists of resources and roll values, and then shuffle them
        List<String> resources = new ArrayList<>(Arrays.asList(
                "brick", "brick", "brick",
                "wood", "wood", "wood", "wood",
                "wheat", "wheat", "wheat", "wheat",
                "ore", "ore", "ore",
                "sheep", "sheep", "sheep", "sheep",
                "desert"
        ));
        List<Integer> rvs = new ArrayList<>(Arrays.asList(
                2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12
        ));
        Collections.shuffle(rvs);
        Collections.shuffle(resources);

        //Hex to be inserted
        //Have to unlink i and itrRVS because of the desert tile
        Hex insert;
        int itrRVS = 0;
        for (int i = 0; i < 19; i++) {
            //Set a Hex to its resource type. If desert, it doesn't get a number.
            if(resources.get(i).equals("desert")) {
                insert = new Hex(i+1, "desert", 0);
            } else {
                insert = new Hex(i+1, resources.get(i), rvs.get(itrRVS));
                itrRVS++;
            }
            hexes.add(insert);
            //Now assign all of these hexes their adjacent vertices
            //The linker also handles
            linkVertices(insert);
        }
    }

    public void linkVertices(Hex index) {
        if(index.getId() <= 3) {
            //These hexes "view" their surrounding vertices in clockwise order from top left to bottom left 0->5
            //This means that vertex "3" to hex 1 is vertex "5" to hex 2 (similarly vertex 2 on hex 1 is vertex 0 on hex 2)
            //The hexes 1 through 3 link to the vertices in the following map:
            index.setVertex(0, vertices[2*index.getId()-2]);
            index.setVertex(1, vertices[2*index.getId()-1]);
            index.setVertex(2, vertices[2*index.getId()]);
            index.setVertex(3, vertices[2*index.getId()+8]);
            index.setVertex(4, vertices[2*index.getId()+7]);
            index.setVertex(5, vertices[2*index.getId()+6]);
        } else if (index.getId() <= 7) {
            //The hexes 4 through 7 map via:
            index.setVertex(0, vertices[2*index.getId()-1]);
            index.setVertex(1, vertices[2*index.getId()]);
            index.setVertex(2, vertices[2*index.getId()+1]);
            index.setVertex(3, vertices[2*index.getId()+11]);
            index.setVertex(4, vertices[2*index.getId()+10]);
            index.setVertex(5, vertices[2*index.getId()+9]);
        } else if (index.getId() <= 12) {
            //The hexes 8 through 12 map via:
            index.setVertex(0, vertices[2*index.getId()]);
            index.setVertex(1, vertices[2*index.getId()+1]);
            index.setVertex(2, vertices[2*index.getId()+2]);
            index.setVertex(3, vertices[2*index.getId()+13]);
            index.setVertex(4, vertices[2*index.getId()+12]);
            index.setVertex(5, vertices[2*index.getId()+11]);
        } else if (index.getId() <= 16) {
            //The hexes 13 through 16 map via:
            index.setVertex(0, vertices[2*index.getId()+2]);
            index.setVertex(1, vertices[2*index.getId()+3]);
            index.setVertex(2, vertices[2*index.getId()+4]);
            index.setVertex(3, vertices[2*index.getId()+14]);
            index.setVertex(4, vertices[2*index.getId()+13]);
            index.setVertex(5, vertices[2*index.getId()+12]);
        } else if (index.getId() <= 19) {
            //The hexes 17 through 19 map via:
            index.setVertex(0, vertices[2*index.getId()+5]);
            index.setVertex(1, vertices[2*index.getId()+6]);
            index.setVertex(2, vertices[2*index.getId()+7]);
            index.setVertex(3, vertices[2*index.getId()+15]);
            index.setVertex(4, vertices[2*index.getId()+14]);
            index.setVertex(5, vertices[2*index.getId()+13]);
        }
        //Add edges to this hex list
        for (int i = 0; i < 6; i++) {
            index.getVertices().get(i).addEdge(index.getVertices().get((i + 1) % 6));
        }
    }

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