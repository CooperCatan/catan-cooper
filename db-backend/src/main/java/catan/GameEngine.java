package catan;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.geom.Point2D;
import com.fasterxml.jackson.databind.DeserializationFeature; 
import java.util.Collections; 
import java.util.Arrays; 
import catan.BoardGenerator.BoardData; 
import com.fasterxml.jackson.core.type.TypeReference; // for deserializing List/Map

public class GameEngine {
    private static final Logger logger = LoggerFactory.getLogger(GameEngine.class);
    // --- std player colors, matches frontend's PLAYER_COLORS) ---
    private static final List<String> PLAYER_COLORS = Arrays.asList(
        "#FF6B6B", // Red
        "#4ECDC4", // Blue/Teal
        "#FFA07A", // Orange (LightSalmon)
        "#98D8AA"  // Green
    );

    // game state being stored from database
    private long gameId;
    private String jsonHexes;
    private String jsonVertices;
    private String jsonEdges;
    private String jsonPlayers;
    private Long winnerId;
    private boolean isGameOver;
    private boolean inProgress;
    
    // bank resources
    private int bankBrick;
    private int bankOre;
    private int bankSheep;
    private int bankWheat;
    private int bankWood;
    
    // development cards
    private int bankYearOfPlenty;
    private int bankMonopoly;
    private int bankRoadBuilding;
    private int bankVictoryPoint;
    private int bankKnight;

    // deserialized board state
    private List<Hex> hexes;
    private List<Vertex> vertices;
    private List<Edge> edges;
    private Map<Long, Player> players;  // accountId -> Player
    private int currentDiceRoll;
    private int robberLocation;

    // setup phase management
    private List<Long> setupPlacementOrder;
    private int currentSetupPlacementIndex;
    private Map<Long, Integer> settlementsPlacedInSetupCount;
    private Map<Long, Integer> roadsPlacedInSetupCount;
    private boolean setupPhaseComplete;
    private Long currentTurnPlayerId; // added to track current turn player in engine
    private List<Long> gamePlayerList; // store the original player list for normal phase turn order

    // constants for game actions 
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

    private static final int MAX_ROADS_PER_PLAYER = 15; // Max roads a player can build

    public GameEngine(long gameId) {
        this.gameId = gameId;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        try (Connection connection = DriverManager.getConnection(
            "jdbc:postgresql://db:5432/catan", "postgres", "password")) {
            GameDAO gameDAO = new GameDAO(connection);
            Game game = gameDAO.findById(gameId);

            if (game == null) {
                logger.error("[GE_INIT] Game with ID {} not found.", gameId);
                throw new RuntimeException("Game with ID " + gameId + " not found for GameEngine.");
            }

            this.inProgress = game.isInProgress();
            this.gamePlayerList = new ArrayList<>(game.getPlayerList() != null ? game.getPlayerList() : Collections.emptyList());
            this.currentTurnPlayerId = game.getCurrentTurnPlayerId();
            this.setupPhaseComplete = game.isSetupPhaseComplete();
            this.currentSetupPlacementIndex = game.getCurrentSetupPlacementIndex();

            if (game.getSetupPlacementOrder() != null && !game.getSetupPlacementOrder().isEmpty()) {
                this.setupPlacementOrder = new ArrayList<>(game.getSetupPlacementOrder());
            } else {
                this.setupPlacementOrder = new ArrayList<>(); // should be initialized by CatanApplication on /start
            }
            if (game.getSettlementsPlacedInSetupCount() != null) {
                this.settlementsPlacedInSetupCount = new HashMap<>(game.getSettlementsPlacedInSetupCount());
            } else {
                this.settlementsPlacedInSetupCount = new HashMap<>();
            }
            if (game.getRoadsPlacedInSetupCount() != null) {
                this.roadsPlacedInSetupCount = new HashMap<>(game.getRoadsPlacedInSetupCount());
            } else {
                this.roadsPlacedInSetupCount = new HashMap<>();
            }

            // load or init bank and other game properties
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
            this.currentDiceRoll = game.getCurrentDiceRoll() != null ? game.getCurrentDiceRoll() : 0;
            this.robberLocation = game.getRobberLocation() != null ? game.getRobberLocation() : -1; // default to -1 if null

            if (game.isInProgress() && game.getJsonPlayers() != null && !game.getJsonPlayers().isEmpty() && !game.getJsonPlayers().equals("[]") && !game.getJsonPlayers().equals("{}")) {
                try {
                    this.players = mapper.readValue(game.getJsonPlayers(),
                        mapper.getTypeFactory().constructMapType(Map.class, Long.class, Player.class));
                } catch (JsonProcessingException e_deserialize) {
                    logger.error("[GE_INIT] CRITICAL: Failed to deserialize jsonPlayers for game {}: {}. Falling back.", gameId, e_deserialize.getMessage());
                    this.players = initializePlayersFromGameDTO(game); // fallback
                }
            } else {
                this.players = initializePlayersFromGameDTO(game); // new game or no existing player state
            }
            if (game.isInProgress() && game.getJsonHexes() != null && !game.getJsonHexes().isEmpty() && !game.getJsonHexes().equals("[]")) {
                this.jsonHexes = game.getJsonHexes();
                this.jsonVertices = game.getJsonVertices();
                this.jsonEdges = game.getJsonEdges();
                this.robberLocation = game.getRobberLocation() != null ? game.getRobberLocation() : -1;
                if (this.robberLocation <= 0 && this.jsonHexes != null) { 
                    try {
                        List<Hex> tempHexes = Arrays.asList(mapper.readValue(this.jsonHexes, Hex[].class));
                        for (Hex hex : tempHexes) {
                            if ("desert".equalsIgnoreCase(hex.getType())) {
                                this.robberLocation = hex.getId();
                                break;
                            }
                        }
                    } catch (Exception e_robber_find) { /* log */ }
                }
            } else { // new game, generate board
                BoardData generatedBoard = BoardGenerator.generateNewBoard();
                if (generatedBoard == null) { throw new RuntimeException("BoardGenerator failed."); }
                    this.jsonHexes = mapper.writeValueAsString(generatedBoard.hexes);
                    this.jsonVertices = mapper.writeValueAsString(generatedBoard.vertices);
                    this.jsonEdges = mapper.writeValueAsString(generatedBoard.edges);
                this.robberLocation = generatedBoard.initialRobberLocation;
            }
            deserializeBoard(); // deserialize all board components

            // re-assign colors (can stay as is)
            if (this.gamePlayerList != null && this.players != null) {
                 for (int i = 0; i < this.gamePlayerList.size(); i++) {
                    Long accountId = this.gamePlayerList.get(i);
                    if (accountId != null) {
                    Player playerToRecolor = this.players.get(accountId);
                    if (playerToRecolor != null) {
                            playerToRecolor.setColor(PLAYER_COLORS.get(i % PLAYER_COLORS.size()));
                        }
                    }
                }
            }
        } catch (SQLException | JsonProcessingException e_sql_json) {
            logger.error("[GE_INIT] SQL/JSON Exception during GameEngine construction for game ID: {}: {}", gameId, e_sql_json.getMessage(), e_sql_json);
            throw new RuntimeException("DB/JSON error in GameEngine constructor", e_sql_json);
        } catch (Exception e_outer) { 
            logger.error("[GE_INIT] Unexpected Exception during GameEngine construction for game ID: {}. Exception Type: {}, Message: {}", 
                         gameId, e_outer.getClass().getName(), e_outer.getMessage(), e_outer);
            throw new RuntimeException("Unexpected error in GameEngine constructor", e_outer);
        }
    }

