package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class TradeDAO {
    @Autowired
    private DataSource dataSource;

    // C - createPlayerTrade, createBankTrade
    public Trade createPlayerTrade(Long gameId, Long turnNumber, Long fromPlayerId, Long toPlayerId, String givenResource, Long givenAmount, String receivedResource, Long receivedAmount) throws SQLException {
        String sql = "INSERT INTO trade (game_id, turn_number, from_player_id, to_player_id, given_resource, given_amount, received_resource, received_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING trade_id";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, gameId);
            stmt.setLong(2, turnNumber);
            stmt.setLong(3, fromPlayerId);
            stmt.setLong(4, toPlayerId);
            stmt.setString(5, givenResource);
            stmt.setLong(6, givenAmount);
            stmt.setString(7, receivedResource);
            stmt.setLong(8, receivedAmount);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Trade trade = new Trade();
                    trade.setTradeId(rs.getLong("trade_id"));
                    trade.setGameId(gameId);
                    trade.setTurnNumber(turnNumber);
                    trade.setFromPlayerId(fromPlayerId);
                    trade.setToPlayerId(toPlayerId);
                    trade.setGivenResource(givenResource);
                    trade.setGivenAmount(givenAmount);
                    trade.setReceivedResource(receivedResource);
                    trade.setReceivedAmount(receivedAmount);
                    return trade;
                }
            }
        }
        throw new SQLException("Failed to create trade");
    }

    public Trade createBankTrade(Long gameId, Long turnNumber, Long fromPlayerId, String givenResource, Long givenAmount, String receivedResource, Long receivedAmount) throws SQLException {
        return createPlayerTrade(gameId, turnNumber, fromPlayerId, null, givenResource, givenAmount, receivedResource, receivedAmount);
    }

    // U - Accept trade (implied)
    public void acceptTrade(Long tradeId) throws SQLException {
        String sql = "UPDATE trade SET is_accepted = TRUE WHERE trade_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, tradeId);
            stmt.executeUpdate();
        }
    }

    // D - Delete trade (implied)
    public void deleteTrade(Long tradeId) throws SQLException {
        String sql = "DELETE FROM trade WHERE trade_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, tradeId);
            stmt.executeUpdate();
        }
    }
}
