package catan;

import catan.util.DataAccessObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameStateDAO extends DataAccessObject<GameState> {
    private static final String GET_ONE = "SELECT * FROM game_state WHERE game_id=? AND turn_number=?";
    private static final String GET_LATEST = "SELECT * FROM game_state WHERE game_id=? ORDER BY turn_number DESC LIMIT 1";
    private static final String INSERT = "INSERT INTO game_state (game_id, turn_number, board_state, winner_id, robber_location, is_game_over, " +
            "bank_brick, bank_ore, bank_sheep, bank_wheat, bank_wood, bank_year_of_plenty, bank_monopoly, " +
            "bank_road_building, bank_victory_point, bank_knight) " +
            "VALUES (?, ?, ?::jsonb, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE = "DELETE FROM game_state WHERE game_id=?";
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

    public GameState findByGameIdAndTurn(long gameId, long turnNumber) {
        try (PreparedStatement statement = this.connection.prepareStatement(GET_ONE)) {
            statement.setLong(1, gameId);
            statement.setLong(2, turnNumber);
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

    @Override
    public GameState create(GameState dto) {
        try (PreparedStatement statement = this.connection.prepareStatement(INSERT)) {
            statement.setLong(1, dto.getGameId());
            statement.setLong(2, dto.getTurnNumber());
            statement.setString(3, dto.getBoardState() != null ? dto.getBoardState().toString() : "{}");
            if (dto.getWinnerId() != null) {
                statement.setLong(4, dto.getWinnerId());
            } else {
                statement.setNull(4, java.sql.Types.BIGINT);
            }
            statement.setString(5, dto.getRobberLocation() != null ? dto.getRobberLocation().toString() : "{\"hex\": \"desert\"}");
            statement.setBoolean(6, dto.isGameOver());
            statement.setLong(7, dto.getBankBrick());
            statement.setLong(8, dto.getBankOre());
            statement.setLong(9, dto.getBankSheep());
            statement.setLong(10, dto.getBankWheat());
            statement.setLong(11, dto.getBankWood());
            statement.setLong(12, dto.getBankYearOfPlenty());
            statement.setLong(13, dto.getBankMonopoly());
            statement.setLong(14, dto.getBankRoadBuilding());
            statement.setLong(15, dto.getBankVictoryPoint());
            statement.setLong(16, dto.getBankKnight());
            
            statement.execute();
            return findByGameIdAndTurn(dto.getGameId(), dto.getTurnNumber());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(long gameId) {
        try (PreparedStatement statement = this.connection.prepareStatement(DELETE)) {
            statement.setLong(1, gameId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Long findLongestRoadHolder(long gameId, long turnNumber) {
        try (PreparedStatement statement = this.connection.prepareStatement(GET_LONGEST_ROAD)) {
            statement.setLong(1, gameId);
            statement.setLong(2, turnNumber);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getLong("account_id");
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
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getLong("account_id");
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
        try {
            String boardStateStr = rs.getString("board_state");
            if (boardStateStr != null) {
                gameState.setBoardState(mapper.readTree(boardStateStr));
            }
            String robberLocationStr = rs.getString("robber_location");
            if (robberLocationStr != null) {
                gameState.setRobberLocation(mapper.readTree(robberLocationStr));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
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