    // helper method to initialize the players
    private Map<Long, Player> initializePlayersFromGameDTO(Game gameDto) {
        Map<Long, Player> initializedPlayers = new HashMap<>();
        if (gameDto != null && gameDto.getPlayerList() != null) {
            List<Long> playerIds = gameDto.getPlayerList(); // get the list of player IDs
            for (int i = 0; i < playerIds.size(); i++) {
                Long accountId = playerIds.get(i);
                if (accountId == null) {
                    continue; // skip null accountId
                }
                Player player = new Player(accountId);
                // assign color based on index from the static PLAYER_COLORS list
                if (i < PLAYER_COLORS.size()) {
                    player.setColor(PLAYER_COLORS.get(i)); 
                } else {
                    // This block should ideally not be hit if player cap is 4 and PLAYER_COLORS has 4 entries.
                    player.setColor("#808080"); // default to Gray
                }
                initializedPlayers.put(accountId, player);
            }
        } else {
            // logger.warn("[GE_HELPER] GameDTO or PlayerList was null. No players initialized from DTO.");
        }
        return initializedPlayers;
    }

    private void deserializeBoard() {
        ObjectMapper mapper = new ObjectMapper();
        // Configure mapper consistently with the constructor's mapper
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); 
        try {
            // HEXES
            Hex[] hexArray = null;
            if (jsonHexes != null && !jsonHexes.equalsIgnoreCase("null") && !jsonHexes.isEmpty() && !jsonHexes.equals("[]")) {
                hexArray = mapper.readValue(jsonHexes, Hex[].class);
            }
            this.hexes = (hexArray != null) ? Arrays.asList(hexArray) : new ArrayList<>();

            // VERTICES
            Vertex[] vertexArray = null;
            if (jsonVertices != null && !jsonVertices.equalsIgnoreCase("null") && !jsonVertices.isEmpty() && !jsonVertices.equals("[]")) {
                vertexArray = mapper.readValue(jsonVertices, Vertex[].class);
            }
            this.vertices = (vertexArray != null) ? Arrays.asList(vertexArray) : new ArrayList<>();

            // EDGES
            Edge[] edgeArray = null;
            if (jsonEdges != null && !jsonEdges.equalsIgnoreCase("null") && !jsonEdges.isEmpty() && !jsonEdges.equals("[]")) {
                edgeArray = mapper.readValue(jsonEdges, Edge[].class);
            }
            this.edges = (edgeArray != null) ? Arrays.asList(edgeArray) : new ArrayList<>();

        } catch (JsonProcessingException e) {
            logger.error("[GE_HELPER] Failed to deserialize board state: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deserialize board state", e);
        } catch (Exception e) {
             logger.error("[GE_HELPER] Unexpected error during board deserialization: {} - Source JSON Hexes: '{}', Vertices: '{}', Edges: '{}'", 
                          e.getMessage(), 
                          jsonHexes != null ? jsonHexes.substring(0, Math.min(jsonHexes.length(), 100)) : "null", // Log snippet
                          jsonVertices != null ? jsonVertices.substring(0, Math.min(jsonVertices.length(), 100)) : "null", // Log snippet
                          jsonEdges != null ? jsonEdges.substring(0, Math.min(jsonEdges.length(), 100)) : "null", // Log snippet
                          e);
            throw new RuntimeException("Unexpected error during board deserialization", e);
        }
    }

    // game actions

    public boolean placeSettlement(long playerId, long vertexId, boolean initialPlacement) {
        Player player = players.get(playerId);
        if (player == null) {
            return false;
        }

        Vertex vertex = vertices.get((int)vertexId);
        if (vertex == null || vertex.isOccupied()) {
            return false;
        }

        // check distance rule
        if (hasAdjacentSettlement((int)vertexId)) {
            return false;
        }

        // check if player has a connected road (except during initial placement)
        if (!initialPlacement && !hasConnectedRoad(playerId, (int)vertexId)) {
            return false;
        }

        // check resources (except during initial placement)
        if (!initialPlacement) {
            if (!player.hasResources(SETTLEMENT_BRICK, 0, SETTLEMENT_SHEEP, SETTLEMENT_WHEAT, SETTLEMENT_WOOD)) {
                return false;
            }
            
            // deduct resources
            player.deductResources(SETTLEMENT_BRICK, 0, SETTLEMENT_SHEEP, SETTLEMENT_WHEAT, SETTLEMENT_WOOD);
            updateBankResources(-SETTLEMENT_BRICK, 0, -SETTLEMENT_SHEEP, -SETTLEMENT_WHEAT, -SETTLEMENT_WOOD);
        }

        // place settlement
        vertex.setOccupied(true);
        vertex.setOwnerId(playerId);
        vertex.setBuildingType("settlement");
        player.incrementSettlements();
        player.addVictoryPoint();

        // persist changes
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

        // check resources
        if (!player.hasResources(0, CITY_ORE, 0, CITY_WHEAT, 0)) {
            return false;
        }

        // deduct resources
        player.deductResources(0, CITY_ORE, 0, CITY_WHEAT, 0);
        updateBankResources(0, -CITY_ORE, 0, -CITY_WHEAT, 0);

        // upgrade to city
        vertex.setBuildingType("city");
        player.decrementSettlements();
        player.incrementCities();
        player.addVictoryPoint();

        // persist changes
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

        // check if road is connected to player's existing road or settlement (except during initial placement)
        if (!initialPlacement && !isValidRoadPlacement(playerId, (int)edgeId)) {
            return false;
        }

        // check resources (except during initial placement)
        if (!initialPlacement) {
            if (!player.hasResources(ROAD_BRICK, 0, 0, 0, ROAD_WOOD)) {
                return false;
            }
            
            // deduct resources
            player.deductResources(ROAD_BRICK, 0, 0, 0, ROAD_WOOD);
            updateBankResources(-ROAD_BRICK, 0, 0, 0, -ROAD_WOOD);
        }

        // place road
        edge.setOccupied(true);
        edge.setOwnerId(playerId);
        player.incrementRoads();

        // check for longest road
        updateLongestRoad();

        // persist changes
        persistGameState();

        return true;
    }

    public boolean buyDevelopmentCard(long playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            return false;
        }

        // check if development cards are available
        if (bankKnight + bankYearOfPlenty + bankMonopoly + bankRoadBuilding + bankVictoryPoint == 0) {
            return false;
        }

        // check resources
        if (!player.hasResources(0, DEVELOPMENT_CARD_ORE, DEVELOPMENT_CARD_SHEEP, DEVELOPMENT_CARD_WHEAT, 0)) {
            return false;
        }

        // deduct resources
        player.deductResources(0, DEVELOPMENT_CARD_ORE, DEVELOPMENT_CARD_SHEEP, DEVELOPMENT_CARD_WHEAT, 0);
        updateBankResources(0, -DEVELOPMENT_CARD_ORE, -DEVELOPMENT_CARD_SHEEP, -DEVELOPMENT_CARD_WHEAT, 0);

        // randomly select a development card
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

        // move robber
        robberLocation = (int)newRobberHex;

        // rob player
        Player robbedPlayer = players.get(robbedPlayerId);
        if (robbedPlayer != null && robbedPlayer.getTotalResources() > 0) {
            String stolenResource = robbedPlayer.getRandomResource();
            if (stolenResource != null) {
                robbedPlayer.deductResource(stolenResource, 1);
                player.addResource(stolenResource, 1);
            }
        }

        // use knight
        player.useKnight();
        updateLargestArmy();

        return true;
    }

    public boolean playYearOfPlenty(long playerId, String resource1, String resource2) {
        Player player = players.get(playerId);
        if (player == null || !player.hasYearOfPlenty()) {
            return false;
        }

        // check if requested resources are available in bank
        if (!hasEnoughInBank(resource1, 1) || !hasEnoughInBank(resource2, 1)) {
            return false;
        }

        // give resources to player
        player.addResource(resource1, 1);
        player.addResource(resource2, 1);
        updateBankResource(resource1, -1);
        updateBankResource(resource2, -1);

        // use card
        player.useYearOfPlenty();
        return true;
    }

    public boolean playMonopoly(long playerId, String resource) {
        Player player = players.get(playerId);
        if (player == null || !player.hasMonopoly()) {
            return false;
        }

        // collect all of the specified resource from other players
        int totalCollected = 0;
        for (Player otherPlayer : players.values()) {
            if (otherPlayer.getAccountId() != playerId) {
                int amount = otherPlayer.getResourceAmount(resource);
                otherPlayer.deductResource(resource, amount);
                totalCollected += amount;
            }
        }

        // give collected resources to player
        player.addResource(resource, totalCollected);

        // use card
        player.useMonopoly();
        return true;
    }

    public boolean playRoadBuilding(long playerId, long edgeId1, long edgeId2) {
        Player player = players.get(playerId);
        if (player == null || !player.hasRoadBuilding()) {
            return false;
        }

        // try to build both roads
        boolean firstRoad = buildRoad(playerId, edgeId1, true);
        boolean secondRoad = buildRoad(playerId, edgeId2, true);

        if (firstRoad && secondRoad) {
            player.useRoadBuilding();
            return true;
        }

        // if either road failed, undo the successful one
        if (firstRoad) {
            undoRoad(edgeId1);
        }

        return false;
    }

    // helper methods

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
        
        // check connected vertices
        for (int vertexId : edge.getConnectedVertices()) {
            Vertex vertex = vertices.get(vertexId);
            if (vertex.isOccupied() && vertex.getOwnerId() == playerId) {
                return true;
            }
        }

        // check connected edges
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

        // update longest road status
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

        // start DFS from each edge owned by the player
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

        // explore connected edges
        for (int connectedEdgeId : edge.getConnectedEdges()) {
            Edge connectedEdge = edges.get(connectedEdgeId);
            if (connectedEdge.isOccupied() && connectedEdge.getOwnerId() == playerId) {
                maxLength = Math.max(maxLength, 1 + dfsRoadLength(connectedEdgeId, playerId, visited));
            }
        }

        return maxLength;
    }

    private void updateLargestArmy() {
        int maxKnights = 2;  // minimum 3 knights required for the status
        Long largestArmyPlayerId = null;

        for (Player player : players.values()) {
            if (player.getKnightUsed() > maxKnights) {
                maxKnights = player.getKnightUsed();
                largestArmyPlayerId = player.getAccountId();
            }
        }

        // update largest army status
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
        logger.debug("[GE_PERSIST] Attempting to persist game state for game ID: {}", this.gameId);
        String dbUrl = "jdbc:postgresql://db:5432/catan";
        String dbUser = "postgres";
        String dbPassword = "password"; 

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            GameDAO gameDAO = new GameDAO(connection);
            Game game = new Game(); // create a new DTO to populate for saving
            game.setGameId(this.gameId);

            // core game state
            game.setInProgress(this.inProgress);
            game.setIsGameOver(this.isGameOver);
            game.setWinnerId(this.winnerId);
            game.setCurrentTurnPlayerId(this.currentTurnPlayerId);
            game.setPlayerList(new ArrayList<>(this.gamePlayerList)); // Persist original player list
            game.setCurrentDiceRoll(this.currentDiceRoll);
            game.setRobberLocation(this.robberLocation);

            // board state (json)
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                
            game.setJsonHexes(mapper.writeValueAsString(this.hexes));
            game.setJsonVertices(mapper.writeValueAsString(this.vertices));
            game.setJsonEdges(mapper.writeValueAsString(this.edges));
            game.setJsonPlayers(mapper.writeValueAsString(this.players)); // persist updated player objects
            
            // bank resources
            game.setBankBrick(this.bankBrick);
            game.setBankOre(this.bankOre);
            game.setBankSheep(this.bankSheep);
            game.setBankWheat(this.bankWheat);
            game.setBankWood(this.bankWood);
            game.setBankYearOfPlenty(this.bankYearOfPlenty);
            game.setBankMonopoly(this.bankMonopoly);
            game.setBankRoadBuilding(this.bankRoadBuilding);
            game.setBankVictoryPoint(this.bankVictoryPoint);
            game.setBankKnight(this.bankKnight);

            // setup phase
            game.setSetupPlacementOrder(new ArrayList<>(this.setupPlacementOrder));
            game.setCurrentSetupPlacementIndex(this.currentSetupPlacementIndex);
            game.setSettlementsPlacedInSetupCount(new HashMap<>(this.settlementsPlacedInSetupCount));
            game.setRoadsPlacedInSetupCount(new HashMap<>(this.roadsPlacedInSetupCount));
            game.setSetupPhaseComplete(this.setupPhaseComplete);
            
            gameDAO.updateGameSetupState(game); 
            logger.debug("[GE_PERSIST] Game state successfully persisted for game ID: {}", this.gameId);

        } catch (SQLException | JsonProcessingException e) {
            logger.error("[GE_PERSIST] SQL/JSON Exception during persistGameState for game ID {}: {}", this.gameId, e.getMessage(), e);
            throw new RuntimeException("DB/JSON error during game state persistence", e);
        } catch (Exception e_persist_outer) { 
            logger.error("[GE_PERSIST] Unexpected exception during persistGameState for game ID {}: {}", this.gameId, e_persist_outer.getMessage(), e_persist_outer);
            throw new RuntimeException("Unexpected error during game state persistence", e_persist_outer);
        }
    }

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

    public boolean isInProgress() { return inProgress; }
    public void setInProgress(boolean inProgress) { this.inProgress = inProgress; }

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

    public Long getCurrentTurnPlayerId() { return this.currentTurnPlayerId; }
    public boolean isSetupPhaseComplete() { return this.setupPhaseComplete; }

    public boolean placeInitialSettlement(long accountId, int vertexId) {
        logger.debug("[GE_SETUP_SETTLE] Attempting initial settlement for account {} at vertex {}. SetupComplete: {}", accountId, vertexId, this.setupPhaseComplete);
        if (this.setupPhaseComplete) {
            logger.warn("[GE_SETUP_SETTLE] FAIL - Setup phase is already complete.");
            return false;
        }
        if (this.currentTurnPlayerId == null || accountId != this.currentTurnPlayerId) {
             logger.warn("[GE_SETUP_SETTLE] FAIL - Not player {}'s turn. Current turn is {}.", accountId, this.currentTurnPlayerId);
            return false;
        }

        int currentSettlementCountForPlayer = settlementsPlacedInSetupCount.getOrDefault(accountId, 0);
        logger.info("[GE_SETUP_SETTLE] Player {} currently has {} settlements placed in setup.", accountId, currentSettlementCountForPlayer);

        // max 2 settlements per player in setup
        if (currentSettlementCountForPlayer >= 2) {
            logger.warn("[GE_SETUP_SETTLE] FAIL - Player {} already placed {} settlements (max 2).", accountId, currentSettlementCountForPlayer);
            return false;
        }

        Vertex targetVertex = findVertexById(vertexId);
        if (targetVertex == null || targetVertex.isOccupied()) {
            logger.warn("[GE_SETUP_SETTLE] FAIL - Vertex {} is already occupied.", vertexId);
            return false;
        }
        if (hasAdjacentSettlement(vertexId)) {
             logger.warn("[GE_SETUP_SETTLE] FAIL - Placement at vertex {} violates distance rule due to occupied adjacent vertex.", vertexId);
             return false;
        }
        
        Player player = this.players.get(accountId);
        if (player == null) {
            logger.warn("[GE_SETUP_SETTLE] FAIL - Player with accountId {} not found in game engine players map.", accountId);
            return false;
        }

            targetVertex.setOwnerId(accountId);
            targetVertex.setBuildingType("settlement");
            targetVertex.setOccupied(true);
            player.incrementSettlements();
        player.addVictoryPoint();
        settlementsPlacedInSetupCount.put(accountId, settlementsPlacedInSetupCount.getOrDefault(accountId, 0) + 1);

        // number of players = gamePlayerList.size()
        // first round: placement index 0 to N-1 (S1)
        // second round: placement index N to 2N-1 (R1)
        // third round: placement index 2N to 3N-1 (S2 - resources)
        // fourth round: placement index 3N to 4N-1 (R2)
        boolean grantResourcesForThisSettlement = false;
        if (this.gamePlayerList != null && !this.gamePlayerList.isEmpty()) {
            int numPlayers = this.gamePlayerList.size();
            // Check if the current placement is for a second settlement (which occurs in the third block of N turns)
            if (this.currentSetupPlacementIndex >= (2 * numPlayers) && this.currentSetupPlacementIndex < (3 * numPlayers)) {
                grantResourcesForThisSettlement = true;
            }
        }

        if (grantResourcesForThisSettlement) {
            logger.info("[GE_SETUP_SETTLE] Second round settlement placement for account {}. Granting initial resources for settlement at {}. Index: {}, NumPlayers: {}", 
                        accountId, vertexId, this.currentSetupPlacementIndex, this.gamePlayerList != null ? this.gamePlayerList.size() : 0);
            List<Hex> adjacentHexesToSettlement = new ArrayList<>();
            // logic to find hexes adjacent to targetVertex (assuming Vertex has getAdjacentHexIds or similar)
            // this needs Vertex to be populated correctly by BoardGenerator and deserialization
            if (targetVertex.getAdjacentHexes() != null) { 
                for (Integer hexId : targetVertex.getAdjacentHexes()) {
                    Hex adjHex = this.hexes.stream().filter(h -> h.getId() == hexId).findFirst().orElse(null);
                    if (adjHex != null && !"desert".equalsIgnoreCase(adjHex.getType())) {
                        adjacentHexesToSettlement.add(adjHex);
                            }
                        }
                    }
                    if (!adjacentHexesToSettlement.isEmpty()) {
                player.addInitialResourcesFromSettlement(adjacentHexesToSettlement); 
                    } else {
                logger.warn("[GE_SETUP_SETTLE] No productive adjacent hexes found for vertex {} to grant initial resources.", vertexId);
            }
        }

        logger.info("[GE_SETUP_SETTLE] Settlement placed for {}. Count: {}. Advancing turn.", accountId, settlementsPlacedInSetupCount.get(accountId));
        advanceSetupTurn(); // persists game state
        return true;
    }

    public boolean placeInitialRoad(long accountId, int edgeId) {
        logger.debug("[GE_SETUP_ROAD] Attempting initial road for account {} at edge {}. SetupComplete: {}", accountId, edgeId, this.setupPhaseComplete);
        if (this.setupPhaseComplete) {
            logger.warn("[GE_SETUP_ROAD] FAIL - Setup phase is already complete.");
            return false;
        }
        if (this.currentTurnPlayerId == null || accountId != this.currentTurnPlayerId) {
            logger.warn("[GE_SETUP_ROAD] FAIL - Not player {}'s turn. Current turn is {}.", accountId, this.currentTurnPlayerId);
            return false;
        }
        // max 2 roads per player in setup
        if (roadsPlacedInSetupCount.getOrDefault(accountId, 0) >= 2) {
            logger.warn("[GE_SETUP_ROAD] FAIL - Player {} already placed 2 roads in setup.", accountId);
            return false;
        }
        // must have placed at least one settlement to place a road, and road count must not exceed settlement count during setup.
        if (settlementsPlacedInSetupCount.getOrDefault(accountId, 0) == 0 || 
            roadsPlacedInSetupCount.getOrDefault(accountId, 0) >= settlementsPlacedInSetupCount.getOrDefault(accountId, 0)) {
            logger.warn("[GE_SETUP_ROAD] FAIL - Player {} cannot place road yet (Settlements: {}, Roads: {}). Must place settlement first or road count exceeds settlement count for setup phase.", 
                        accountId, settlementsPlacedInSetupCount.getOrDefault(accountId, 0), roadsPlacedInSetupCount.getOrDefault(accountId, 0));
            return false;
        }

        Edge targetEdge = findEdgeById(edgeId);
        if (targetEdge == null || targetEdge.isOccupied()) {
            logger.warn("[GE_SETUP_ROAD] FAIL - Edge {} is already occupied.", edgeId);
            return false;
        }

        Player player = this.players.get(accountId);
        if (player == null || player.getNumRoads() >= MAX_ROADS_PER_PLAYER) {
            logger.warn("[GE_SETUP_ROAD] FAIL - Player {} already has the maximum number of roads ({}).", accountId, MAX_ROADS_PER_PLAYER);
            return false;
        }

        boolean connectedToPlayerSettlement = false;
        List<Integer> connectedVertexIds = targetEdge.getConnectedVertices();
        if (connectedVertexIds != null) {
            for (Integer vertexId : connectedVertexIds) {
                Vertex v = findVertexById(vertexId);
                if (v != null && v.isOccupied() && v.getOwnerId() != null && v.getOwnerId().equals(accountId)) {
                    connectedToPlayerSettlement = true;
                    break;
                }
            }
        }
        if (!connectedToPlayerSettlement) {
            logger.warn("[GE_SETUP_ROAD] FAIL - Road at Edge {} for player {} is not connected to any of their existing settlements for initial placement.", edgeId, accountId);
            return false;
        }
        
        targetEdge.setOwnerId(accountId);
        targetEdge.setOccupied(true);
        player.incrementRoads();
        roadsPlacedInSetupCount.put(accountId, roadsPlacedInSetupCount.getOrDefault(accountId, 0) + 1);

        logger.info("[GE_SETUP_ROAD] Road placed for {}. Count: {}. Advancing turn.", accountId, roadsPlacedInSetupCount.get(accountId));
        advanceSetupTurn(); // persists game state
            return true;
    }

    private void advanceSetupTurn() {
        if (this.setupPhaseComplete) {
            logger.info("[GE_ADV_TURN] Setup already complete. No turn advancement.");
            return;
        }

        this.currentSetupPlacementIndex++;
        logger.info("[GE_ADV_TURN] Advanced setup placement index to: {}", this.currentSetupPlacementIndex);

        if (this.setupPlacementOrder == null || this.setupPlacementOrder.isEmpty()) {
            logger.error("[GE_ADV_TURN] Setup placement order is null or empty! Cannot advance turn.");
            this.setupPhaseComplete = true; // mark as complete to go to regular game phase
            this.currentTurnPlayerId = (this.gamePlayerList != null && !this.gamePlayerList.isEmpty()) ? this.gamePlayerList.get(0) : null;
            persistGameState();
            return;
        }

        if (this.currentSetupPlacementIndex >= this.setupPlacementOrder.size()) {
            this.setupPhaseComplete = true;
            this.currentTurnPlayerId = (this.gamePlayerList != null && !this.gamePlayerList.isEmpty()) ? this.gamePlayerList.get(0) : null;
            logger.info("[GE_ADV_TURN] Setup phase complete! All placements made. Next turn player (normal phase): {}", this.currentTurnPlayerId);
        } else {
            this.currentTurnPlayerId = this.setupPlacementOrder.get(this.currentSetupPlacementIndex);
            logger.info("[GE_ADV_TURN] Next setup turn for player: {}. Index: {} of {}", this.currentTurnPlayerId, this.currentSetupPlacementIndex, this.setupPlacementOrder.size());
        }
        persistGameState(); // persist after every turn advancement in setup
    }

    // method to be called from CatanApplication when a setup action is requested
    // ensures correct player is taking action based on setup turn order
    public boolean performSetupAction(long requestingAccountId, String actionType, Integer locationId, boolean isSecondRoundRequest) {
        if (this.setupPhaseComplete) {
            logger.warn("[GE_PERFORM_SETUP] Action requested but setup phase is complete.");
            return false; 
        }
        if (this.currentTurnPlayerId == null || requestingAccountId != this.currentTurnPlayerId) {
            logger.warn("[GE_PERFORM_SETUP] Not player {}'s turn for setup action. Current: {}", requestingAccountId, this.currentTurnPlayerId);
            return false; 
        }

        // Validate actionType against currentSetupPlacementIndex
        int numPlayers = (this.gamePlayerList != null && !this.gamePlayerList.isEmpty()) ? this.gamePlayerList.size() : 0;
        if (numPlayers <= 0) { // Should not happen if game started correctly
            logger.error("[GE_PERFORM_SETUP] Number of players is {} or less, cannot determine expected setup action.", numPlayers);
            return false;
        }

        boolean expectedSettlementAction = false;
        boolean expectedRoadAction = false;

        // Determine expected action based on currentSetupPlacementIndex
        // S1: 0 to N-1
        // R1: N to 2N-1
        // S2: 2N to 3N-1
        // R2: 3N to 4N-1
        if (this.currentSetupPlacementIndex >= 0 && this.currentSetupPlacementIndex < numPlayers) { // S1
            expectedSettlementAction = true;
        } else if (this.currentSetupPlacementIndex >= numPlayers && this.currentSetupPlacementIndex < 2 * numPlayers) { // R1
            expectedRoadAction = true;
        } else if (this.currentSetupPlacementIndex >= 2 * numPlayers && this.currentSetupPlacementIndex < 3 * numPlayers) { // S2
            expectedSettlementAction = true;
        } else if (this.currentSetupPlacementIndex >= 3 * numPlayers && this.currentSetupPlacementIndex < 4 * numPlayers) { // R2
            expectedRoadAction = true;
        } else {
            logger.warn("[GE_PERFORM_SETUP] currentSetupPlacementIndex {} is out of bounds for {} players (Total setup actions: {}).", 
                        this.currentSetupPlacementIndex, numPlayers, 4 * numPlayers);
            return false; // Index is invalid
        }

        if ("SETTLEMENT".equalsIgnoreCase(actionType)) {
            if (!expectedSettlementAction) {
                logger.warn("[GE_PERFORM_SETUP] Received SETTLEMENT action, but expected a ROAD action for index {} with {} players.", this.currentSetupPlacementIndex, numPlayers);
                return false;
            }
            if (locationId == null) {
                 logger.warn("[GE_PERFORM_SETUP] VertexId is null for SETTLEMENT action.");
                 return false;
            }
            // isSecondRoundPlacement is determined internally now based on currentSetupPlacementIndex
            return placeInitialSettlement(requestingAccountId, locationId.intValue());
        } else if ("ROAD".equalsIgnoreCase(actionType)) {
            if (!expectedRoadAction) {
                logger.warn("[GE_PERFORM_SETUP] Received ROAD action, but expected a SETTLEMENT action for index {} with {} players.", this.currentSetupPlacementIndex, numPlayers);
                return false;
            }
            if (locationId == null) {
                logger.warn("[GE_PERFORM_SETUP] EdgeId is null for ROAD action.");
                return false;
            }
            return placeInitialRoad(requestingAccountId, locationId.intValue());
        }
        
        logger.warn("[GE_PERFORM_SETUP] Unknown action type received: {}", actionType);
        return false; // Should be caught by earlier checks if actionType is invalid.
    }

    // helper method to find an edge by its ID
    private Edge findEdgeById(int edgeId) {
        if (this.edges == null) {
            logger.warn("[GE_FIND_EDGE] Edge list is null. Cannot find edge {}.", edgeId);
            return null;
        }
        for (Edge e : this.edges) {
            if (e.getId() == edgeId) {
                return e;
            }
        }
        logger.warn("[GE_FIND_EDGE] Edge with ID {} not found in game engine's edges list.", edgeId);
        return null;
    }

    // helper method to find a vertex by its ID
    private Vertex findVertexById(int vertexId) {
        if (this.vertices == null) {
            logger.warn("[GE_FIND_VERTEX] Vertex list is null. Cannot find vertex {}.", vertexId);
            return null;
        }
        for (Vertex v : this.vertices) {
            if (v.getId() == vertexId) {
                return v;
            }
        }
        logger.warn("[GE_FIND_VERTEX] Vertex with ID {} not found in game engine's vertices list.", vertexId);
        return null;
    }

    // inner class to hold dice roll results
    public static class DiceRollResult {
        private final int dice1;
        private final int dice2;
        private final int sum;

        public DiceRollResult(int d1, int d2) {
            this.dice1 = d1;
            this.dice2 = d2;
            this.sum = d1 + d2;
        }

        public int getDice1() { return dice1; }
        public int getDice2() { return dice2; }
        public int getSum() { return sum; }
    }

    // helper to deduct from bank (assumes bank properties are member variables)
    private void deductFromBank(String resourceType, int amount) {
        switch (resourceType.toLowerCase()) {
            case "brick": this.bankBrick -= amount; break;
            case "wood": this.bankWood -= amount; break;
            case "ore": this.bankOre -= amount; break;
            case "sheep": this.bankSheep -= amount; break;
            case "wheat": this.bankWheat -= amount; break;
            default: logger.warn("[GE_BANK] Unknown resource type for bank deduction: {}", resourceType);
        }
    }

    // helper to check if bank has enough (assumes bank properties are member variables)
    private boolean bankHasEnough(String resourceType, int amount) {
        switch (resourceType.toLowerCase()) {
            case "brick": return this.bankBrick >= amount;
            case "wood": return this.bankWood >= amount;
            case "ore": return this.bankOre >= amount;
            case "sheep": return this.bankSheep >= amount;
            case "wheat": return this.bankWheat >= amount;
            default: 
                logger.warn("[GE_BANK] Unknown resource type for bank check: {}", resourceType);
                return false;
        }
    }

    public DiceRollResult rollDiceAndDistributeResources(long rollingPlayerAccountId) {
        // TODO: Add proper turn verification here. For single player, it's always their turn.
        // if (this.game.getCurrentTurnPlayerId() != rollingPlayerAccountId) { // Assuming Game DTO has current turn player
        //    throw new RuntimeException("Not your turn to roll dice.");
        // }

        Random random = new Random();
        int d1 = random.nextInt(6) + 1;
        int d2 = random.nextInt(6) + 1;
        this.currentDiceRoll = d1 + d2;
        logger.info("[GE_DICE] Player {} rolled {} + {} = {}.", rollingPlayerAccountId, d1, d2, this.currentDiceRoll);

        if (this.currentDiceRoll == 7) {
            logger.info("[GE_DICE] Rolled a 7! Robber event (not yet implemented further).");
            // TODO: Implement robber logic: move robber, discard cards if > 7, steal resource.
        } else {
            logger.info("[GE_DICE] Distributing resources for roll {}.", this.currentDiceRoll);
            for (Hex hex : this.hexes) {
                if (hex.getNumber() != null && hex.getNumber() == this.currentDiceRoll && 
                    !"desert".equalsIgnoreCase(hex.getType()) && hex.getId() != this.robberLocation) {
                    
                    String resourceType = hex.getType();
                    logger.debug("[GE_DICE] Hex {} ({}) matches roll. Resource: {}", hex.getId(), hex.getNumber(), resourceType);

                    // Find adjacent vertices to this hex
                    // This requires iterating vertices and checking their adjacentHexes list.
                    for (Vertex vertex : this.vertices) {
                        if (vertex.getAdjacentHexes() != null && vertex.getAdjacentHexes().contains(hex.getId())) {
                            if (vertex.isOccupied() && vertex.getOwnerId() != null) {
                                Player owner = this.players.get(vertex.getOwnerId());
                                if (owner != null) {
                                    int resourcesToGive = 0;
                                    if ("settlement".equalsIgnoreCase(vertex.getBuildingType())) {
                                        resourcesToGive = 1;
                                    } else if ("city".equalsIgnoreCase(vertex.getBuildingType())) {
                                        resourcesToGive = 2;
                                    }

                                    if (resourcesToGive > 0) {
                                        if (bankHasEnough(resourceType, resourcesToGive)) {
                                            owner.addResource(resourceType, resourcesToGive);
                                            deductFromBank(resourceType, resourcesToGive);
                                            logger.info("[GE_DICE] Player {} gets {} {} from hex {} via vertex {}. Player resources: brick={}, wood={}, ore={}, sheep={}, wheat={}. Bank: {}={}", 
                                                owner.getAccountId(), resourcesToGive, resourceType, hex.getId(), vertex.getId(),
                                                owner.getBrick(), owner.getWood(), owner.getOre(), owner.getSheep(), owner.getWheat(),
                                                resourceType, bankHasEnough(resourceType, 0) ? (resourceType.equals("brick") ? bankBrick : resourceType.equals("wood") ? bankWood : resourceType.equals("ore") ? bankOre : resourceType.equals("sheep") ? bankSheep : bankWheat) : "N/A");
                                        } else {
                                            logger.warn("[GE_DICE] Bank does not have enough {} to give to player {}. Needed: {}, Available: {} (approx)", 
                                                resourceType, owner.getAccountId(), resourcesToGive, 
                                                (resourceType.equals("brick") ? bankBrick : resourceType.equals("wood") ? bankWood : resourceType.equals("ore") ? bankOre : resourceType.equals("sheep") ? bankSheep : bankWheat));
                                        }
                                    }
                                } else {
                                    logger.warn("[GE_DICE] Owner {} of vertex {} not found in players map.", vertex.getOwnerId(), vertex.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
        persistGameState();
        return new DiceRollResult(d1, d2);
    }
}