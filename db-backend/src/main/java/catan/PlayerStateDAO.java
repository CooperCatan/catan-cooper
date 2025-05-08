package catan;

import catan.util.DataAccessObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class PlayerStateDAO extends DataAccessObject<PlayerState> {
    
    public PlayerStateDAO(Connection connection) {
        super(connection);
    }

    @Override
    public PlayerState findById(long id) {
        // unused but needs to be defined
        return null;
    }

    public PlayerState findPlayerState(long accountId, long gameId, long turnNumber) {
        String sql = "SELECT * FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.setLong(2, gameId);
            statement.setLong(3, turnNumber);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                PlayerState state = new PlayerState();
                state.setAccountId(rs.getLong("account_id"));
                state.setGameId(rs.getLong("game_id"));
                state.setTurnNumber(rs.getLong("turn_number"));
                state.setOre(rs.getLong("hand_ore"));
                state.setSheep(rs.getLong("hand_sheep"));
                state.setWheat(rs.getLong("hand_wheat"));
                state.setWood(rs.getLong("hand_wood"));
                state.setBrick(rs.getLong("hand_brick"));
                state.setVictoryPoint(rs.getLong("hand_victory_point"));
                state.setKnight(rs.getLong("hand_knight"));
                state.setMonopoly(rs.getLong("hand_monopoly"));
                state.setYearOfPlenty(rs.getLong("hand_year_of_plenty"));
                state.setRoadBuilding(rs.getLong("hand_road_building"));
                state.setNumSettlements(rs.getLong("num_settlements"));
                state.setNumRoads(rs.getLong("num_roads"));
                state.setNumCities(rs.getLong("num_cities"));
                state.setNumLongestContinuousRoad(rs.getLong("num_longest_continuous_road"));
                state.setLargestArmy(rs.getLong("largest_army"));
                state.setLongestRoad(rs.getLong("longest_road"));
                return state;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public PlayerState createEmptyHand(long accountId, long gameId, long turnNumber) {
        String sql = "INSERT INTO player_state (account_id, game_id, turn_number, " +
                    "hand_ore, hand_sheep, hand_wheat, hand_wood, hand_brick, " +
                    "hand_victory_point, hand_knight, hand_monopoly, hand_year_of_plenty, " +
                    "hand_road_building, num_settlements, num_roads, num_cities, " +
                    "num_longest_continuous_road, largest_army, longest_road) " +
                    "VALUES (?, ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false)";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.setLong(2, gameId);
            statement.setLong(3, turnNumber);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return findPlayerState(accountId, gameId, turnNumber);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean hasEnoughResources(long accountId, long gameId, long turnNumber, String resourceType, long quantity) {
        PlayerState state = findPlayerState(accountId, gameId, turnNumber);
        if (state == null) return false;
        
        return switch (resourceType.toLowerCase()) {
            case "ore" -> state.getOre() >= quantity;
            case "sheep" -> state.getSheep() >= quantity;
            case "wheat" -> state.getWheat() >= quantity;
            case "wood" -> state.getWood() >= quantity;
            case "brick" -> state.getBrick() >= quantity;
            default -> false;
        };
    }

    public PlayerState update(PlayerState playerState) {
        String sql = "UPDATE player_state SET " +
                "hand_ore = ?, " +
                "hand_sheep = ?, " +
                "hand_wheat = ?, " +
                "hand_wood = ?, " +
                "hand_brick = ?, " +
                "hand_victory_point = ?, " +
                "hand_knight = ?, " +
                "hand_monopoly = ?, " +
                "hand_year_of_plenty = ?, " +
                "hand_road_building = ?, " +
                "num_settlements = ?, " +
                "num_roads = ?, " +
                "num_cities = ?, " +
                "num_longest_continuous_road = ?, " +
                "largest_army = ?, " +
                "longest_road = ? " +
                "WHERE account_id = ? AND game_id = ? AND turn_number = ?";

        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setLong(1, playerState.getOre());
            statement.setLong(2, playerState.getSheep());
            statement.setLong(3, playerState.getWheat());
            statement.setLong(4, playerState.getWood());
            statement.setLong(5, playerState.getBrick());
            statement.setLong(6, playerState.getVictoryPoint());
            statement.setLong(7, playerState.getKnight());
            statement.setLong(8, playerState.getMonopoly());
            statement.setLong(9, playerState.getYearOfPlenty());
            statement.setLong(10, playerState.getRoadBuilding());
            statement.setLong(11, playerState.getNumSettlements());
            statement.setLong(12, playerState.getNumRoads());
            statement.setLong(13, playerState.getNumCities());
            statement.setLong(14, playerState.getNumLongestContinuousRoad());
            statement.setLong(15, playerState.isLargestArmy());
            statement.setLong(16, playerState.isLongestRoad());
            statement.setLong(17, playerState.getAccountId());
            statement.setLong(18, playerState.getGameId());
            statement.setLong(19, playerState.getTurnNumber());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                return findPlayerState(playerState.getAccountId(),
                        playerState.getGameId(),
                        playerState.getTurnNumber());
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public PlayerState updateOnBoardGain(long accountId, long gameId, long turnNumber, 
                                       String resourceType, int numSettlements, int numCities) {

        PlayerState currentState = findPlayerState(accountId, gameId, turnNumber);
        if (currentState == null) {
            return null;
        }
        
        // settlements get 1 resource and cities get 2 resources
        int totalResources = numSettlements + (numCities * 2);
        
        String sql = "UPDATE player_state SET hand_" + resourceType.toLowerCase() + 
                    " = hand_" + resourceType.toLowerCase() + " + ? " +
                    "WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setInt(1, totalResources);
            statement.setLong(2, accountId);
            statement.setLong(3, gameId);
            statement.setLong(4, turnNumber);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                return findPlayerState(accountId, gameId, turnNumber);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public PlayerState updateOnRobberLoss(long accountId, long gameId, long turnNumber) {
        PlayerState state = findPlayerState(accountId, gameId, turnNumber);
        if (state == null) return null;

        // get total number of resource cards
        long totalCards = state.getOre() + state.getSheep() +
                         state.getWheat() + state.getWood() + state.getBrick();
        if (totalCards == 0) return state;

        // randomly select which resource to take
        Random random = new Random();
        long randomNum = random.nextLong(totalCards) + 1;
        String resourceToTake;
        
        if (randomNum <= state.getOre()) resourceToTake = "ore";
        else if (randomNum <= state.getOre() + state.getSheep()) resourceToTake = "sheep";
        else if (randomNum <= state.getOre() + state.getSheep() + state.getWheat()) resourceToTake = "wheat";
        else if (randomNum <= state.getOre() + state.getSheep() + state.getWheat() + state.getWood()) resourceToTake = "wood";
        else resourceToTake = "brick";

        String sql = "UPDATE player_state SET hand_" + resourceToTake + 
                    " = hand_" + resourceToTake + " - 1 " +
                    "WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.setLong(2, gameId);
            statement.setLong(3, turnNumber);
            statement.executeUpdate();
            return findPlayerState(accountId, gameId, turnNumber);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deletePlayerHand(long accountId, long gameId, long turnNumber) {
        String sql = "DELETE FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.setLong(2, gameId);
            statement.setLong(3, turnNumber);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PlayerState create(PlayerState dto) {
        return createEmptyHand(dto.getAccountId(), dto.getGameId(), dto.getTurnNumber());
    }

    @Override
    public boolean delete(long id) {
        throw new UnsupportedOperationException("Delete by single ID not supported for PlayerState");
    }
} 