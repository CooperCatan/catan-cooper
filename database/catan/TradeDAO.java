package catan;

import catan.util.DataAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TradeDAO extends DataAccessObject<Trade> {

    private static final String CREATE = "INSERT INTO trades (game_id, player_id, resource, amount) VALUES (?, ?, ?, ?)";
    private static final String READ = "SELECT * FROM trades WHERE id = ?";
    private static final String UPDATE = "UPDATE trades SET game_id = ?, player_id = ?, resource = ?, amount = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM trades WHERE id = ?";

    public TradeDAO(Connection connection){
        super(connection);
    }

    @Override
    public Trade create(Trade trade){
        try(PreparedStatement stmt = connection.prepareStatement(CREATE)){
            stmt.setLong(1, trade.getGameId());
            stmt.setLong(2, trade.getPlayerId());
            stmt.setString(3, trade.getResource());
            stmt.setInt(4, trade.getAmount());
            stmt.executeUpdate();
            return trade;
    }
    
    @Override
    public Trade read(long id){
        try(PreparedStatement stmt = connection.prepareStatement(READ)){
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? new Trade(rs) : null;
        }
    }

    @Override
    public Trade update(Trade trade){
        try(PreparedStatement stmt = connection.prepareStatement(UPDATE)){
            stmt.setLong(1, trade.getGameId());
            stmt.setLong(2, trade.getPlayerId());
            stmt.setString(3, trade.getResource());
            stmt.setInt(4, trade.getAmount());
            stmt.setLong(5, trade.getId());
        }
    }

    @Override
    public Trade delete(Trade trade){
        try(PreparedStatement stmt = connection.prepareStatement(DELETE)){
            stmt.setLong(1, trade.getId());
            stmt.executeUpdate();
            return trade;
        }
    }
}