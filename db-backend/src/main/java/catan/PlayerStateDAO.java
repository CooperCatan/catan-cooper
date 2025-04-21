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
        // unushed but needs to be defined
        return null;
    }

    public PlayerState findPlayerState(long accountId, long gameId, long turnNumber) {
        System.out.println("DEBUG - Finding player state for accountId=" + accountId + ", gameId=" + gameId + ", turnNumber=" + turnNumber);
        String sql = "SELECT * FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.setLong(2, gameId);
            statement.setLong(3, turnNumber);
            System.out.println("DEBUG - Executing SQL: " + sql + " with params: [" + accountId + ", " + gameId + ", " + turnNumber + "]");
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                PlayerState state = new PlayerState();
                state.setAccountId(rs.getLong("account_id"));
                state.setGameId(rs.getLong("game_id"));
                state.setTurnNumber(rs.getLong("turn_number"));
                state.setHandOre(rs.getLong("hand_ore"));
                state.setHandSheep(rs.getLong("hand_sheep"));
                state.setHandWheat(rs.getLong("hand_wheat"));
                state.setHandWood(rs.getLong("hand_wood"));
                state.setHandBrick(rs.getLong("hand_brick"));
                state.setHandVictoryPoint(rs.getLong("hand_victory_point"));
                state.setHandKnight(rs.getLong("hand_knight"));
                state.setHandMonopoly(rs.getLong("hand_monopoly"));
                state.setHandYearOfPlenty(rs.getLong("hand_year_of_plenty"));
                state.setHandRoadBuilding(rs.getLong("hand_road_building"));
                state.setNumSettlements(rs.getLong("num_settlements"));
                state.setNumRoads(rs.getLong("num_roads"));
                state.setNumCities(rs.getLong("num_cities"));
                state.setNumLongestContinuousRoad(rs.getLong("num_longest_continuous_road"));
                state.setLargestArmy(rs.getBoolean("largest_army"));
                state.setLongestRoad(rs.getBoolean("longest_road"));
                System.out.println("DEBUG - Found existing player state");
                return state;
            }
            System.out.println("DEBUG - No player state found");
            return null;
        } catch (SQLException e) {
            System.err.println("ERROR - Failed to find player state: " + e.getMessage());
            System.err.println("ERROR - SQL State: " + e.getSQLState());
            System.err.println("ERROR - Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public PlayerState createEmptyHand(long accountId, long gameId, long turnNumber) {
        System.out.println("DEBUG - Creating empty hand for accountId=" + accountId + ", gameId=" + gameId + ", turnNumber=" + turnNumber);
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
            System.out.println("DEBUG - Executing SQL: " + sql + " with params: [" + accountId + ", " + gameId + ", " + turnNumber + "]");
            int rowsAffected = statement.executeUpdate();
            System.out.println("DEBUG - Insert affected " + rowsAffected + " rows");
            if (rowsAffected > 0) {
                return findPlayerState(accountId, gameId, turnNumber);
            } else {
                System.err.println("ERROR - Insert statement did not affect any rows");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("ERROR - Failed to create empty hand: " + e.getMessage());
            System.err.println("ERROR - SQL State: " + e.getSQLState());
            System.err.println("ERROR - Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean hasEnoughResources(long accountId, long gameId, long turnNumber, String resourceType, long quantity) {
        PlayerState state = findPlayerState(accountId, gameId, turnNumber);
        if (state == null) return false;
        
        return switch (resourceType.toLowerCase()) {
            case "ore" -> state.getHandOre() >= quantity;
            case "sheep" -> state.getHandSheep() >= quantity;
            case "wheat" -> state.getHandWheat() >= quantity;
            case "wood" -> state.getHandWood() >= quantity;
            case "brick" -> state.getHandBrick() >= quantity;
            default -> false;
        };
    }

    public PlayerState updateOnBoardGain(long accountId, long gameId, long turnNumber, 
                                       String resourceType, int numSettlements, int numCities) {
        System.out.println("DEBUG - Updating board gain for accountId=" + accountId + 
                         ", gameId=" + gameId + ", turnNumber=" + turnNumber);
        System.out.println("DEBUG - Resource: " + resourceType + 
                         ", Settlements: " + numSettlements + ", Cities: " + numCities);

        // First check if the player state exists
        PlayerState currentState = findPlayerState(accountId, gameId, turnNumber);
        if (currentState == null) {
            System.err.println("ERROR - No player state found for accountId=" + accountId + 
                             ", gameId=" + gameId + ", turnNumber=" + turnNumber);
            return null;
        }
        System.out.println("DEBUG - Found existing player state");
        
        // In Catan, settlements get 1 resource and cities get 2 resources
        int totalResources = numSettlements + (numCities * 2);
        System.out.println("DEBUG - Calculating total resources: " + totalResources);
        
        String sql = "UPDATE player_state SET hand_" + resourceType.toLowerCase() + 
                    " = hand_" + resourceType.toLowerCase() + " + ? " +
                    "WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setInt(1, totalResources);
            statement.setLong(2, accountId);
            statement.setLong(3, gameId);
            statement.setLong(4, turnNumber);
            
            System.out.println("DEBUG - Executing SQL: " + sql);
            System.out.println("DEBUG - Parameters: [" + totalResources + ", " + 
                             accountId + ", " + gameId + ", " + turnNumber + "]");
            
            int rowsAffected = statement.executeUpdate();
            System.out.println("DEBUG - Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                return findPlayerState(accountId, gameId, turnNumber);
            } else {
                System.err.println("ERROR - Update did not affect any rows");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("ERROR - SQL error while updating resources: " + e.getMessage());
            System.err.println("ERROR - SQL State: " + e.getSQLState());
            System.err.println("ERROR - Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public PlayerState updateOnRobberLoss(long accountId, long gameId, long turnNumber) {
        PlayerState state = findPlayerState(accountId, gameId, turnNumber);
        if (state == null) return null;

        // get total number of resource cards
        long totalCards = state.getHandOre() + state.getHandSheep() + 
                         state.getHandWheat() + state.getHandWood() + state.getHandBrick();
        if (totalCards == 0) return state;

        // randomly select which resource to take
        Random random = new Random();
        long randomNum = random.nextLong(totalCards) + 1;
        String resourceToTake;
        
        if (randomNum <= state.getHandOre()) resourceToTake = "ore";
        else if (randomNum <= state.getHandOre() + state.getHandSheep()) resourceToTake = "sheep";
        else if (randomNum <= state.getHandOre() + state.getHandSheep() + state.getHandWheat()) resourceToTake = "wheat";
        else if (randomNum <= state.getHandOre() + state.getHandSheep() + state.getHandWheat() + state.getHandWood()) resourceToTake = "wood";
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