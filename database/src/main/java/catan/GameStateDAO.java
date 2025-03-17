package catan;

import catan.util.DataAccessObject;
import catan.util.DatabaseConnectionManager;
import catan.util.JDBCExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GameStateDAO implements DataAccessObject<GameState> {
    private final DatabaseConnectionManager connectionManager;
    private final JDBCExecutor executor;

    public GameStateDAO() {
        this.connectionManager = new DatabaseConnectionManager();
        this.executor = new JDBCExecutor(connectionManager);
    }

    @Override
    public void create(GameState gameState) {
        String sql = "INSERT INTO game_states (game_id, turn, action_type, state_data) VALUES (?, ?, ?, ?)";
        executor.executeUpdate(sql, gameState.getGameId(), gameState.getTurn(), gameState.getActionType(), gameState.getStateData());
    }

    @Override
    public GameState findById(long id) {
        String sql = "SELECT * FROM game_states WHERE state_id = ?";
        return executor.executeQuery(sql, this::mapResultSetToGameState, id);
    }

    @Override
    public List<GameState> findAll() {
        String sql = "SELECT * FROM game_states";
        return executor.executeQueryList(sql, this::mapResultSetToGameState);
    }

    @Override
    public void update(GameState gameState) {
        String sql = "UPDATE game_states SET game_id = ?, turn = ?, action_type = ?, state_data = ? WHERE state_id = ?";
        executor.executeUpdate(sql, gameState.getGameId(), gameState.getTurn(), gameState.getActionType(), gameState.getStateData(), gameState.getId());
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM game_states WHERE state_id = ?";
        executor.executeUpdate(sql, id);
    }

    private GameState mapResultSetToGameState(ResultSet rs) throws SQLException {
        GameState gameState = new GameState();
        gameState.setId(rs.getLong("state_id"));
        gameState.setGameId(rs.getLong("game_id"));
        gameState.setTurn(rs.getLong("turn"));
        gameState.setActionType(rs.getString("action_type"));
        gameState.setStateData(rs.getString("state_data"));
        return gameState;
    }
}