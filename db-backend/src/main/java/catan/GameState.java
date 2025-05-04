package catan;

import catan.util.DataTransferObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.sql.Connection;
import java.util.*;

public class GameState implements DataTransferObject {
    private long gameId;
    private long turnNumber;
    private String jsonHexes;
    private String jsonVertices;
    private String jsonEdges;
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
    private Connection connection;
    private PlayerStateDAO playerStateDAO;


    public GameState() {
        this.jsonHexes = null;
        this.jsonVertices = null;
        this.jsonEdges = null;
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

    public GameState(Connection connection) {
        this();  // Call the default constructor to initialize default values
        this.connection = connection;
        this.playerStateDAO = new PlayerStateDAO(connection);
    }

    public void newGame() {
        Vertex[] vertices = new Vertex[54];
        //Make all the vertices on the board
        for (int i = 0; i < 54; i++) {
            vertices[i] = new Vertex(i+1);
        }
        //Make a list of hexes
        List<Hex> hexes = new ArrayList<Hex>();

        //Make the lists of resources and roll values, and then shuffle them
        List<String> resources = new ArrayList<>(Arrays.asList(
                "brick", "brick", "brick",
                "wood", "wood", "wood", "wood",
                "wheat", "wheat", "wheat", "wheat",
                "ore", "ore", "ore",
                "sheep", "sheep", "sheep", "sheep",
                "desert"
        ));
        List<Integer> rvs = new ArrayList<>(Arrays.asList(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12));
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
            //The linker also handles edges
            linkVertices(insert, vertices);
        }

        Gson gson = new Gson();
        String jHexes = serializeHex(hexes, gson);
        String jVertices = serializeVertex(hexes, gson);
        String jEdges = serializeEdges(hexes, gson);
        this.jsonHexes = jHexes;
        this.jsonVertices = jVertices;
        this.jsonEdges = jEdges;
    }

    public String serializeHex(List<Hex> hexes, Gson gson) {
        return gson.toJson(hexes);
    }

    public String serializeVertex(List<Hex> hexes, Gson gson) {
        Map<Integer, Vertex> vertexMap = new HashMap<>();

        for (Hex hex : hexes) {
            for (Vertex v : hex.getVertices()) {
                vertexMap.putIfAbsent(v.getId(), v);
            }
        }

        return gson.toJson(new ArrayList<>(vertexMap.values()));
    }

    public String serializeEdges(List<Hex> hexes, Gson gson) {
        Map<String, Edge> edgeMap = new HashMap<>();

        for (Hex hex : hexes) {
            for (Vertex vex : hex.getVertices()) {
                for (Edge cross : vex.getAdjacentEdges()) {
                    String key = cross.getVertex1() < cross.getVertex2() ? cross.getVertex1() + "-" + cross.getVertex2() : cross.getVertex2() + "-" + cross.getVertex1();
                    edgeMap.putIfAbsent(key, cross);
                }
            }
        }

        return gson.toJson(new ArrayList<>(edgeMap.values()));
    }

    public List<Hex> deserialize(String jHexes, String jVertices, String jEdges, Gson gson) {
        List<Hex> hexes = gson.fromJson(jHexes, new TypeToken<List<Hex>>(){}.getType());
        List<Vertex> vertices = gson.fromJson(jVertices, new TypeToken<List<Vertex>>(){}.getType());
        List<Edge> edges = gson.fromJson(jEdges, new TypeToken<List<Edge>>(){}.getType());

        //Mappings lets us find by id quickly
        Map<Integer, Vertex> vertexMap = new HashMap<>();
        Map<String, Edge> edgeMap = new HashMap<>();
        for (Vertex v : vertices) {
            vertexMap.put(v.getId(), v);
        }
        for (Edge edge : edges) {
            int v1 = edge.getVertex1();
            int v2 = edge.getVertex2();
            String key = v1 < v2 ? v1 + "-" + v2 : v2 + "-" + v1;
            edgeMap.put(key, edge);
        }

        //Link up hexes and add appropriate edges across vertices
        for (Hex hex : hexes) {
            //Think this could also go i < 6
            for (int i = 0; i < hex.getVertexIds().size(); i++) {
                Integer vertexId = hex.getVertexIds().get(i);
                hex.setVertex(i, vertexMap.get(vertexId));
                //Now that the hex has mapped to this vertex, fill out its edges on the graph
                for(Integer target : hex.getVertices().get(i).getAdjacentEdgeIds()) {
                    //Key goes one way to prevent duplication (1-5 edge is the same as 5-1 edge)
                    //May be redundant with the way edges are set up initially
                    String key = vertexId < target ? vertexId + "-" + target : target + "-" + vertexId;
                    Edge cross = edgeMap.get(key);
                    //If null, there's a problem to report. Otherwise, if there are no adjacent edges, add cross. If it is valid for this vertex, add it
                    //The adding feature handles duplicates across vertices
                    if(cross == null) {
                        System.out.println(vertexId + "-" + target);
                    } else if(hex.getVertices().get(i).getAdjacentEdges() == null || (vertexId == cross.getVertex1() || vertexId == cross.getVertex2())) {
                        hex.getVertices().get(i).addEdge(vertexMap.get(target), cross.hasRoad(), cross.getPlayerId());
                    }
                }
            }
        }

        return hexes;
    }

