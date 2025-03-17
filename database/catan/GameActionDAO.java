package catan;

import catan.util.DataAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameActionDAO extends DataAccessObject<GameAction> {

    private static final String CREATE = "INSERT INTO game_actions (action_id, game_id, account_id, turn, action_type) VALUES (?, ?, ?, ?, ?)";
    private static final String READ = "SELECT * FROM game_actions WHERE id = ?";
    private static final String UPDATE = "UPDATE game_actions SET game_id = ?, turn = ?, action_type = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM game_actions WHERE action_id = ?";

    public GameActionDAO(Connection connection){
        super(connection);
    }

    @Override
    public GameAction findById(long id) {
        return null;
    }

    @Override
    public GameAction create(GameAction gameAction){
        try(PreparedStatement stmt = connection.prepareStatement(CREATE)){
            stmt.setLong(1, gameAction.getGameId());
            stmt.setLong(2, gameAction.getAccountId());
            stmt.setLong(3, gameAction.getTurn());
            stmt.setString(4, gameAction.getActionType());
            stmt.executeUpdate();
            //Remove the game action object
            delete(gameAction);
            //return a copy if it needs to be used
            return gameAction;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameAction update(GameAction dto) {
        return null;
    }

    public GameAction delete(GameAction gameAction){
        try(PreparedStatement stmt = connection.prepareStatement(DELETE)){
            stmt.setLong(1, gameAction.getId());
            stmt.executeUpdate();
            return gameAction;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}