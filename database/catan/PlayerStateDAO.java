package catan;

import catan.util.DataAccessObject;
import java.sql.*;
import java.util.Map;

public class PlayerStateDAO extends DataAccessObject<PlayerState> {
    private static final String CREATE_EMPTY = 
        "INSERT INTO player_state (account_id, game_id, turn_number) VALUES (?, ?, ?)";
    private static final String READ = 
        "SELECT * FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";
    private static final String CHECK_RESOURCE = 
        "SELECT ? FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";
    private static final String GET_VICTORY_POINTS = 
        "SELECT (hand_victory_point + CASE WHEN largest_army THEN 2 ELSE 0 END + " +
        "CASE WHEN longest_road THEN 2 ELSE 0 END) as total_vp " +
        "FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";
    private static final String UPDATE_BOARD_GAIN = 
        "UPDATE player_state SET hand_ore = hand_ore + ?, hand_brick = hand_brick + ?, " +
        "hand_wood = hand_wood + ?, hand_sheep = hand_sheep + ?, hand_wheat = hand_wheat + ? " +
        "WHERE account_id = ? AND game_id = ? AND turn_number = ?";
    private static final String UPDATE_ROBBER_LOSS = 
        "UPDATE player_state SET hand_ore = hand_ore - ?, hand_brick = hand_brick - ?, " +
        "hand_wood = hand_wood - ?, hand_sheep = hand_sheep - ?, hand_wheat = hand_wheat - ? " +
        "WHERE account_id = ? AND game_id = ? AND turn_number = ?";
    private static final String UPDATE_YOP_GAIN = 
        "UPDATE player_state SET ? = ? + 1, ? = ? + 1 " +
        "WHERE account_id = ? AND game_id = ? AND turn_number = ?";
    private static final String UPDATE_MONO_GAIN = 
        "UPDATE player_state SET ? = ? + ? " +
        "WHERE account_id = ? AND game_id = ? AND turn_number = ?";
    private static final String UPDATE_LONGEST_ROAD = 
        "UPDATE player_state SET longest_road = ? WHERE account_id = ? AND game_id = ? AND turn_number = ?";
    private static final String UPDATE_LARGEST_ARMY = 
        "UPDATE player_state SET largest_army = ? WHERE account_id = ? AND game_id = ? AND turn_number = ?";
    private static final String DELETE = 
        "DELETE FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";

    public PlayerStateDAO(Connection connection) {
        super(connection);
    }

    @Override
    public PlayerState create(PlayerState dto) {
        try (PreparedStatement stmt = connection.prepareStatement(CREATE_EMPTY)) {
            stmt.setLong(1, dto.getAccountId());
            stmt.setLong(2, dto.getGameId());
            stmt.setLong(3, dto.getTurnNumber());
            stmt.executeUpdate();
            return read(dto.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Error creating player state", e);
        }
    }

    @Override
    public PlayerState read(long id) {
        throw new UnsupportedOperationException("Use readPlayerState instead");
    }

    public PlayerState readPlayerState(long accountId, long gameId, long turnNumber) {
        try (PreparedStatement stmt = connection.prepareStatement(READ)) {
            stmt.setLong(1, accountId);
            stmt.setLong(2, gameId);
            stmt.setLong(3, turnNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PlayerState(rs); 
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error reading player state", e);
        }
    }

    public boolean hasResource(long accountId, long gameId, long turnNumber, String resource) {
        try (PreparedStatement stmt = connection.prepareStatement(CHECK_RESOURCE)) {
            stmt.setString(1, "hand_" + resource.toLowerCase());
            stmt.setLong(2, accountId);
            stmt.setLong(3, gameId);
            stmt.setLong(4, turnNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking resource", e);
        }
    }

    public int getVictoryPoints(long accountId, long gameId, long turnNumber) {
        try (PreparedStatement stmt = connection.prepareStatement(GET_VICTORY_POINTS)) {
            stmt.setLong(1, accountId);
            stmt.setLong(2, gameId);
            stmt.setLong(3, turnNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("total_vp") : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting victory points", e);
        }
    }

    public void updateBoardGain(long accountId, long gameId, long turnNumber, 
                              Map<String, Integer> resources) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_BOARD_GAIN)) {
            stmt.setInt(1, resources.getOrDefault("ore", 0));
            stmt.setInt(2, resources.getOrDefault("brick", 0));
            stmt.setInt(3, resources.getOrDefault("wood", 0));
            stmt.setInt(4, resources.getOrDefault("sheep", 0));
            stmt.setInt(5, resources.getOrDefault("wheat", 0));
            stmt.setLong(6, accountId);
            stmt.setLong(7, gameId);
            stmt.setLong(8, turnNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating board gain", e);
        }
    }

    public void updateRobberLoss(long accountId, long gameId, long turnNumber, 
                                Map<String, Integer> resources) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_ROBBER_LOSS)) {
            stmt.setInt(1, resources.getOrDefault("ore", 0));
            stmt.setInt(2, resources.getOrDefault("brick", 0));
            stmt.setInt(3, resources.getOrDefault("wood", 0));
            stmt.setInt(4, resources.getOrDefault("sheep", 0));
            stmt.setInt(5, resources.getOrDefault("wheat", 0));
            stmt.setLong(6, accountId);
            stmt.setLong(7, gameId);
            stmt.setLong(8, turnNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating robber loss", e);
        }
    }

    public void updateYearOfPlentyGain(long accountId, long gameId, long turnNumber, 
                                     String resource1, String resource2) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_YOP_GAIN)) {
            stmt.setString(1, "hand_" + resource1.toLowerCase());
            stmt.setString(2, "hand_" + resource1.toLowerCase());
            stmt.setString(3, "hand_" + resource2.toLowerCase());
            stmt.setString(4, "hand_" + resource2.toLowerCase());
            stmt.setLong(5, accountId);
            stmt.setLong(6, gameId);
            stmt.setLong(7, turnNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating Year of Plenty gain", e);
        }
    }

    public void updateMonopolyGain(long accountId, long gameId, long turnNumber, 
                                 String resource, int amount) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_MONO_GAIN)) {
            stmt.setString(1, "hand_" + resource.toLowerCase());
            stmt.setString(2, "hand_" + resource.toLowerCase());
            stmt.setInt(3, amount);
            stmt.setLong(4, accountId);
            stmt.setLong(5, gameId);
            stmt.setLong(6, turnNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating Monopoly gain", e);
        }
    }

    public void updateLongestRoad(long accountId, long gameId, long turnNumber, boolean hasLongestRoad) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_LONGEST_ROAD)) {
            stmt.setBoolean(1, hasLongestRoad);
            stmt.setLong(2, accountId);
            stmt.setLong(3, gameId);
            stmt.setLong(4, turnNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating longest road", e);
        }
    }

    public void updateLargestArmy(long accountId, long gameId, long turnNumber, boolean hasLargestArmy) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_LARGEST_ARMY)) {
            stmt.setBoolean(1, hasLargestArmy);
            stmt.setLong(2, accountId);
            stmt.setLong(3, gameId);
            stmt.setLong(4, turnNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating largest army", e);
        }
    }

    @Override
    public PlayerState delete(long id) {
        throw new UnsupportedOperationException("Use deletePlayerState instead");
    }

    public PlayerState deletePlayerState(long accountId, long gameId, long turnNumber) {
        PlayerState state = readPlayerState(accountId, gameId, turnNumber);
        if (state != null) {
            try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
                stmt.setLong(1, accountId);
                stmt.setLong(2, gameId);
                stmt.setLong(3, turnNumber);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error deleting player state", e);
            }
        }
        return state;
    }
} 