    public void linkVertices(Hex index, Vertex[] vertices) {
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
        } else {
            //Invalid, try not to Seg Fault from hex vertex list
            return;
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

    public Long getWinnerId() { return winnerId; }
    public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }

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
        int selectedCard = weightedDeck.get(random.nextInt(weightedDeck.size()));

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

    public void incrementTurn() {
        this.turnNumber++;
        this.rollDice();
        return;
    }

    public int placeCity(int v, Long accountId) {
        Gson gson = new Gson();
        List<Hex> hexes = this.deserialize(this.jsonHexes, this.jsonVertices, this.jsonEdges, gson);
        //Look for the right vertex in the deserialized board
        for(Hex hex : hexes) {
            for(Vertex vex : hex.getVertices()) {
                if(vex.getId() == v) {
                    if(vex.setBuilding(2, accountId)) {
                        this.jsonHexes = serializeHex(hexes, gson);
                        this.jsonVertices = serializeVertex(hexes, gson);
                        this.jsonEdges = serializeEdges(hexes, gson);
                        return 1;
                    }
                    return 0;
                }
            }
        }
        return 0;
    }

    public int placeSettlement(int v, Long accountId) {
        Gson gson = new Gson();
        List<Hex> hexes = this.deserialize(this.jsonHexes, this.jsonVertices, this.jsonEdges, gson);
        //Look for the right vertex in the deserialized board
        for(Hex hex : hexes) {
            for(Vertex vex : hex.getVertices()) {
                if(vex.getId() == v) {
                    boolean validRoad = false;
                    for(Edge findRoad : vex.getAdjacentEdges()) {
                        if(findRoad.hasRoad() && (findRoad.getPlayerId() == accountId)) {
                            validRoad = true;
                        }
                    }
                    if(!validRoad) {
                        return 0;
                    }
                    boolean noneAdjacent = true;
                    for(int i = 0; i < vex.getAdjacentEdgeIds().size(); i++) {
                        for(Hex targetHex : hexes) {
                            for(Vertex targetVex : targetHex.getVertices()) {
                                if((targetVex.getId() == vex.getAdjacentEdgeIds().get(i)) && targetVex.getBuildingType() >= 1) {
                                    noneAdjacent = false;
                                }
                            }
                        }
                    }
                    //If the conditionals are valid, update the game state
                    if(noneAdjacent && validRoad && vex.getBuildingType() == 0 && vex.setBuilding(1, accountId)) {
                        this.jsonHexes = serializeHex(hexes, gson);
                        this.jsonVertices = serializeVertex(hexes, gson);
                        this.jsonEdges = serializeEdges(hexes, gson);
                        return 1;
                    }
                    return 0;
                }
            }
        }
        return 0;
    }

