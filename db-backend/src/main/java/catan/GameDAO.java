package catan;

import catan.util.DataAccessObject;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.HashMap;

public class GameDAO extends DataAccessObject<Game> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GET_ONE = "SELECT * FROM game WHERE game_id=?";
    private static final String GET_ALL = "SELECT * FROM game";
    private static final String GET_ACTIVE_GAMES = "SELECT * FROM game WHERE is_game_over=false ORDER BY created_at DESC";
    private static final String GET_COMPLETED_GAMES = "SELECT * FROM game WHERE is_game_over=true ORDER BY created_at DESC";
    private static final String DELETE_EMPTY_GAMES = "DELETE FROM game WHERE array_length(player_list, 1) = 0 AND in_progress = true";
    private static final String INSERT = "INSERT INTO game (player_list, winner_id, is_game_over, in_progress, game_name, " +
        "json_hexes, json_vertices, json_edges, json_players, current_dice_roll, robber_location, " +
        "bank_brick, bank_ore, bank_sheep, bank_wheat, bank_wood, " +
        "bank_year_of_plenty, bank_monopoly, bank_road_building, bank_victory_point, bank_knight, " +
        "current_turn_player_id, setup_phase_complete, current_setup_placement_index, setup_placement_order, settlements_placed_in_setup_count, roads_placed_in_setup_count) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING game_id";
    private static final String DELETE = "DELETE FROM game WHERE game_id=?";
    private static final String ADD_PLAYER = "UPDATE game SET player_list = array_append(player_list, ?) WHERE game_id=?";
    private static final String REMOVE_PLAYER = "UPDATE game SET player_list = array_remove(player_list, ?) WHERE game_id=?";
    private static final String UPDATE_GAME_STATE = "UPDATE game SET " +
        "json_hexes=?, json_vertices=?, json_edges=?, json_players=?, " +
        "current_dice_roll=?, robber_location=?, " +
        "bank_brick=?, bank_ore=?, bank_sheep=?, bank_wheat=?, bank_wood=?, " +
        "bank_year_of_plenty=?, bank_monopoly=?, bank_road_building=?, bank_victory_point=?, bank_knight=?, " +
        "in_progress=?, is_game_over=?, winner_id=? " +
        "WHERE game_id=?";
    private static final String UPDATE_GAME_SETUP_STATE = "UPDATE game SET " +
        "json_hexes=?, json_vertices=?, json_edges=?, json_players=?, " +
        "in_progress=?, current_turn_player_id=?, robber_location=?, " +
        "setup_phase_complete=?, current_setup_placement_index=?, " +
        "setup_placement_order=?, settlements_placed_in_setup_count=?, roads_placed_in_setup_count=? " +
        "WHERE game_id=?";

    public GameDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Game findById(long id) {
        try (PreparedStatement statement = this.connection.prepareStatement(GET_ONE)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return extractFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Game> findAll() {
        List<Game> games = new ArrayList<>();
        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery(GET_ALL);
            while (rs.next()) {
                games.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return games;
    }

    public List<Game> findActiveGames() {
        List<Game> games = new ArrayList<>();
        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery(GET_ACTIVE_GAMES);
            while (rs.next()) {
                games.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return games;
    }

    public List<Game> findCompletedGames() {
        List<Game> games = new ArrayList<>();
        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery(GET_COMPLETED_GAMES);
            while (rs.next()) {
                games.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return games;
    }

    @Override
    public Game create(Game game) {
        try (PreparedStatement statement = this.connection.prepareStatement(INSERT)) {
            System.out.println("[DEBUG] Starting game creation in GameDAO");
            
            // init empty array for player list
            System.out.println("[DEBUG] Creating player array with list: " + game.getPlayerList());
            Array playerArray = connection.createArrayOf("bigint", game.getPlayerList().toArray());
            statement.setArray(1, playerArray);
            statement.setNull(2, java.sql.Types.BIGINT); // winner_id
            statement.setBoolean(3, game.isGameOver());
            statement.setBoolean(4, game.isInProgress());
            statement.setString(5, game.getGameName());
            
            // set game state with null checks
            statement.setString(6, game.getJsonHexes() != null ? game.getJsonHexes() : "[]");
            statement.setString(7, game.getJsonVertices() != null ? game.getJsonVertices() : "[]");
            statement.setString(8, game.getJsonEdges() != null ? game.getJsonEdges() : "[]");
            statement.setString(9, game.getJsonPlayers() != null ? game.getJsonPlayers() : "[]");
            setNullableInt(statement, 10, game.getCurrentDiceRoll());
            setNullableInt(statement, 11, game.getRobberLocation());
            setNullableInt(statement, 12, game.getBankBrick());
            setNullableInt(statement, 13, game.getBankOre());
            setNullableInt(statement, 14, game.getBankSheep());
            setNullableInt(statement, 15, game.getBankWheat());
            setNullableInt(statement, 16, game.getBankWood());
            setNullableInt(statement, 17, game.getBankYearOfPlenty());
            setNullableInt(statement, 18, game.getBankMonopoly());
            setNullableInt(statement, 19, game.getBankRoadBuilding());
            setNullableInt(statement, 20, game.getBankVictoryPoint());
            setNullableInt(statement, 21, game.getBankKnight());

            setNullableLong(statement, 22, game.getCurrentTurnPlayerId());
            statement.setBoolean(23, game.isSetupPhaseComplete());
            statement.setInt(24, game.getCurrentSetupPlacementIndex());
            try {
                statement.setString(25, game.getSetupPlacementOrder() != null ? objectMapper.writeValueAsString(game.getSetupPlacementOrder()) : "[]");
                statement.setString(26, game.getSettlementsPlacedInSetupCount() != null ? objectMapper.writeValueAsString(game.getSettlementsPlacedInSetupCount()) : "{}");
                statement.setString(27, game.getRoadsPlacedInSetupCount() != null ? objectMapper.writeValueAsString(game.getRoadsPlacedInSetupCount()) : "{}");
            } catch (JsonProcessingException e) {
                System.out.println("[ERROR] JSON processing error in create (setup fields): " + e.getMessage());
                e.printStackTrace();
                statement.setString(25, "[]");
                statement.setString(26, "{}");
                statement.setString(27, "{}");
            }
            
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                long gameId = rs.getLong("game_id");
                game.setGameId(gameId); 
                Game found = findById(gameId);
                return found;
            } else {
                System.out.println("[ERROR] No game ID returned from INSERT");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] SQL error in create: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.out.println("[ERROR] Unexpected error in create: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(long id) {
        try (PreparedStatement statement = this.connection.prepareStatement(DELETE)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Game addPlayer(long gameId, long playerId) {
        try (PreparedStatement statement = this.connection.prepareStatement(ADD_PLAYER)) {
            statement.setLong(1, playerId);
            statement.setLong(2, gameId);
            if (statement.executeUpdate() > 0) {
                return findById(gameId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public Game removePlayer(long gameId, long playerId) {
        try (PreparedStatement statement = this.connection.prepareStatement(REMOVE_PLAYER)) {
            statement.setLong(1, playerId);
            statement.setLong(2, gameId);
            if (statement.executeUpdate() > 0) {
                return findById(gameId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public void deleteEmptyGames() {
        try (PreparedStatement statement = this.connection.prepareStatement(DELETE_EMPTY_GAMES)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Game extractFromResultSet(ResultSet rs) throws SQLException {
        Game game = new Game();
        game.setGameId(rs.getLong("game_id"));
        Array playerArray = rs.getArray("player_list");
        if (playerArray != null) {
            Long[] playerIds = (Long[]) playerArray.getArray();
            game.setPlayerList(new ArrayList<>(Arrays.asList(playerIds)));
        }
        game.setWinnerId(rs.getLong("winner_id"));
        if (rs.wasNull()) {
            game.setWinnerId(null);
        }
        game.setIsGameOver(rs.getBoolean("is_game_over"));
        game.setInProgress(rs.getBoolean("in_progress"));
        game.setCreatedAt(rs.getTimestamp("created_at"));
        game.setGameName(rs.getString("game_name"));
        
        // extract game state
        game.setJsonHexes(rs.getString("json_hexes"));
        game.setJsonVertices(rs.getString("json_vertices"));
        game.setJsonEdges(rs.getString("json_edges"));
        game.setJsonPlayers(rs.getString("json_players"));
        game.setCurrentDiceRoll(rs.getInt("current_dice_roll"));
        if (rs.wasNull()) game.setCurrentDiceRoll(null);
        game.setRobberLocation(rs.getInt("robber_location"));
        if (rs.wasNull()) game.setRobberLocation(null);
        game.setBankBrick(rs.getInt("bank_brick"));
        if (rs.wasNull()) game.setBankBrick(null);
        game.setBankOre(rs.getInt("bank_ore"));
        if (rs.wasNull()) game.setBankOre(null);
        game.setBankSheep(rs.getInt("bank_sheep"));
        if (rs.wasNull()) game.setBankSheep(null);
        game.setBankWheat(rs.getInt("bank_wheat"));
        if (rs.wasNull()) game.setBankWheat(null);
        game.setBankWood(rs.getInt("bank_wood"));
        if (rs.wasNull()) game.setBankWood(null);
        game.setBankYearOfPlenty(rs.getInt("bank_year_of_plenty"));
        if (rs.wasNull()) game.setBankYearOfPlenty(null);
        game.setBankMonopoly(rs.getInt("bank_monopoly"));
        if (rs.wasNull()) game.setBankMonopoly(null);
        game.setBankRoadBuilding(rs.getInt("bank_road_building"));
        if (rs.wasNull()) game.setBankRoadBuilding(null);
        game.setBankVictoryPoint(rs.getInt("bank_victory_point"));
        if (rs.wasNull()) game.setBankVictoryPoint(null);
        game.setBankKnight(rs.getInt("bank_knight"));
        if (rs.wasNull()) game.setBankKnight(null);
        
        System.out.println("[DEBUG_EXTRACT] Extracting game ID: " + game.getId());

        try {
            Long cTurnPlayerId = (Long) rs.getObject("current_turn_player_id");
            game.setCurrentTurnPlayerId(cTurnPlayerId);
            System.out.println("[DEBUG_EXTRACT] game ID " + game.getId() + " - current_turn_player_id: " + cTurnPlayerId);

            boolean spComplete = rs.getBoolean("setup_phase_complete");
            game.setSetupPhaseComplete(spComplete);
            System.out.println("[DEBUG_EXTRACT] game ID " + game.getId() + " - setup_phase_complete: " + spComplete);

            int csPlacementIndex = rs.getInt("current_setup_placement_index");
            game.setCurrentSetupPlacementIndex(csPlacementIndex);
            System.out.println("[DEBUG_EXTRACT] game ID " + game.getId() + " - current_setup_placement_index: " + csPlacementIndex);

            String setupOrderJson = rs.getString("setup_placement_order");
            System.out.println("[DEBUG_EXTRACT] game ID " + game.getId() + " - setup_placement_order (JSON string): " + setupOrderJson);
            if (setupOrderJson != null && !setupOrderJson.isEmpty() && !"null".equalsIgnoreCase(setupOrderJson.trim())) {
                game.setSetupPlacementOrder(objectMapper.readValue(setupOrderJson, new TypeReference<List<Long>>(){}));
            } else {
                game.setSetupPlacementOrder(new ArrayList<>());
                 System.out.println("[DEBUG_EXTRACT] game ID " + game.getId() + " - setup_placement_order parsed as empty list due to null/empty/\'null\' string.");
            }

            String settlementsCountJson = rs.getString("settlements_placed_in_setup_count");
            System.out.println("[DEBUG_EXTRACT] game ID " + game.getId() + " - settlements_placed_in_setup_count (JSON string): " + settlementsCountJson);
            if (settlementsCountJson != null && !settlementsCountJson.isEmpty() && !"null".equalsIgnoreCase(settlementsCountJson.trim())) {
                game.setSettlementsPlacedInSetupCount(objectMapper.readValue(settlementsCountJson, new TypeReference<Map<Long, Integer>>(){}));
            } else {
                game.setSettlementsPlacedInSetupCount(new HashMap<>());
                System.out.println("[DEBUG_EXTRACT] game ID " + game.getId() + " - settlements_placed_in_setup_count parsed as empty map due to null/empty/\'null\' string.");
            }

            String roadsCountJson = rs.getString("roads_placed_in_setup_count");
            System.out.println("[DEBUG_EXTRACT] game ID " + game.getId() + " - roads_placed_in_setup_count (JSON string): " + roadsCountJson);
            if (roadsCountJson != null && !roadsCountJson.isEmpty() && !"null".equalsIgnoreCase(roadsCountJson.trim())) {
                game.setRoadsPlacedInSetupCount(objectMapper.readValue(roadsCountJson, new TypeReference<Map<Long, Integer>>(){}));
            } else {
                game.setRoadsPlacedInSetupCount(new HashMap<>());
                System.out.println("[DEBUG_EXTRACT] game ID " + game.getId() + " - roads_placed_in_setup_count parsed as empty map due to null/empty/\'null\' string.");
            }
             System.out.println("[DEBUG_EXTRACT] game ID " + game.getId() + " - Successfully processed all setup fields.");

        } catch (JsonProcessingException e) {
            System.err.println("[ERROR_EXTRACT] JSON parsing error for game ID " + game.getId() + ": " + e.getMessage());
            e.printStackTrace(); 
            game.setSetupPlacementOrder(new ArrayList<>());
            game.setSettlementsPlacedInSetupCount(new HashMap<>());
            game.setRoadsPlacedInSetupCount(new HashMap<>());
        } 
        System.out.println("[DEBUG_EXTRACT] Successfully extracted all fields for game ID: " + game.getId());
        return game;
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value != null) {
            statement.setInt(index, value);
        } else {
            statement.setNull(index, java.sql.Types.INTEGER);
        }
    }

    private void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value != null) {
            statement.setLong(index, value);
        } else {
            statement.setNull(index, java.sql.Types.BIGINT);
        }
    }

    public void updateGameState(Game game) throws SQLException {
        String sql = "UPDATE games SET " +
                "json_hexes = ?, json_vertices = ?, json_edges = ?, json_players = ?, " +
                "current_dice_roll = ?, robber_location = ?, " +
                "bank_brick = ?, bank_ore = ?, bank_sheep = ?, bank_wheat = ?, bank_wood = ?, " +
                "bank_year_of_plenty = ?, bank_monopoly = ?, bank_road_building = ?, " +
                "bank_victory_point = ?, bank_knight = ?, winner_id = ?, is_game_over = ?, " +
                "in_progress = ?, current_turn_player_id = ? " +
                "WHERE game_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, game.getJsonHexes());
            pstmt.setString(2, game.getJsonVertices());
            pstmt.setString(3, game.getJsonEdges());
            pstmt.setString(4, game.getJsonPlayers());
            pstmt.setObject(5, game.getCurrentDiceRoll()); 
            pstmt.setObject(6, game.getRobberLocation()); 
            pstmt.setInt(7, game.getBankBrick());
            pstmt.setInt(8, game.getBankOre());
            pstmt.setInt(9, game.getBankSheep());
            pstmt.setInt(10, game.getBankWheat());
            pstmt.setInt(11, game.getBankWood());
            pstmt.setInt(12, game.getBankYearOfPlenty());
            pstmt.setInt(13, game.getBankMonopoly());
            pstmt.setInt(14, game.getBankRoadBuilding());
            pstmt.setInt(15, game.getBankVictoryPoint());
            pstmt.setInt(16, game.getBankKnight());
            pstmt.setObject(17, game.getWinnerId()); 
            pstmt.setBoolean(18, game.isGameOver());
            pstmt.setBoolean(19, game.isInProgress());
            pstmt.setObject(20, game.getCurrentTurnPlayerId());
            pstmt.setLong(21, game.getId());
            pstmt.executeUpdate();
        }
    }

    public void updateGameSetupState(Game game) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_GAME_SETUP_STATE)) {
            pstmt.setString(1, game.getJsonHexes());
            pstmt.setString(2, game.getJsonVertices());
            pstmt.setString(3, game.getJsonEdges());
            pstmt.setString(4, game.getJsonPlayers());
            pstmt.setBoolean(5, game.isInProgress());
            setNullableLong(pstmt, 6, game.getCurrentTurnPlayerId()); 
            setNullableInt(pstmt, 7, game.getRobberLocation());
            pstmt.setBoolean(8, game.isSetupPhaseComplete());
            pstmt.setInt(9, game.getCurrentSetupPlacementIndex());
            
            try {
                pstmt.setString(10, objectMapper.writeValueAsString(game.getSetupPlacementOrder()));
                pstmt.setString(11, objectMapper.writeValueAsString(game.getSettlementsPlacedInSetupCount()));
                pstmt.setString(12, objectMapper.writeValueAsString(game.getRoadsPlacedInSetupCount()));
            } catch (JsonProcessingException e) {
                // handle or rethrow as a SQLException or a RuntimeException
                throw new SQLException("Error serializing setup data to JSON", e);
            }
            
            pstmt.setLong(13, game.getId());
            pstmt.executeUpdate();
        }
    }
}
