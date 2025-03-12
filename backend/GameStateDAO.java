package your.package.name;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GameStateDAO {
    @Autowired
    private DataSource dataSource;

    // C - create GameState
    public GameState createGameState(JSONObject boardState) throws SQLException {
        String sql = "INSERT INTO game_state (turn_number, board_state) VALUES (1, ?::jsonb) RETURNING game_id, turn_number";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, boardState.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    GameState game = new GameState();
                    game.setGameId(rs.getLong("game_id"));
                    game.setTurnNumber(rs.getLong("turn_number"));
                    game.setBoardState(boardState);
                    return game;
                }
            }
        }
        throw new SQLException("Failed to create game state");
    }

    // R - get current game state
    public GameState getCurrentGameState(Long gameId) throws SQLException {
        String sql = "SELECT * FROM game_state WHERE game_id = ? ORDER BY turn_number DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, gameId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    GameState game = new GameState();
                    game.setGameId(rs.getLong("game_id"));
                    game.setTurnNumber(rs.getLong("turn_number"));
                    game.setWinnerId(rs.getObject("winner_id", Long.class));
                    game.setRobberLocation(new JSONObject(rs.getString("robber_location")));
                    game.setIsGameOver(rs.getBoolean("is_game_over"));
                    game.setBoardState(new JSONObject(rs.getString("board_state")));
                    return game;
                }
            }
        }
        return null;
    }

    // U - deleteGame (mark as over)
    public void markGameOver(Long gameId, Long winnerId) throws SQLException {
        GameState current = getCurrentGameState(gameId);
        String sql = "INSERT INTO game_state (game_id, turn_number, winner_id, robber_location, is_game_over, board_state) VALUES (?, ?, ?, ?::jsonb, ?, ?::jsonb)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, gameId);
            stmt.setLong(2, current.getTurnNumber() + 1);
            stmt.setLong(3, winnerId);
            stmt.setString(4, current.getRobberLocation().toString());
            stmt.setBoolean(5, true);
            stmt.setString(6, current.getBoardState().toString());
            stmt.executeUpdate();
        }
    }

    // R - longestRoadCalc, longestArmyCalc (placeholder for now)
    public Long calculateLongestRoad(Long gameId) throws SQLException {
        // TBD: Implement logic to calculate longest road
        return 0L;
    }

    public Long calculateLargestArmy(Long gameId) throws SQLException {
        // TBD: Implement logic to calculate largest army
        return 0L;
    }
}
