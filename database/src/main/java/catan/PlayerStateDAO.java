package catan;

import catan.util.DataAccessObject;
import catan.util.DatabaseConnectionManager;
import catan.util.JDBCExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PlayerStateDAO implements DataAccessObject<PlayerState> {
    private final DatabaseConnectionManager connectionManager;
    private final JDBCExecutor executor;

    public PlayerStateDAO() {
        this.connectionManager = new DatabaseConnectionManager();
        this.executor = new JDBCExecutor(connectionManager);
    }

    @Override
    public void create(PlayerState playerState) {
        String sql = "INSERT INTO player_states (game_id, player_id, resource, amount) VALUES (?, ?, ?, ?)";
        executor.executeUpdate(sql, playerState.getGameId(), playerState.getPlayerId(), playerState.getResource(), playerState.getAmount());
    }

    @Override
    public PlayerState findById(long id) {
        String sql = "SELECT * FROM player_states WHERE state_id = ?";
        return executor.executeQuery(sql, this::mapResultSetToPlayerState, id);
    }

    @Override
    public List<PlayerState> findAll() {
        String sql = "SELECT * FROM player_states";
        return executor.executeQueryList(sql, this::mapResultSetToPlayerState);
    }

    @Override
    public void update(PlayerState playerState) {
        String sql = "UPDATE player_states SET game_id = ?, player_id = ?, resource = ?, amount = ? WHERE state_id = ?";
        executor.executeUpdate(sql, playerState.getGameId(), playerState.getPlayerId(), playerState.getResource(), playerState.getAmount(), playerState.getId());
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM player_states WHERE state_id = ?";
        executor.executeUpdate(sql, id);
    }

    private PlayerState mapResultSetToPlayerState(ResultSet rs) throws SQLException {
        PlayerState playerState = new PlayerState();
        playerState.setId(rs.getLong("state_id"));
        playerState.setGameId(rs.getLong("game_id"));
        playerState.setPlayerId(rs.getLong("player_id"));
        playerState.setResource(rs.getString("resource"));
        playerState.setAmount(rs.getInt("amount"));
        return playerState;
    }
} 