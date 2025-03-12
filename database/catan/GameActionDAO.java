package catan;

import catan.util.DataAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameActionDAO extends DataAccessObject<GameAction> {

    private static final String CREATE = "INSERT INTO game_actions (game_id, turn, action_type) VALUES (?, ?, ?)";
    private static final String READ = "SELECT * FROM game_actions WHERE id = ?";
    private static final String UPDATE = "UPDATE game_actions SET game_id = ?, turn = ?, action_type = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM game_actions WHERE id = ?";

    public GameActionDAO(Connection connection){
        super(connection);
    }

    @Override
    public GameAction create(GameAction gameAction){
        try(PreparedStatement stmt = connection.prepareStatement(CREATE)){
            stmt.setLong(1, gameAction.getGameId());
            stmt.setLong(2, gameAction.getTurn());
            stmt.setString(3, gameAction.getActionType());
            stmt.executeUpdate();
            return gameAction;
        }
    }

    public GameAction read(long id){
        try(PreparedStatement stmt = connection.prepareStatement(READ)){
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? new GameAction(rs) : null;
        }
    }

    public GameAction update(GameAction gameAction){
        try(PreparedStatement stmt = connection.prepareStatement(UPDATE)){
            stmt.setLong(1, gameAction.getGameId());
            stmt.setLong(2, gameAction.getTurn());
            stmt.setString(3, gameAction.getActionType());
        }
    }

    public GameAction delete(GameAction gameAction){
        try(PreparedStatement stmt = connection.prepareStatement(DELETE)){
            stmt.setLong(1, gameAction.getId());
            stmt.executeUpdate();
            return gameAction;
        }
    }
}