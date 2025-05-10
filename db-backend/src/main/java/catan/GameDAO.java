package catan;

import catan.util.DataAccessObject;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameDAO extends DataAccessObject<Game> {

    private static final String GET_ONE = "SELECT * FROM game WHERE game_id=?";
    private static final String GET_ALL = "SELECT * FROM game";
    private static final String GET_ACTIVE_GAMES = "SELECT * FROM game WHERE is_game_over=false ORDER BY created_at DESC";
    private static final String GET_COMPLETED_GAMES = "SELECT * FROM game WHERE is_game_over=true ORDER BY created_at DESC";
    private static final String DELETE_EMPTY_GAMES = "DELETE FROM game WHERE array_length(player_list, 1) = 0 AND in_progress = true";
    private static final String INSERT = "INSERT INTO game (player_list, winner_id, is_game_over, in_progress, game_name, " +
        "json_hexes, json_vertices, json_edges, json_players, current_dice_roll, robber_location, " +
        "bank_brick, bank_ore, bank_sheep, bank_wheat, bank_wood, " +
        "bank_year_of_plenty, bank_monopoly, bank_road_building, bank_victory_point, bank_knight) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING game_id";
    private static final String DELETE = "DELETE FROM game WHERE game_id=?";
    private static final String ADD_PLAYER = "UPDATE game SET player_list = array_append(player_list, ?) WHERE game_id=?";
    private static final String REMOVE_PLAYER = "UPDATE game SET player_list = array_remove(player_list, ?) WHERE game_id=?";
    private static final String UPDATE_GAME_STATE = "UPDATE game SET " +
        "json_hexes=?, json_vertices=?, json_edges=?, json_players=?, " +
        "current_dice_roll=?, robber_location=?, " +
        "bank_brick=?, bank_ore=?, bank_sheep=?, bank_wheat=?, bank_wood=?, " +
        "bank_year_of_plenty=?, bank_monopoly=?, bank_road_building=?, bank_victory_point=?, bank_knight=? " +
        "WHERE game_id=?";

    public GameDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Game findById(long id) {
        try (PreparedStatement statement = this.connection.prepareStatement(GET_ONE)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return extractFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Game> findAll() {
        List<Game> games = new ArrayList<>();
        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery(GET_ALL);
            while (rs.next()) {
                games.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return games;
    }

    public List<Game> findActiveGames() {
        List<Game> games = new ArrayList<>();
        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery(GET_ACTIVE_GAMES);
            while (rs.next()) {
                games.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return games;
    }

    public List<Game> findCompletedGames() {
        List<Game> games = new ArrayList<>();
        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery(GET_COMPLETED_GAMES);
            while (rs.next()) {
                games.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return games;
    }

    @Override
    public Game create(Game game) {
        try (PreparedStatement statement = this.connection.prepareStatement(INSERT)) {
            // Initialize an empty array for player_list
            Array playerArray = connection.createArrayOf("bigint", new Long[0]);
            statement.setArray(1, playerArray);
            statement.setNull(2, java.sql.Types.BIGINT); // winner_id
            statement.setBoolean(3, game.isGameOver());
            statement.setBoolean(4, game.isInProgress());
            statement.setString(5, game.getGameName());
            
            // Set game state with null checks
            statement.setString(6, game.getJsonHexes() != null ? game.getJsonHexes() : "[]");
            statement.setString(7, game.getJsonVertices() != null ? game.getJsonVertices() : "[]");
            statement.setString(8, game.getJsonEdges() != null ? game.getJsonEdges() : "[]");
            statement.setString(9, game.getJsonPlayers() != null ? game.getJsonPlayers() : "[]");
            setNullableInt(statement, 10, game.getCurrentDiceRoll());
            setNullableInt(statement, 11, game.getRobberLocation());
            setNullableInt(statement, 12, game.getBankBrick());
            setNullableInt(statement, 13, game.getBankOre());
            setNullableInt(statement, 14, game.getBankSheep());
            setNullableInt(statement, 15, game.getBankWheat());
            setNullableInt(statement, 16, game.getBankWood());
            setNullableInt(statement, 17, game.getBankYearOfPlenty());
            setNullableInt(statement, 18, game.getBankMonopoly());
            setNullableInt(statement, 19, game.getBankRoadBuilding());
            setNullableInt(statement, 20, game.getBankVictoryPoint());
            setNullableInt(statement, 21, game.getBankKnight());
            
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                long gameId = rs.getLong("game_id");
                game.setGameId(gameId); // Set the ID on the game object
                return findById(gameId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public boolean delete(long id) {
        try (PreparedStatement statement = this.connection.prepareStatement(DELETE)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Game addPlayer(long gameId, long playerId) {
        try (PreparedStatement statement = this.connection.prepareStatement(ADD_PLAYER)) {
            statement.setLong(1, playerId);
            statement.setLong(2, gameId);
            if (statement.executeUpdate() > 0) {
                return findById(gameId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public Game removePlayer(long gameId, long playerId) {
        try (PreparedStatement statement = this.connection.prepareStatement(REMOVE_PLAYER)) {
            statement.setLong(1, playerId);
            statement.setLong(2, gameId);
            if (statement.executeUpdate() > 0) {
                return findById(gameId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public Game updateGameState(Game game) {
        try (PreparedStatement statement = this.connection.prepareStatement(UPDATE_GAME_STATE)) {
            statement.setString(1, game.getJsonHexes());
            statement.setString(2, game.getJsonVertices());
            statement.setString(3, game.getJsonEdges());
            statement.setString(4, game.getJsonPlayers());
            setNullableInt(statement, 5, game.getCurrentDiceRoll());
            setNullableInt(statement, 6, game.getRobberLocation());
            setNullableInt(statement, 7, game.getBankBrick());
            setNullableInt(statement, 8, game.getBankOre());
            setNullableInt(statement, 9, game.getBankSheep());
            setNullableInt(statement, 10, game.getBankWheat());
            setNullableInt(statement, 11, game.getBankWood());
            setNullableInt(statement, 12, game.getBankYearOfPlenty());
            setNullableInt(statement, 13, game.getBankMonopoly());
            setNullableInt(statement, 14, game.getBankRoadBuilding());
            setNullableInt(statement, 15, game.getBankVictoryPoint());
            setNullableInt(statement, 16, game.getBankKnight());
            statement.setLong(17, game.getId());
            
            if (statement.executeUpdate() > 0) {
                return findById(game.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public void deleteEmptyGames() {
        try (PreparedStatement statement = this.connection.prepareStatement(DELETE_EMPTY_GAMES)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Game extractFromResultSet(ResultSet rs) throws SQLException {
        Game game = new Game();
        game.setGameId(rs.getLong("game_id"));
        Array playerArray = rs.getArray("player_list");
        if (playerArray != null) {
            Long[] playerIds = (Long[]) playerArray.getArray();
            game.setPlayerList(new ArrayList<>(Arrays.asList(playerIds)));
        }
        game.setWinnerId(rs.getLong("winner_id"));
        if (rs.wasNull()) {
            game.setWinnerId(null);
        }
        game.setIsGameOver(rs.getBoolean("is_game_over"));
        game.setInProgress(rs.getBoolean("in_progress"));
        game.setCreatedAt(rs.getTimestamp("created_at"));
        game.setGameName(rs.getString("game_name"));
        
        // Extract game state
        game.setJsonHexes(rs.getString("json_hexes"));
        game.setJsonVertices(rs.getString("json_vertices"));
        game.setJsonEdges(rs.getString("json_edges"));
        game.setJsonPlayers(rs.getString("json_players"));
        game.setCurrentDiceRoll(rs.getInt("current_dice_roll"));
        if (rs.wasNull()) game.setCurrentDiceRoll(null);
        game.setRobberLocation(rs.getInt("robber_location"));
        if (rs.wasNull()) game.setRobberLocation(null);
        game.setBankBrick(rs.getInt("bank_brick"));
        if (rs.wasNull()) game.setBankBrick(null);
        game.setBankOre(rs.getInt("bank_ore"));
        if (rs.wasNull()) game.setBankOre(null);
        game.setBankSheep(rs.getInt("bank_sheep"));
        if (rs.wasNull()) game.setBankSheep(null);
        game.setBankWheat(rs.getInt("bank_wheat"));
        if (rs.wasNull()) game.setBankWheat(null);
        game.setBankWood(rs.getInt("bank_wood"));
        if (rs.wasNull()) game.setBankWood(null);
        game.setBankYearOfPlenty(rs.getInt("bank_year_of_plenty"));
        if (rs.wasNull()) game.setBankYearOfPlenty(null);
        game.setBankMonopoly(rs.getInt("bank_monopoly"));
        if (rs.wasNull()) game.setBankMonopoly(null);
        game.setBankRoadBuilding(rs.getInt("bank_road_building"));
        if (rs.wasNull()) game.setBankRoadBuilding(null);
        game.setBankVictoryPoint(rs.getInt("bank_victory_point"));
        if (rs.wasNull()) game.setBankVictoryPoint(null);
        game.setBankKnight(rs.getInt("bank_knight"));
        if (rs.wasNull()) game.setBankKnight(null);
        
        return game;
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value != null) {
            statement.setInt(index, value);
        } else {
            statement.setNull(index, java.sql.Types.INTEGER);
        }
    }
}
