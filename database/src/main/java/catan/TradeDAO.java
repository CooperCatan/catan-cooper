package catan;

import catan.util.DataAccessObject;
import catan.util.DatabaseConnectionManager;
import catan.util.JDBCExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TradeDAO implements DataAccessObject<Trade> {
    private final DatabaseConnectionManager connectionManager;
    private final JDBCExecutor executor;

    public TradeDAO() {
        this.connectionManager = new DatabaseConnectionManager();
        this.executor = new JDBCExecutor(connectionManager);
    }

    @Override
    public void create(Trade trade) {
        String sql = "INSERT INTO trades (player_id, resource, amount) VALUES (?, ?, ?)";
        executor.executeUpdate(sql, trade.getPlayerId(), trade.getResource(), trade.getAmount());
    }

    @Override
    public Trade findById(long id) {
        String sql = "SELECT * FROM trades WHERE trade_id = ?";
        return executor.executeQuery(sql, this::mapResultSetToTrade, id);
    }

    @Override
    public List<Trade> findAll() {
        String sql = "SELECT * FROM trades";
        return executor.executeQueryList(sql, this::mapResultSetToTrade);
    }

    @Override
    public void update(Trade trade) {
        String sql = "UPDATE trades SET player_id = ?, resource = ?, amount = ? WHERE trade_id = ?";
        executor.executeUpdate(sql, trade.getPlayerId(), trade.getResource(), trade.getAmount(), trade.getId());
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM trades WHERE trade_id = ?";
        executor.executeUpdate(sql, id);
    }

    private Trade mapResultSetToTrade(ResultSet rs) throws SQLException {
        Trade trade = new Trade();
        trade.setId(rs.getLong("trade_id"));
        trade.setPlayerId(rs.getLong("player_id"));
        trade.setResource(rs.getString("resource"));
        trade.setAmount(rs.getInt("amount"));
        return trade;
    }
}