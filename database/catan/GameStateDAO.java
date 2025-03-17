package catan;

import catan.util.DataAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameStateDAO extends DataAccessObject<GameState> {

    private static final String CREATE = "INSERT INTO game_states (game_id, turn, action_type) VALUES (?, ?, ?)";
    private static final String READ = "SELECT * FROM game_states WHERE id = ?";
    private static final String UPDATE = "UPDATE game_states SET game_id = ?, turn = ?, action_type = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM game_states WHERE id = ?";

    public GameStateDAO(Connection connection){
        super(connection);
    }

    @Override
    public GameState findById(long id) {
        return null;
    }

    @Override
    public GameState create(GameState gameState){
        return null;
    }

    public GameState read(long id){
        try(PreparedStatement stmt = connection.prepareStatement(READ)){
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? new GameState(rs) : null;
        }
    }

    public GameState update(GameState gameState){
        try(PreparedStatement stmt = connection.prepareStatement(UPDATE)){
            stmt.setLong(1, gameState.getGameId());
            stmt.setLong(2, gameState.getTurn());
            stmt.setString(3, gameState.getActionType());
        }
    }

    public GameState delete(GameState gameState){
        try(PreparedStatement stmt = connection.prepareStatement(DELETE)){
            stmt.setLong(1, gameState.getId());
            stmt.executeUpdate();
            return gameState;
        }
    }   
}