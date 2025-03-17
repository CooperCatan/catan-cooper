package catan;

import catan.util.DataAccessObject;
import catan.util.DatabaseConnectionManager;
import catan.util.JDBCExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GameActionDAO implements DataAccessObject<GameAction> {
    private final DatabaseConnectionManager connectionManager;
    private final JDBCExecutor executor;

    public GameActionDAO() {
        this.connectionManager = new DatabaseConnectionManager();
        this.executor = new JDBCExecutor(connectionManager);
    }

    @Override
    public void create(GameAction gameAction) {
        String sql = "INSERT INTO game_actions (game_id, action_type, action_data) VALUES (?, ?, ?)";
        executor.executeUpdate(sql, gameAction.getGameId(), gameAction.getActionType(), gameAction.getActionData());
    }

    @Override
    public GameAction findById(long id) {
        String sql = "SELECT * FROM game_actions WHERE action_id = ?";
        return executor.executeQuery(sql, this::mapResultSetToGameAction, id);
    }

    @Override
    public List<GameAction> findAll() {
        String sql = "SELECT * FROM game_actions";
        return executor.executeQueryList(sql, this::mapResultSetToGameAction);
    }

    @Override
    public void update(GameAction gameAction) {
        String sql = "UPDATE game_actions SET game_id = ?, action_type = ?, action_data = ? WHERE action_id = ?";
        executor.executeUpdate(sql, gameAction.getGameId(), gameAction.getActionType(), gameAction.getActionData(), gameAction.getId());
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM game_actions WHERE action_id = ?";
        executor.executeUpdate(sql, id);
    }

    private GameAction mapResultSetToGameAction(ResultSet rs) throws SQLException {
        GameAction gameAction = new GameAction();
        gameAction.setId(rs.getLong("action_id"));
        gameAction.setGameId(rs.getLong("game_id"));
        gameAction.setActionType(rs.getString("action_type"));
        gameAction.setActionData(rs.getString("action_data"));
        return gameAction;
    }
}