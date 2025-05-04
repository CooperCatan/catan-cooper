package catan;

import catan.util.DataAccessObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GameStateDAO extends DataAccessObject<GameState> {

    private static final String GET_ONE =
        "SELECT * FROM game_state WHERE game_id=? AND turn_number=?";
    private static final String GET_LATEST =
        "SELECT * FROM game_state WHERE game_id=? ORDER BY turn_number DESC LIMIT 1";
    private static final String INSERT =
        "INSERT INTO game_state (game_id, turn_number, board_state, winner_id, robber_location, is_game_over, " +
        "bank_brick, bank_ore, bank_sheep, bank_wheat, bank_wood, bank_year_of_plenty, bank_monopoly, " +
        "bank_road_building, bank_victory_point, bank_knight) " +
        "VALUES (nextval('game_id_seq'), ?, ?::jsonb, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "RETURNING game_id";
    private static final String DELETE_GAME_ACTIONS = 
        "DELETE FROM game_action WHERE game_id=?";
    private static final String DELETE_TRADES = 
        "DELETE FROM trade WHERE game_id=?";
    private static final String DELETE_PLAYER_STATES = 
        "DELETE FROM player_state WHERE game_id=?";
    private static final String DELETE_GAME_STATE = 
        "DELETE FROM game_state WHERE game_id=?";
    private static final String GET_LONGEST_ROAD =
        "SELECT account_id FROM player_state " +
        "WHERE game_id=? AND turn_number=? AND longest_road=true";
    private static final String GET_LARGEST_ARMY =
        "SELECT account_id FROM player_state " +
        "WHERE game_id=? AND turn_number=? AND largest_army=true";

    public GameStateDAO(Connection connection) {
        super(connection);
    }

    @Override
    public GameState findById(long gameId) {
        try (PreparedStatement statement = this.connection.prepareStatement(GET_LATEST)) {
            statement.setLong(1, gameId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public GameState findByGameIdAndTurn(long gameId, long turnNumber) {
        try (PreparedStatement statement = this.connection.prepareStatement(GET_ONE)) {
            statement.setLong(1, gameId);
            statement.setLong(2, turnNumber);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public GameState create(GameState dto) {
        try (PreparedStatement statement = this.connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            // always start with turn 0 for a new game
            statement.setLong(1, 0);
            statement.setString(2, dto.getJsonHexes());
            statement.setString(3, dto.getJsonVertices());
            statement.setString(4, dto.getJsonEdges());
            statement.setBoolean(5, dto.isGameOver());
            statement.setLong(6, dto.getBankBrick());
            statement.setLong(7, dto.getBankOre());
            statement.setLong(8, dto.getBankSheep());
            statement.setLong(9, dto.getBankWheat());
            statement.setLong(10, dto.getBankWood());
            statement.setLong(11, dto.getBankYearOfPlenty());
            statement.setLong(12, dto.getBankMonopoly());
            statement.setLong(13, dto.getBankRoadBuilding());
            statement.setLong(14, dto.getBankVictoryPoint());
            statement.setLong(15, dto.getBankKnight());
                        
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating game state failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long gameId = generatedKeys.getLong(1);
                    return findByGameIdAndTurn(gameId, 0);
                } else {
                    throw new SQLException("Creating game state failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR - SQL State: " + e.getSQLState());
            System.err.println("ERROR - Error Code: " + e.getErrorCode());
            System.err.println("ERROR - Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create game state: " + e.getMessage(), e);
        }
    }

    public GameState update(GameState gameState) {
        String sql = "UPDATE game_state SET " +
                "json_hexes = ?, " +
                "json_vertices = ?, " +
                "json_edges = ?, " +
                "winner_id = ?, " +
                "is_game_over = ?, " +
                "bank_brick = ?, " +
                "bank_ore = ?, " +
                "bank_sheep = ?, " +
                "bank_wheat = ?, " +
                "bank_wood = ?, " +
                "bank_year_of_plenty = ?, " +
                "bank_monopoly = ?, " +
                "bank_road_building = ?, " +
                "bank_victory_point = ?, " +
                "bank_knight = ? " +
                "WHERE game_id = ? AND turn_number = ?";

        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, gameState.getJsonHexes());
            statement.setString(2, gameState.getJsonVertices());
            statement.setString(3, gameState.getJsonEdges());
            statement.setObject(4, gameState.getWinnerId());
            statement.setBoolean(5, gameState.isGameOver());
            statement.setLong(6, gameState.getBankBrick());
            statement.setLong(7, gameState.getBankOre());
            statement.setLong(8, gameState.getBankSheep());
            statement.setLong(9, gameState.getBankWheat());
            statement.setLong(10, gameState.getBankWood());
            statement.setLong(11, gameState.getBankYearOfPlenty());
            statement.setLong(12, gameState.getBankMonopoly());
            statement.setLong(13, gameState.getBankRoadBuilding());
            statement.setLong(14, gameState.getBankVictoryPoint());
            statement.setLong(15, gameState.getBankKnight());
            statement.setLong(16, gameState.getGameId());
            statement.setLong(17, gameState.getTurnNumber());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                return findByGameIdAndTurn(gameState.getGameId(), gameState.getTurnNumber());
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(long gameId) {
        try {

            try (PreparedStatement statement = this.connection.prepareStatement(DELETE_GAME_ACTIONS)) {
                statement.setLong(1, gameId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = this.connection.prepareStatement(DELETE_TRADES)) {
                statement.setLong(1, gameId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = this.connection.prepareStatement(DELETE_PLAYER_STATES)) {
                statement.setLong(1, gameId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = this.connection.prepareStatement(DELETE_GAME_STATE)) {
                statement.setLong(1, gameId);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting game with ID " + gameId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Long findLongestRoadHolder(long gameId, long turnNumber) {
        try (PreparedStatement statement = this.connection.prepareStatement(GET_LONGEST_ROAD)) {
            statement.setLong(1, gameId);
            statement.setLong(2, turnNumber);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("account_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public Long findLargestArmyHolder(long gameId, long turnNumber) {
        try (PreparedStatement statement = this.connection.prepareStatement(GET_LARGEST_ARMY)) {
            statement.setLong(1, gameId);
            statement.setLong(2, turnNumber);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("account_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    private GameState extractFromResultSet(ResultSet rs) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        GameState gameState = new GameState();
        gameState.setGameId(rs.getLong("game_id"));
        gameState.setTurnNumber(rs.getLong("turn_number"));
        Long winnerId = rs.getLong("winner_id");
        if (!rs.wasNull()) {
            gameState.setWinnerId(winnerId);
        }
        gameState.setGameOver(rs.getBoolean("is_game_over"));
        gameState.setBankBrick(rs.getLong("bank_brick"));
        gameState.setBankOre(rs.getLong("bank_ore"));
        gameState.setBankSheep(rs.getLong("bank_sheep"));
        gameState.setBankWheat(rs.getLong("bank_wheat"));
        gameState.setBankWood(rs.getLong("bank_wood"));
        gameState.setBankYearOfPlenty(rs.getLong("bank_year_of_plenty"));
        gameState.setBankMonopoly(rs.getLong("bank_monopoly"));
        gameState.setBankRoadBuilding(rs.getLong("bank_road_building"));
        gameState.setBankVictoryPoint(rs.getLong("bank_victory_point"));
        gameState.setBankKnight(rs.getLong("bank_knight"));
        return gameState;
    }
}
