package catan;

import catan.util.DataAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerDAO extends DataAccessObject<Player> {

    private static final String GET_ONE = "SELECT * " +
            "FROM player WHERE player_id=?";
    private static final String INSERT = "INSERT INTO player (user_name, password, total_games, total_wins, total_losses) " +
            " VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE player SET user_name=?, password=?, total_games=?, total_wins=?, total_losses=? " +
            " WHERE player_id = ?";
    public PlayerDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Player findById(long id) {
        Player player = new Player();
        try(PreparedStatement statement = this.connection.prepareStatement(GET_ONE);) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                player.setPlayerId(rs.getLong("player_id"));
                player.setUserName(rs.getString("user_name"));
                player.setPassword(rs.getString("password"));
                player.setTotalGames(rs.getInt("total_games"));
                player.setTotalWins(rs.getInt("total_wins"));
                player.setTotalLosses(rs.getInt("total_losses"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return player;
    }

    @Override
    public Player create(Player dto) {
        try(PreparedStatement statement = this.connection.prepareStatement(INSERT);) {
            // counts from 1!!
            statement.setString(1, dto.getUserName());
            statement.setString(2, dto.getPassword());
            statement.setInt(3, dto.getTotalGames());
            statement.setInt(4, dto.getTotalWins());
            statement.setInt(5, dto.getTotalLosses());
            statement.execute();
            int id = this.getLastVal(PLAYER_SEQUENCE);
            return this.findById(id);
            //return dto;
        } catch(SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player update(Player dto) {
        try(PreparedStatement statement = this.connection.prepareStatement(UPDATE);) {
            // counts from 1!!
            statement.setString(1, dto.getUserName());
            statement.setString(2, dto.getPassword());
            statement.setInt(3, dto.getTotalGames());
            statement.setInt(4, dto.getTotalWins());
            statement.setInt(5, dto.getTotalLosses());
            statement.setLong(6, dto.getId());
            statement.execute();
            return this.findById(dto.getId());
        } catch(SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}