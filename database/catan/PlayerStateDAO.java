package catan;

import catan.util.DataAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerStateDAO extends DataAccessObject<PlayerState> {

    private static final String CREATE = "INSERT INTO player_states (game_id, turn, action_type) VALUES (?, ?, ?)";
    private static final String READ = "SELECT * FROM game_states WHERE id = ?";
    private static final String UPDATE = "UPDATE game_states SET game_id = ?, turn = ?, action_type = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM game_states WHERE id = ?";

    public GameStateDAO(Connection connection){
        super(connection);
    }

    @Override
    public PlayerState findById(long id) {
        return null;
    }

    @Override
    public PlayerState create(PlayerState playerState){
        return null;
    }

    public PlayerState read(long id){
        try(PreparedStatement stmt = connection.prepareStatement(READ)){
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? new PlayerState(rs) : null;
        }
    }

    public PlayerState update(PlayerState playerState){
        try(PreparedStatement stmt = connection.prepareStatement(UPDATE)){
            stmt.setLong(1, playerState.getGameId());
            stmt.setLong(2, playerState.getTurn());
            stmt.setString(3, playerState.getActionType());
        }
    }

    public PlayerState delete(PlayerState playerState){
        try(PreparedStatement stmt = connection.prepareStatement(DELETE)){
            stmt.setLong(1, playerState.getId());
            stmt.executeUpdate();
            return playerState;
        }
    }
}