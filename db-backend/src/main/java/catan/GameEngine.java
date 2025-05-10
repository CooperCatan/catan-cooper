package catan;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GameEngine {
    // Game state from database
    private long gameId;
    private String jsonHexes;
    private String jsonVertices;
    private String jsonEdges;
    private String jsonPlayers;
    private Long winnerId;
    private boolean isGameOver;
    
    // Bank resources
    private int bankBrick;
    private int bankOre;
    private int bankSheep;
    private int bankWheat;
    private int bankWood;
    
    // Development cards
    private int bankYearOfPlenty;
    private int bankMonopoly;
    private int bankRoadBuilding;
    private int bankVictoryPoint;
    private int bankKnight;

    // Deserialized board state
    private List<Hex> hexes;
    private List<Vertex> vertices;
    private List<Edge> edges;
    private Map<Long, Player> players;  // accountId -> Player
    private int currentDiceRoll;
    private int robberLocation;

    // Constants
    private static final int SETTLEMENT_BRICK = 1;
    private static final int SETTLEMENT_WOOD = 1;
    private static final int SETTLEMENT_WHEAT = 1;
    private static final int SETTLEMENT_SHEEP = 1;
    
    private static final int CITY_ORE = 3;
    private static final int CITY_WHEAT = 2;
    
    private static final int ROAD_BRICK = 1;
    private static final int ROAD_WOOD = 1;

    private static final int DEVELOPMENT_CARD_ORE = 1;
    private static final int DEVELOPMENT_CARD_WHEAT = 1;
    private static final int DEVELOPMENT_CARD_SHEEP = 1;

    private static final String[] RESOURCE_TYPES = {"wood", "brick", "ore", "wheat", "sheep", "desert"};
    private static final Integer[] PIP_VALUES = {2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12};
    private static final int NUM_HEXES = 19;

    public GameEngine(long gameId) {
        this.gameId = gameId;
        
        // Try to load existing game state from database
        try (Connection connection = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/catan",
            "postgres",
            "postgres"
        )) {
            GameDAO gameDAO = new GameDAO(connection);
            Game game = gameDAO.findById(gameId);
            
            if (game != null && game.getJsonHexes() != null) {
                // Load existing game state
                ObjectMapper mapper = new ObjectMapper();
                try {
                    this.jsonHexes = game.getJsonHexes();
                    this.jsonVertices = game.getJsonVertices();
                    this.jsonEdges = game.getJsonEdges();
                    this.jsonPlayers = game.getJsonPlayers();
                    this.currentDiceRoll = game.getCurrentDiceRoll() != null ? game.getCurrentDiceRoll() : 0;
                    this.robberLocation = game.getRobberLocation() != null ? game.getRobberLocation() : 0;
                    this.bankBrick = game.getBankBrick() != null ? game.getBankBrick() : 19;
                    this.bankOre = game.getBankOre() != null ? game.getBankOre() : 19;
                    this.bankSheep = game.getBankSheep() != null ? game.getBankSheep() : 19;
                    this.bankWheat = game.getBankWheat() != null ? game.getBankWheat() : 19;
                    this.bankWood = game.getBankWood() != null ? game.getBankWood() : 19;
                    this.bankYearOfPlenty = game.getBankYearOfPlenty() != null ? game.getBankYearOfPlenty() : 2;
                    this.bankMonopoly = game.getBankMonopoly() != null ? game.getBankMonopoly() : 2;
                    this.bankRoadBuilding = game.getBankRoadBuilding() != null ? game.getBankRoadBuilding() : 2;
                    this.bankVictoryPoint = game.getBankVictoryPoint() != null ? game.getBankVictoryPoint() : 5;
                    this.bankKnight = game.getBankKnight() != null ? game.getBankKnight() : 14;
                    
                    // Deserialize players
                    if (game.getJsonPlayers() != null) {
                        this.players = mapper.readValue(game.getJsonPlayers(), 
                            mapper.getTypeFactory().constructMapType(Map.class, Long.class, Player.class));
                    } else {
                        this.players = new HashMap<>();
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to deserialize game state", e);
                }
            } else {
                // Initialize new game state
                String boardState = generateInitialBoardState();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Map<String, Object> state = mapper.readValue(boardState, Map.class);
                    this.jsonHexes = mapper.writeValueAsString(state.get("hexes"));
                    this.jsonVertices = mapper.writeValueAsString(state.get("vertices"));
                    this.jsonEdges = mapper.writeValueAsString(state.get("edges"));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to parse board state", e);
                }
                
                // Initialize bank resources
                this.bankBrick = 19;
                this.bankOre = 19;
                this.bankSheep = 19;
                this.bankWheat = 19;
                this.bankWood = 19;
                
                // Initialize development cards
                this.bankYearOfPlenty = 2;
                this.bankMonopoly = 2;
                this.bankRoadBuilding = 2;
                this.bankVictoryPoint = 5;
                this.bankKnight = 14;

                this.players = new HashMap<>();
            }
            
            deserializeBoard();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    private void deserializeBoard() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.hexes = Arrays.asList(mapper.readValue(jsonHexes, Hex[].class));
            this.vertices = Arrays.asList(mapper.readValue(jsonVertices, Vertex[].class));
            this.edges = Arrays.asList(mapper.readValue(jsonEdges, Edge[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize board state", e);
        }
    }

    private String generateInitialBoardState() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Generate hexes
            List<Hex> hexes = new ArrayList<>();
            List<String> resourceTypes = new ArrayList<>();
            List<Integer> pipValues = new ArrayList<>(Arrays.asList(PIP_VALUES));
            
            // Add resources according to standard Catan rules:
            // 4 wood, 4 wheat, 4 sheep, 3 brick, 3 ore, 1 desert
            resourceTypes.addAll(Arrays.asList("wood", "wood", "wood", "wood"));
            resourceTypes.addAll(Arrays.asList("wheat", "wheat", "wheat", "wheat"));
            resourceTypes.addAll(Arrays.asList("sheep", "sheep", "sheep", "sheep"));
            resourceTypes.addAll(Arrays.asList("brick", "brick", "brick"));
            resourceTypes.addAll(Arrays.asList("ore", "ore", "ore"));
            
            // Shuffle both lists
            Collections.shuffle(resourceTypes);
            Collections.shuffle(pipValues);
            
            // Create hexes
            for (int i = 0; i < NUM_HEXES; i++) {
                Hex hex = new Hex();
                hex.setId(i + 1);
                
                // Place desert in the middle (index 9)
                if (i == 9) {
                    hex.setType("desert");
                    hex.setHasRobber(true);
                    hex.setPipValue(0);  // Desert has no pip value
                } else {
                    int resourceIndex = i > 9 ? i - 1 : i;
                    int pipIndex = i > 9 ? i - 1 : i;
                    hex.setType(resourceTypes.get(resourceIndex));
                    hex.setPipValue(pipValues.get(pipIndex));
                    hex.setHasRobber(false);
                }
                
                // Set coordinates based on the hexagonal Catan board layout
                hex.setX(calculateHexX(i));
                hex.setY(calculateHexY(i));
                
                hexes.add(hex);
            }
            
            // Generate vertices
            List<Vertex> vertices = new ArrayList<>();
            for (int i = 0; i < 54; i++) {  // Standard Catan has 54 vertices
                Vertex vertex = new Vertex();
                vertex.setId(i + 1);
                vertex.setOccupied(false);
                vertex.setBuildingType(null);
                vertex.setOwnerId(null);
                vertices.add(vertex);
            }
            
            // Generate edges
            List<Edge> edges = new ArrayList<>();
            for (int i = 0; i < 72; i++) {  // Standard Catan has 72 edges
                Edge edge = new Edge();
                edge.setId(i + 1);
                edge.setOccupied(false);
                edge.setOwnerId(null);
                edges.add(edge);
            }
            
            // Convert to JSON
            Map<String, Object> boardState = new HashMap<>();
            boardState.put("hexes", hexes);
            boardState.put("vertices", vertices);
            boardState.put("edges", edges);
            
            return mapper.writeValueAsString(boardState);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to generate initial board state", e);
        }
    }

    private double calculateHexX(int index) {
        // Calculate x coordinate based on hex position in hexagonal grid
        if (index < 3) {  // First row (3 hexes)
            return index * 2 + 2;
        } else if (index < 7) {  // Second row (4 hexes)
            return (index - 3) * 2 + 1;
        } else if (index < 12) {  // Third row (5 hexes)
            return (index - 7) * 2;
        } else if (index < 16) {  // Fourth row (4 hexes)
            return (index - 12) * 2 + 1;
        } else {  // Fifth row (3 hexes)
            return (index - 16) * 2 + 2;
        }
    }

    private double calculateHexY(int index) {
        // Calculate y coordinate based on hex position in hexagonal grid
        if (index < 3) {  // First row
            return 0;
        } else if (index < 7) {  // Second row
            return 1.5;
        } else if (index < 12) {  // Third row
            return 3;
        } else if (index < 16) {  // Fourth row
            return 4.5;
        } else {  // Fifth row
            return 6;
        }
    }

    // Game Actions

    public boolean placeSettlement(long playerId, long vertexId, boolean initialPlacement) {
        Player player = players.get(playerId);
        if (player == null) {
            return false;
        }

        Vertex vertex = vertices.get((int)vertexId);
        if (vertex == null || vertex.isOccupied()) {
            return false;
        }

        // Check distance rule
        if (hasAdjacentSettlement((int)vertexId)) {
            return false;
        }

        // Check if player has a connected road (except during initial placement)
        if (!initialPlacement && !hasConnectedRoad(playerId, (int)vertexId)) {
            return false;
        }

        // Check resources (except during initial placement)
        if (!initialPlacement) {
            if (!player.hasResources(SETTLEMENT_BRICK, 0, SETTLEMENT_SHEEP, SETTLEMENT_WHEAT, SETTLEMENT_WOOD)) {
                return false;
            }
            
            // Deduct resources
            player.deductResources(SETTLEMENT_BRICK, 0, SETTLEMENT_SHEEP, SETTLEMENT_WHEAT, SETTLEMENT_WOOD);
            updateBankResources(-SETTLEMENT_BRICK, 0, -SETTLEMENT_SHEEP, -SETTLEMENT_WHEAT, -SETTLEMENT_WOOD);
        }

        // Place settlement
        vertex.setOccupied(true);
        vertex.setOwnerId(playerId);
        vertex.setBuildingType("settlement");
        player.incrementSettlements();
        player.addVictoryPoint();

        // Persist changes
        persistGameState();

        return true;
    }

    public boolean placeCity(long playerId, long vertexId) {
        Player player = players.get(playerId);
        if (player == null) {
            return false;
        }

        Vertex vertex = vertices.get((int)vertexId);
        if (vertex == null || !vertex.isOccupied() || 
            !vertex.getOwnerId().equals(playerId) || 
            !"settlement".equals(vertex.getBuildingType())) {
            return false;
        }

        // Check resources
        if (!player.hasResources(0, CITY_ORE, 0, CITY_WHEAT, 0)) {
            return false;
        }

        // Deduct resources
        player.deductResources(0, CITY_ORE, 0, CITY_WHEAT, 0);
        updateBankResources(0, -CITY_ORE, 0, -CITY_WHEAT, 0);

        // Upgrade to city
        vertex.setBuildingType("city");
        player.decrementSettlements();
        player.incrementCities();
        player.addVictoryPoint();

        // Persist changes
        persistGameState();

        return true;
    }

    public boolean buildRoad(long playerId, long edgeId, boolean initialPlacement) {
        Player player = players.get(playerId);
        if (player == null) {
            return false;
        }

        Edge edge = edges.get((int)edgeId);
        if (edge == null || edge.isOccupied()) {
            return false;
        }

        // Check if road is connected to player's existing road or settlement (except during initial placement)
        if (!initialPlacement && !isValidRoadPlacement(playerId, (int)edgeId)) {
            return false;
        }

        // Check resources (except during initial placement)
        if (!initialPlacement) {
            if (!player.hasResources(ROAD_BRICK, 0, 0, 0, ROAD_WOOD)) {
                return false;
            }
            
            // Deduct resources
            player.deductResources(ROAD_BRICK, 0, 0, 0, ROAD_WOOD);
            updateBankResources(-ROAD_BRICK, 0, 0, 0, -ROAD_WOOD);
        }

        // Place road
        edge.setOccupied(true);
        edge.setOwnerId(playerId);
        player.incrementRoads();

        // Check for longest road
        updateLongestRoad();

        // Persist changes
        persistGameState();

        return true;
    }

    public boolean buyDevelopmentCard(long playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            return false;
        }

        // Check if development cards are available
        if (bankKnight + bankYearOfPlenty + bankMonopoly + bankRoadBuilding + bankVictoryPoint == 0) {
            return false;
        }

        // Check resources
        if (!player.hasResources(0, DEVELOPMENT_CARD_ORE, DEVELOPMENT_CARD_SHEEP, DEVELOPMENT_CARD_WHEAT, 0)) {
            return false;
        }

        // Deduct resources
        player.deductResources(0, DEVELOPMENT_CARD_ORE, DEVELOPMENT_CARD_SHEEP, DEVELOPMENT_CARD_WHEAT, 0);
        updateBankResources(0, -DEVELOPMENT_CARD_ORE, -DEVELOPMENT_CARD_SHEEP, -DEVELOPMENT_CARD_WHEAT, 0);

        // Randomly select a development card
        String cardType = getRandomDevelopmentCard();
        if (cardType != null) {
            player.addDevelopmentCard(cardType);
            return true;
        }

        return false;
    }

    public boolean playKnight(long playerId, long newRobberHex, long robbedPlayerId) {
        Player player = players.get(playerId);
        if (player == null || !player.hasKnight()) {
            return false;
        }

        // Move robber
        robberLocation = (int)newRobberHex;

        // Rob player
        Player robbedPlayer = players.get(robbedPlayerId);
        if (robbedPlayer != null && robbedPlayer.getTotalResources() > 0) {
            String stolenResource = robbedPlayer.getRandomResource();
            if (stolenResource != null) {
                robbedPlayer.deductResource(stolenResource, 1);
                player.addResource(stolenResource, 1);
            }
        }

        // Use knight
        player.useKnight();
        updateLargestArmy();

        return true;
    }

    public boolean playYearOfPlenty(long playerId, String resource1, String resource2) {
        Player player = players.get(playerId);
        if (player == null || !player.hasYearOfPlenty()) {
            return false;
        }

        // Check if requested resources are available in bank
        if (!hasEnoughInBank(resource1, 1) || !hasEnoughInBank(resource2, 1)) {
            return false;
        }

        // Give resources to player
        player.addResource(resource1, 1);
        player.addResource(resource2, 1);
        updateBankResource(resource1, -1);
        updateBankResource(resource2, -1);

        // Use card
        player.useYearOfPlenty();
        return true;
    }

    public boolean playMonopoly(long playerId, String resource) {
        Player player = players.get(playerId);
        if (player == null || !player.hasMonopoly()) {
            return false;
        }

        // Collect all of the specified resource from other players
        int totalCollected = 0;
        for (Player otherPlayer : players.values()) {
            if (otherPlayer.getAccountId() != playerId) {
                int amount = otherPlayer.getResourceAmount(resource);
                otherPlayer.deductResource(resource, amount);
                totalCollected += amount;
            }
        }

        // Give collected resources to player
        player.addResource(resource, totalCollected);

        // Use card
        player.useMonopoly();
        return true;
    }

    public boolean playRoadBuilding(long playerId, long edgeId1, long edgeId2) {
        Player player = players.get(playerId);
        if (player == null || !player.hasRoadBuilding()) {
            return false;
        }

        // Try to build both roads
        boolean firstRoad = buildRoad(playerId, edgeId1, true);
        boolean secondRoad = buildRoad(playerId, edgeId2, true);

        if (firstRoad && secondRoad) {
            player.useRoadBuilding();
            return true;
        }

        // If either road failed, undo the successful one
        if (firstRoad) {
            undoRoad(edgeId1);
        }

        return false;
    }

    // Helper methods

    private boolean hasAdjacentSettlement(long vertexId) {
        Vertex vertex = vertices.get((int)vertexId);
        for (int adjacentVertexId : vertex.getAdjacentVertices()) {
            Vertex adjacentVertex = vertices.get(adjacentVertexId);
            if (adjacentVertex.isOccupied()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasConnectedRoad(long playerId, long vertexId) {
        Vertex vertex = vertices.get((int)vertexId);
        for (int edgeId : vertex.getConnectedEdges()) {
            Edge edge = edges.get(edgeId);
            if (edge.isOccupied() && edge.getOwnerId() == playerId) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidRoadPlacement(long playerId, long edgeId) {
        Edge edge = edges.get((int)edgeId);
        
        // Check connected vertices
        for (int vertexId : edge.getConnectedVertices()) {
            Vertex vertex = vertices.get(vertexId);
            if (vertex.isOccupied() && vertex.getOwnerId() == playerId) {
                return true;
            }
        }

        // Check connected edges
        for (int connectedEdgeId : edge.getConnectedEdges()) {
            Edge connectedEdge = edges.get(connectedEdgeId);
            if (connectedEdge.isOccupied() && connectedEdge.getOwnerId() == playerId) {
                return true;
            }
        }

        return false;
    }

    private void updateLongestRoad() {
        int maxRoadLength = 0;
        Long longestRoadPlayerId = null;

        for (Player player : players.values()) {
            int roadLength = calculateLongestRoad(player.getAccountId());
            if (roadLength > maxRoadLength) {
                maxRoadLength = roadLength;
                longestRoadPlayerId = player.getAccountId();
            }
        }

        // Update longest road status
        if (maxRoadLength >= 5) {
            for (Player player : players.values()) {
                if (player.getAccountId() == longestRoadPlayerId) {
                    if (!player.hasLongestRoad()) {
                        player.setLongestRoad(true);
                        player.addVictoryPoints(2);
                    }
                } else if (player.hasLongestRoad()) {
                    player.setLongestRoad(false);
                    player.deductVictoryPoints(2);
                }
            }
        }
    }

    private int calculateLongestRoad(long playerId) {
        Set<Integer> visited = new HashSet<>();
        int maxLength = 0;

        // Start DFS from each edge owned by the player
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            if (edge.isOccupied() && edge.getOwnerId() == playerId) {
                visited.clear();
                maxLength = Math.max(maxLength, dfsRoadLength(i, playerId, visited));
            }
        }

        return maxLength;
    }

    private int dfsRoadLength(int edgeId, long playerId, Set<Integer> visited) {
        if (visited.contains(edgeId)) {
            return 0;
        }

        visited.add(edgeId);
        Edge edge = edges.get(edgeId);
        int maxLength = 1;

        // Explore connected edges
        for (int connectedEdgeId : edge.getConnectedEdges()) {
            Edge connectedEdge = edges.get(connectedEdgeId);
            if (connectedEdge.isOccupied() && connectedEdge.getOwnerId() == playerId) {
                maxLength = Math.max(maxLength, 1 + dfsRoadLength(connectedEdgeId, playerId, visited));
            }
        }

        return maxLength;
    }

    private void updateLargestArmy() {
        int maxKnights = 2;  // Minimum 3 knights required
        Long largestArmyPlayerId = null;

        for (Player player : players.values()) {
            if (player.getKnightUsed() > maxKnights) {
                maxKnights = player.getKnightUsed();
                largestArmyPlayerId = player.getAccountId();
            }
        }

        // Update largest army status
        if (largestArmyPlayerId != null) {
            for (Player player : players.values()) {
                if (player.getAccountId() == largestArmyPlayerId) {
                    if (!player.hasLargestArmy()) {
                        player.setLargestArmy(true);
                        player.addVictoryPoints(2);
                    }
                } else if (player.hasLargestArmy()) {
                    player.setLargestArmy(false);
                    player.deductVictoryPoints(2);
                }
            }
        }
    }

    private String getRandomDevelopmentCard() {
        int totalCards = bankKnight + bankYearOfPlenty + bankMonopoly + bankRoadBuilding + bankVictoryPoint;
        if (totalCards == 0) {
            return null;
        }

        int randomNum = new Random().nextInt(totalCards);
        int sum = 0;

        if ((sum += bankKnight) > randomNum) {
            bankKnight--;
            return "knight";
        }
        if ((sum += bankYearOfPlenty) > randomNum) {
            bankYearOfPlenty--;
            return "yearOfPlenty";
        }
        if ((sum += bankMonopoly) > randomNum) {
            bankMonopoly--;
            return "monopoly";
        }
        if ((sum += bankRoadBuilding) > randomNum) {
            bankRoadBuilding--;
            return "roadBuilding";
        }
        if ((sum += bankVictoryPoint) > randomNum) {
            bankVictoryPoint--;
            return "victoryPoint";
        }

        return null;
    }

    private void updateBankResources(int brick, int ore, int sheep, int wheat, int wood) {
        bankBrick += brick;
        bankOre += ore;
        bankSheep += sheep;
        bankWheat += wheat;
        bankWood += wood;
    }

    private void updateBankResource(String resource, int amount) {
        switch (resource.toLowerCase()) {
            case "brick": bankBrick += amount; break;
            case "ore": bankOre += amount; break;
            case "sheep": bankSheep += amount; break;
            case "wheat": bankWheat += amount; break;
            case "wood": bankWood += amount; break;
        }
    }

    private boolean hasEnoughInBank(String resource, int amount) {
        switch (resource.toLowerCase()) {
            case "brick": return bankBrick >= amount;
            case "ore": return bankOre >= amount;
            case "sheep": return bankSheep >= amount;
            case "wheat": return bankWheat >= amount;
            case "wood": return bankWood >= amount;
            default: return false;
        }
    }

    private void undoRoad(long edgeId) {
        Edge edge = edges.get((int)edgeId);
        if (edge.isOccupied()) {
            Player player = players.get(edge.getOwnerId());
            edge.setOccupied(false);
            edge.setOwnerId(null);
            player.decrementRoads();
        }
    }

    private void persistGameState() {
        try (Connection connection = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/catan",
            "postgres",
            "postgres"
        )) {
            GameDAO gameDAO = new GameDAO(connection);
            Game game = gameDAO.findById(gameId);
            if (game != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    game.setJsonHexes(mapper.writeValueAsString(hexes));
                    game.setJsonVertices(mapper.writeValueAsString(vertices));
                    game.setJsonEdges(mapper.writeValueAsString(edges));
                    game.setJsonPlayers(mapper.writeValueAsString(players));
                    game.setCurrentDiceRoll(currentDiceRoll);
                    game.setRobberLocation(robberLocation);
                    game.setBankBrick(bankBrick);
                    game.setBankOre(bankOre);
                    game.setBankSheep(bankSheep);
                    game.setBankWheat(bankWheat);
                    game.setBankWood(bankWood);
                    game.setBankYearOfPlenty(bankYearOfPlenty);
                    game.setBankMonopoly(bankMonopoly);
                    game.setBankRoadBuilding(bankRoadBuilding);
                    game.setBankVictoryPoint(bankVictoryPoint);
                    game.setBankKnight(bankKnight);
                    gameDAO.updateGameState(game);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize game state", e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    // Getters and Setters
    public long getGameId() { return gameId; }
    public void setGameId(long gameId) { this.gameId = gameId; }

    public String getJsonHexes() { return jsonHexes; }
    public void setJsonHexes(String jsonHexes) { 
        this.jsonHexes = jsonHexes;
        deserializeBoard();
    }

    public String getJsonVertices() { return jsonVertices; }
    public void setJsonVertices(String jsonVertices) { 
        this.jsonVertices = jsonVertices;
        deserializeBoard();
    }

    public String getJsonEdges() { return jsonEdges; }
    public void setJsonEdges(String jsonEdges) { 
        this.jsonEdges = jsonEdges;
        deserializeBoard();
    }

    public String getJsonPlayers() { return jsonPlayers; }
    public void setJsonPlayers(String jsonPlayers) { 
        this.jsonPlayers = jsonPlayers;
        // Deserialize players when jsonPlayers is updated
        if (jsonPlayers != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.players = mapper.readValue(jsonPlayers, 
                    mapper.getTypeFactory().constructMapType(Map.class, Long.class, Player.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize players", e);
            }
        }
    }

    public Long getWinnerId() { return winnerId; }
    public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }

    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }

    public int getBankBrick() { return bankBrick; }
    public void setBankBrick(int bankBrick) { this.bankBrick = bankBrick; }

    public int getBankOre() { return bankOre; }
    public void setBankOre(int bankOre) { this.bankOre = bankOre; }

    public int getBankSheep() { return bankSheep; }
    public void setBankSheep(int bankSheep) { this.bankSheep = bankSheep; }

    public int getBankWheat() { return bankWheat; }
    public void setBankWheat(int bankWheat) { this.bankWheat = bankWheat; }

    public int getBankWood() { return bankWood; }
    public void setBankWood(int bankWood) { this.bankWood = bankWood; }

    public int getBankYearOfPlenty() { return bankYearOfPlenty; }
    public void setBankYearOfPlenty(int bankYearOfPlenty) { this.bankYearOfPlenty = bankYearOfPlenty; }

    public int getBankMonopoly() { return bankMonopoly; }
    public void setBankMonopoly(int bankMonopoly) { this.bankMonopoly = bankMonopoly; }

    public int getBankRoadBuilding() { return bankRoadBuilding; }
    public void setBankRoadBuilding(int bankRoadBuilding) { this.bankRoadBuilding = bankRoadBuilding; }

    public int getBankVictoryPoint() { return bankVictoryPoint; }
    public void setBankVictoryPoint(int bankVictoryPoint) { this.bankVictoryPoint = bankVictoryPoint; }

    public int getBankKnight() { return bankKnight; }
    public void setBankKnight(int bankKnight) { this.bankKnight = bankKnight; }

    public List<Hex> getHexes() { return hexes; }
    public List<Vertex> getVertices() { return vertices; }
    public List<Edge> getEdges() { return edges; }
    public Map<Long, Player> getPlayers() { return players; }
    public void addPlayer(Player player) { this.players.put(player.getAccountId(), player); }
    
    public int getCurrentDiceRoll() { return currentDiceRoll; }
    public void setCurrentDiceRoll(int currentDiceRoll) { this.currentDiceRoll = currentDiceRoll; }
    
    public int getRobberLocation() { return robberLocation; }
    public void setRobberLocation(int robberLocation) { this.robberLocation = robberLocation; }
}