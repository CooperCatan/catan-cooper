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

    public GameEngine(long gameId) {
        logger.info("[GE_INIT] Initializing GameEngine for game ID: {}", gameId);
        this.gameId = gameId;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        try (Connection connection = DriverManager.getConnection(
            "jdbc:postgresql://db:5432/catan",
            "postgres",
            "password"
        )) {
            GameDAO gameDAO = new GameDAO(connection);
            logger.info("[GE_INIT] Attempting to find game ID: {} in DAO", gameId);
            Game game = gameDAO.findById(gameId);
            logger.info("[GE_INIT] Game DTO fetched from DAO. Game Name: {}, PlayerList size: {}", 
                        (game != null ? game.getGameName() : "NULL_GAME_DTO"), 
                        (game != null && game.getPlayerList() != null ? game.getPlayerList().size() : "NULL_OR_EMPTY_PLAYER_LIST"));

            if (game == null) {
                logger.error("[GE_INIT] Game with ID {} not found. Throwing RuntimeException.", gameId);
                throw new RuntimeException("Game with ID " + gameId + " not found for GameEngine.");
            }

            // Load or Initialize Bank and other game properties
            logger.debug("[GE_INIT] Loading bank properties for game ID: {}", gameId);
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
            logger.debug("[GE_INIT] Bank properties loaded. Robber initial location from DTO: {}", this.robberLocation);

            if (game.isInProgress() && game.getJsonHexes() != null && !game.getJsonHexes().isEmpty() && !game.getJsonHexes().equals("[]")) {
                logger.info("[GE_INIT] Game ID: {} is already in progress. Loading existing board state from DB.", gameId);
                this.jsonHexes = game.getJsonHexes();
                this.jsonVertices = game.getJsonVertices();
                this.jsonEdges = game.getJsonEdges();
                this.jsonPlayers = game.getJsonPlayers();
                // load existing robber location if available and valid
                if (this.robberLocation <= 0 && this.jsonHexes != null) { // if robber location wasn't set or is invalid, try to find desert
                    try {
                        List<Hex> tempHexes = Arrays.asList(mapper.readValue(this.jsonHexes, Hex[].class));
                        for (Hex hex : tempHexes) {
                            if ("desert".equalsIgnoreCase(hex.getType())) {
                                this.robberLocation = hex.getId();
                                logger.info("[GE_INIT] Found desert hex ID {} from existing jsonHexes and set as robberLocation.", this.robberLocation);
                                break;
                            }
                        }
                    } catch (Exception e_robber_find) {
                        logger.warn("[GE_INIT] Could not parse existing jsonHexes to find desert for robber location: {}", e_robber_find.getMessage());
                    }
                }
                if (this.robberLocation <= 0) {
                     logger.warn("[GE_INIT] Robber location still invalid ({}) after checking existing DB state.", this.robberLocation);
                }
                 logger.debug("[GE_INIT] Existing board JSON and robber location ({}) loaded from DB.", this.robberLocation);
                
                if (this.jsonPlayers != null && !this.jsonPlayers.isEmpty() && !this.jsonPlayers.equals("[]") && !this.jsonPlayers.equals("{}")) {
                    try {
                        logger.debug("[GE_INIT] Deserializing existing jsonPlayers for game ID: {}", gameId);
                        this.players = mapper.readValue(this.jsonPlayers,
                            mapper.getTypeFactory().constructMapType(Map.class, Long.class, Player.class));
                        logger.debug("[GE_INIT] jsonPlayers deserialized successfully for game ID: {}. Player count: {}", gameId, (this.players != null ? this.players.size() : 0));
                    } catch (JsonProcessingException e_deserialize) { 
                        logger.error("[GE_INIT] CRITICAL: Failed to deserialize jsonPlayers for game {} even after ignoring unknown properties. Error: {}", gameId, e_deserialize.getMessage(), e_deserialize);
                        logger.warn("[GE_INIT] Falling back to re-initializing players from DTO due to deserialization error.");
                        this.players = initializePlayersFromGameDTO(game);
                        try { 
                            this.jsonPlayers = mapper.writeValueAsString(this.players); 
                            logger.info("[GE_INIT] Successfully serialized re-initialized players after deserialization failure.");
                        } catch (JsonProcessingException e_serialize_fallback) { 
                            logger.error("[GE_INIT] CRITICAL: Failed to serialize re-initialized players during fallback: {}", e_serialize_fallback.getMessage(), e_serialize_fallback);
                            this.jsonPlayers = "{}"; 
                        }
                    }
                } else {
                    logger.info("[GE_INIT] jsonPlayers is empty or null for in-progress game {}. Initializing from DTO.", gameId);
                    this.players = initializePlayersFromGameDTO(game);
                    try {
                       this.jsonPlayers = mapper.writeValueAsString(this.players);
                       logger.info("[GE_INIT] Successfully serialized players initialized from DTO (jsonPlayers was initially empty).");
                    } catch (JsonProcessingException e_player_init) {
                        logger.error("[GE_INIT] Failed to serialize players initialized from DTO (jsonPlayers was initially empty): {}", e_player_init.getMessage(), e_player_init);
                        this.jsonPlayers = "{}"; 
                    }
                }
            } else {
                logger.info("[GE_INIT] Game ID: {} is NEW or board not set up. Generating initial board state using BoardGenerator.", gameId);
                
                // --- Call BoardGenerator --- 
                BoardData generatedBoard = BoardGenerator.generateNewBoard();
                
                if (generatedBoard == null || generatedBoard.hexes == null || generatedBoard.hexes.isEmpty()) {
                    logger.error("[GE_INIT] CRITICAL: BoardGenerator returned null or empty board data! Cannot initialize GameEngine.");
                    throw new RuntimeException("Failed to generate initial board state.");
                }
                
                // --- Serialize generated board data to JSON strings --- 
                try {
                    this.jsonHexes = mapper.writeValueAsString(generatedBoard.hexes);
                    this.jsonVertices = mapper.writeValueAsString(generatedBoard.vertices);
                    this.jsonEdges = mapper.writeValueAsString(generatedBoard.edges);
                } catch (JsonProcessingException e_serialize_board) {
                     logger.error("[GE_INIT] CRITICAL: Failed to serialize generated board state to JSON: {}", e_serialize_board.getMessage(), e_serialize_board);
                    throw new RuntimeException("Failed to serialize generated board state.", e_serialize_board);
                }
                
                // --- Set robber location from generated data ---
                this.robberLocation = generatedBoard.initialRobberLocation;
                logger.info("[GE_INIT] Board generated. Robber location set to: {}. Serialized JSON stored.", this.robberLocation);

                // --- Initialize players (as it's a new game setup) --- 
                logger.info("[GE_INIT] Initializing players from Game DTO for new game ID: {}", gameId);
                this.players = initializePlayersFromGameDTO(game); 
                try {
                    this.jsonPlayers = mapper.writeValueAsString(this.players);
                    logger.info("[GE_INIT] jsonPlayers serialized for new game ID: {}.", gameId);
                } catch (JsonProcessingException e_serialize_new_players) {
                    logger.error("[GE_INIT] Failed to serialize initial players state for new game: {}", e_serialize_new_players.getMessage(), e_serialize_new_players);
                    this.jsonPlayers = "{}"; 
                }
            }

            logger.info("[GE_INIT] Deserializing final board components (hexes, vertices, edges) for game ID: {}", gameId);
            deserializeBoard();
            // --- DEBUG LOG: Check edges after deserialization ---
            if (this.edges != null && !this.edges.isEmpty()) {
                logger.debug("[GE_INIT_DEBUG] Edges loaded after deserializeBoard(). Count: {}. First few edges:", this.edges.size());
                for (int i = 0; i < Math.min(5, this.edges.size()); i++) {
                    Edge edge = this.edges.get(i);
                    logger.debug("[GE_INIT_DEBUG]   - Edge ID: {}, Occupied: {}, Owner: {}, ConnectedVertices: {}", 
                                 edge.getId(), edge.isOccupied(), edge.getOwnerId(), 
                                 (edge.getConnectedVertices() != null ? edge.getConnectedVertices().toString() : "null"));
                }
            } else {
                logger.warn("[GE_INIT_DEBUG] No edges loaded or edges list is null after deserializeBoard().");
            }
            // --- END DEBUG LOG ---
            logger.info("[GE_INIT] GameEngine construction successful for game ID: {}", gameId);

        } catch (SQLException e_sql) {
            logger.error("[GE_INIT] SQLException during GameEngine construction for game ID: {}: {}", gameId, e_sql.getMessage(), e_sql);
            throw new RuntimeException("Failed to connect to database or query game in GameEngine", e_sql);
        } catch (Exception e_outer) { 
            logger.error("[GE_INIT] Unexpected Exception during GameEngine construction for game ID: {}: {}", gameId, e_outer.getMessage(), e_outer);
            throw new RuntimeException("Unexpected error in GameEngine constructor", e_outer);
        }
    }

    // helper method to initialize the players ,ap
    private Map<Long, Player> initializePlayersFromGameDTO(Game gameDto) {
        logger.debug("[GE_HELPER] Initializing players from DTO. GameDto is null: {}, PlayerList is null: {}", 
            (gameDto == null), 
            (gameDto != null ? gameDto.getPlayerList() == null : "N/A"));
        Map<Long, Player> initializedPlayers = new HashMap<>();
        if (gameDto != null && gameDto.getPlayerList() != null) {
            List<Long> playerIds = gameDto.getPlayerList(); // get the list of player IDs
            logger.debug("[GE_HELPER] PlayerList size: {}", playerIds.size());
            for (int i = 0; i < playerIds.size(); i++) {
                Long accountId = playerIds.get(i);
                logger.debug("[GE_HELPER] Creating Player object for accountId: {}", accountId);
                Player player = new Player(accountId);
                // assign color based on index from the static PLAYER_COLORS list
                if (i < PLAYER_COLORS.size()) {
                    player.setColor(PLAYER_COLORS.get(i)); // Use static list
                    logger.debug("[GE_HELPER] Assigned color {} to player index {}", PLAYER_COLORS.get(i), i); // Use static list
                } else {
                    // fallback if more players than colors (e.g., reuse or default)
                    player.setColor("#808080"); // Default to Gray
                    logger.warn("[GE_HELPER] More players than defined colors. Assigning default gray to player index {}", i);
                }
                initializedPlayers.put(accountId, player);
            }
        }
        logger.debug("[GE_HELPER] Finished initializing players from DTO. Count: {}", initializedPlayers.size());
        return initializedPlayers;
    }

    private void deserializeBoard() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.hexes = Arrays.asList(mapper.readValue(jsonHexes, Hex[].class));

            this.vertices = Arrays.asList(mapper.readValue(jsonVertices, Vertex[].class));

            this.edges = Arrays.asList(mapper.readValue(jsonEdges, Edge[].class));
            logger.debug("[GE_HELPER] Board components (Hexes: {}, Vertices: {}, Edges: {}) deserialized.", 
                         (this.hexes != null ? this.hexes.size() : 0),
                         (this.vertices != null ? this.vertices.size() : 0),
                         (this.edges != null ? this.edges.size() : 0));
        } catch (JsonProcessingException e) {
            logger.error("[GE_HELPER] Failed to deserialize board state: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deserialize board state", e);
        } catch (Exception e) {
             logger.error("[GE_HELPER] Unexpected error during board deserialization: {}", e.getMessage(), e);
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

        // use try-with-resources for the Connection
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            logger.debug("[GE_PERSIST] Database connection successful for game ID: {}", this.gameId);
            GameDAO gameDAO = new GameDAO(connection);
            Game game = gameDAO.findById(gameId); 

            if (game != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                
                String jsonHexesStr, jsonVerticesStr, jsonEdgesStr, jsonPlayersStr; 
                
                try {
                    logger.debug("[GE_PERSIST] Serializing game state components for game ID: {}", this.gameId);
                    jsonHexesStr = mapper.writeValueAsString(this.hexes);
                    jsonVerticesStr = mapper.writeValueAsString(this.vertices);
                    jsonEdgesStr = mapper.writeValueAsString(this.edges);
                    jsonPlayersStr = mapper.writeValueAsString(this.players);
                } catch (JsonProcessingException e) {
                    logger.error("[GE_PERSIST] Failed to serialize game state for game ID {}: {}", this.gameId, e.getMessage(), e);
                    throw new RuntimeException("Failed to serialize game state", e);
                }

                //  update the Game DTO
                game.setJsonHexes(jsonHexesStr);
                game.setJsonVertices(jsonVerticesStr);
                game.setJsonEdges(jsonEdgesStr);
                game.setJsonPlayers(jsonPlayersStr);
                game.setCurrentDiceRoll(this.currentDiceRoll);
                game.setRobberLocation(this.robberLocation);
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

                // persist the updated Game DTO 
                logger.debug("[GE_PERSIST] Calling gameDAO.updateGameState for game ID: {}", this.gameId);
                gameDAO.updateGameState(game); 
                logger.info("[GE_PERSIST] Game state successfully persisted for game ID: {}", this.gameId);

            } else {
                logger.warn("[GE_PERSIST] Game with ID {} not found by DAO during persist operation. Cannot save state.", this.gameId);
            }
        } catch (SQLException e) {
            logger.error("[GE_PERSIST] SQLException occurred during database operation for game ID {}: {}", this.gameId, e.getMessage(), e);
            if (e.getMessage().contains("authentication failed") || e.getMessage().contains("Connection refused")) {
                 logger.error("[GE_PERSIST] DB Connection attempt details - URL: {}, User: {}", dbUrl, dbUser);
            }
            throw new RuntimeException("Database error during game state persistence", e);
        } catch (Exception e) { 
            logger.error("[GE_PERSIST] Unexpected exception during persistGameState for game ID {}: {}", this.gameId, e.getMessage(), e);
            throw new RuntimeException("Unexpected error during game state persistence", e);
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

    public boolean placeInitialSettlement(long accountId, int vertexId, boolean isSecondRoundPlacement) {
        logger.debug("[GE_SETUP_SETTLE] START - accountId: {}, vertexId: {}, isSecond: {}", 
                    accountId, vertexId, isSecondRoundPlacement);

        Vertex targetVertex = null;
        logger.debug("[GE_SETUP_SETTLE] Finding vertex with ID: {}", vertexId);
        for (Vertex v : this.vertices) {
            if (v.getId() == vertexId) {
                targetVertex = v;
                break;
            }
        }

        if (targetVertex == null) {
            logger.warn("[GE_SETUP_SETTLE] FAIL - Vertex {} not found in game engine's vertices list.", vertexId);
            return false;
        }
        logger.debug("[GE_SETUP_SETTLE] Found vertex: {}. Checking occupation status.", vertexId);

        if (targetVertex.isOccupied()) {
            logger.warn("[GE_SETUP_SETTLE] FAIL - Vertex {} is already occupied by player {}. Cannot place settlement.", vertexId, targetVertex.getOwnerId());
            return false;
        }
        logger.debug("[GE_SETUP_SETTLE] Vertex {} is not occupied. Checking distance rule.", vertexId);

        // distance rule Check
        if (hasAdjacentSettlement(vertexId)) {
             logger.warn("[GE_SETUP_SETTLE] FAIL - Placement at vertex {} violates distance rule due to occupied adjacent vertex.", vertexId);
             return false;
        }
        logger.debug("[GE_SETUP_SETTLE] Distance rule check passed for vertex {}.", vertexId);
        
        logger.debug("[GE_SETUP_SETTLE] Finding player with accountId: {}", accountId);
        Player player = this.players.get(accountId);
        if (player == null) {
            logger.warn("[GE_SETUP_SETTLE] FAIL - Player with accountId {} not found in game engine players map.", accountId);
            return false; // Should not happen if GameEngine initialized correctly
        }
        logger.debug("[GE_SETUP_SETTLE] Found player: {}. Proceeding to place settlement.", accountId);

        // all checks passed, place the settlement
        try {
            logger.debug("[GE_SETUP_SETTLE] Setting properties on vertex {}: ownerId={}, buildingType=\"settlement\", occupied=true", vertexId, accountId);
            targetVertex.setOwnerId(accountId);
            targetVertex.setBuildingType("settlement");
            targetVertex.setOccupied(true);
            logger.info("[GE_SETUP_SETTLE] Vertex {} successfully occupied by account {}. Updating player stats.", vertexId, accountId);

            player.incrementSettlements();
            player.addVictoryPoint(); // Initial settlements grant VPs
            logger.debug("[GE_SETUP_SETTLE] Player {} settlements incremented to {}, VPs to {}. Checking if second round placement.", 
                         accountId, player.getNumSettlements(), player.getVictoryPoints());

            // grant resources if it's the second placement in setup phase
            if (isSecondRoundPlacement) {
                logger.info("[GE_SETUP_SETTLE] Second round placement for account {}. Attempting to grant initial resources.", accountId);
                List<Hex> adjacentHexesToSettlement = new ArrayList<>();
                List<Integer> adjacentHexIds = targetVertex.getAdjacentHexes(); // This list is currently expected to be empty
                logger.debug("[GE_SETUP_SETTLE] Vertex {} adjacentHexes count: {}", vertexId, (adjacentHexIds == null ? 0 : adjacentHexIds.size()));
                if (adjacentHexIds != null && !adjacentHexIds.isEmpty()) { // This block will likely be skipped
                    logger.debug("[GE_SETUP_SETTLE] Finding adjacent hex objects for vertex {}...", vertexId);
                    for (int hexId : adjacentHexIds) {
                         logger.trace("[GE_SETUP_SETTLE] Looking for hex ID: {}", hexId);
                        for (Hex hex : this.hexes) {
                            if (hex.getId() == hexId) {
                                adjacentHexesToSettlement.add(hex);
                                 logger.trace("[GE_SETUP_SETTLE] Added hex {} to list for resource granting.", hexId);
                                break;
                            }
                        }
                    }
                    if (!adjacentHexesToSettlement.isEmpty()) {
                         logger.debug("[GE_SETUP_SETTLE] Granting resources from {} adjacent hexes to player {}. Check player state for details.", 
                                      adjacentHexesToSettlement.size(), accountId);
                        player.addInitialResourcesFromSettlement(adjacentHexesToSettlement);
                    } else {
                        logger.warn("[GE_SETUP_SETTLE] Found adjacent hex IDs for vertex {}, but could not find corresponding Hex objects.", vertexId);
                    }
                } else {
                    logger.warn("[GE_SETUP_SETTLE] No adjacent hex IDs found for vertex {} (or list was null/empty). Cannot grant initial resources.", vertexId);
                }
            } else {
                 logger.debug("[GE_SETUP_SETTLE] Not second round placement, skipping resource grant.");
            }

            logger.debug("[GE_SETUP_SETTLE] Attempting to persist game state after placing settlement for account {} at vertex {}.", accountId, vertexId);
            persistGameState(); // Save changes to DB via GameDAO
            logger.info("[GE_SETUP_SETTLE] SUCCESS - Initial settlement placed and game state persisted for account {} at vertex {}.", accountId, vertexId);
            return true;
        } catch (Exception e) {
             logger.error("[GE_SETUP_SETTLE] FAIL - Exception occurred *after* checks passed, during vertex/player update or persist: {}", e.getMessage(), e);
             // Attempt to rollback vertex changes? Might be complex depending on state.
             // For now, just log the error and return false.
             // Consider adding rollback logic if needed.
             return false;
        }
        // courtesy of claude
    }


}