    public int placeRoad(int v1, int v2, Long accountId) {
        Gson gson = new Gson();
        List<Hex> hexes = this.deserialize(this.jsonHexes, this.jsonVertices, this.jsonEdges, gson);
        Edge targetEdge = null;
        Vertex vertex1 = null;
        Vertex vertex2 = null;

        //Find the vertices you care about
        outerLoop:
        for(Hex hex : hexes) {
            for(Vertex vertex : hex.getVertices()) {
                if(vertex.getId() == v1) {
                    vertex1 = vertex;
                }
                if(vertex.getId() == v2) {
                    vertex2 = vertex;
                }
                if(vertex1 != null && vertex2 != null) {
                    break outerLoop;
                }
            }
        }

        //Find the edge that we want to build on
        for(Edge edge : vertex1.getAdjacentEdges()) {
            if((edge.getVertex1() == v1 && edge.getVertex2() == v2) || (edge.getVertex1() == v2 && edge.getVertex2() == v1)) {
                targetEdge = edge;
                break;
            }
        }

        //Sanity check for unconnected vertices
        if(targetEdge == null) {
            return 0;
        }

        //Road exists, can't be placed
        boolean validRoad = false;
        if(targetEdge.hasRoad()) {
            return 0;
        } else {
            validRoad = true;
        }

        boolean hasConnection = false;
        //Check if player has a building at either vertex, this helps with first turn stuff
        if(vertex1.getPlayerId() == accountId || vertex2.getPlayerId() == accountId) {
            hasConnection = true;
        }
        //Check if either vertex has a road connecting to them
        if(!hasConnection) {
            for(Edge edge : vertex1.getAdjacentEdges()) {
                if(edge.hasRoad() && edge.getPlayerId() == accountId) {
                    hasConnection = true;
                    break;
                }
            }
            if(!hasConnection) {
                for(Edge edge : vertex2.getAdjacentEdges()) {
                    if(edge.hasRoad() && edge.getPlayerId() == accountId) {
                        hasConnection = true;
                        break;
                    }
                }
            }
        }

        //Invalid Road placement, can't be conncected to road
        if(!hasConnection) {
            return 0;
        }

        //Place the road
        targetEdge.setRoad(true);
        targetEdge.setPlayerId(accountId);
        this.jsonHexes = serializeHex(hexes, gson);
        this.jsonVertices = serializeVertex(hexes, gson);
        this.jsonEdges = serializeEdges(hexes, gson);
        return 1;
    }

    public int rollDice() {
        Random random = new Random();
        int roll = (int) (Math.random() * 6) + 1 + (int) (Math.random() * 6) + 1;
        //Unpack the JSON gameState to set up the hex list
        Gson gson = new Gson();
        List<Hex> hexes = this.deserialize(this.jsonHexes, this.jsonVertices, this.jsonEdges, gson);
        //Go through each hex and find what to give
        for (Hex hex : hexes) {
            //Only give resources to hexes matching the roll value
            if(hex.getRollValue() == roll) {
                for (Vertex vex : hex.getVertices()) {
                    //Check if a player has a building here
                    Long playerId = vex.getPlayerId();
                    if(playerId != null && playerId > 0) {
                        //If there is a building make a playerState object for the relevant player and update it
                        PlayerState playerState = playerStateDAO.findById(playerId);
                        if (playerState == null) {
                            System.err.println("PlayerState not found for playerId: " + playerId);
                            return 0;
                        }
                        //Since buildingType is 1 to 1 with the amount of resources you get, just add the building type
                        switch(hex.getResourceType()) {
                            case "brick" -> {
                                int amount = vex.getBuildingType();
                                if (bankBrick >= amount) {
                                    playerState.setBrick(playerState.getBrick() + amount);
                                    bankBrick -= amount;
                                }
                            }
                            case "wood" -> {
                                int amount = vex.getBuildingType();
                                if (bankWood >= amount) {
                                    playerState.setWood(playerState.getWood() + amount);
                                    bankWood -= amount;
                                }
                            }
                            case "wheat" -> {
                                int amount = vex.getBuildingType();
                                if (bankWheat >= amount) {
                                    playerState.setWheat(playerState.getWheat() + amount);
                                    bankWheat -= amount;
                                }
                            }
                            case "ore" -> {
                                int amount = vex.getBuildingType();
                                if (bankOre >= amount) {
                                    playerState.setOre(playerState.getOre() + amount);
                                    bankOre -= amount;
                                }
                            }
                            case "sheep" -> {
                                int amount = vex.getBuildingType();
                                if (bankSheep >= amount) {
                                    playerState.setSheep(playerState.getSheep() + amount);
                                    bankSheep -= amount;
                                }
                            }
                        }
                        playerStateDAO.update(playerState);
                    }
                }
            }
        }
        return roll;
    }

    public String getJsonHexes() {
        return jsonHexes;
    }

    public String getJsonVertices() {
        return jsonVertices;
    }

    public String getJsonEdges() {
        return jsonEdges;
    }
}