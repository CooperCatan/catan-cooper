package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlayerStateDAO {
    @Autowired
    private DataSource dataSource;

    // C - CreateEmptyPlayer
    public void createEmptyPlayer(Long accountId, Long gameId, Long turnNumber) throws SQLException {
        String sql = "INSERT INTO player_state (account_id, game_id, turn_number) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.setLong(2, gameId);
            stmt.setLong(3, turnNumber);
            stmt.executeUpdate();
        }
    }

    // R - userHasResource
    public boolean userHasResource(Long accountId, Long gameId, Long turnNumber, String resource, Long amount) throws SQLException {
        String sql = "SELECT " + resource + " FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.setLong(2, gameId);
            stmt.setLong(3, turnNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(resource) >= amount;
                }
            }
        }
        return false;
    }

    // R - numVP
    public Long getVictoryPoints(Long accountId, Long gameId, Long turnNumber) throws SQLException {
        String sql = "SELECT num_settlements, num_cities, victory_point, largest_army, longest_road FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.setLong(2, gameId);
            stmt.setLong(3, turnNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("num_settlements") + (rs.getLong("num_cities") * 2) + rs.getLong("victory_point")
                        + (rs.getBoolean("largest_army") ? 2 : 0) + (rs.getBoolean("longest_road") ? 2 : 0);
                }
            }
        }
        return 0L;
    }

    // R - longestRoad, largestArmy
    public boolean hasLongestRoad(Long accountId, Long gameId, Long turnNumber) throws SQLException {
        String sql = "SELECT longest_road FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.setLong(2, gameId);
            stmt.setLong(3, turnNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("longest_road");
                }
            }
        }
        return false;
    }

    public boolean hasLargestArmy(Long accountId, Long gameId, Long turnNumber) throws SQLException {
        String sql = "SELECT largest_army FROM player_state WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.setLong(2, gameId);
            stmt.setLong(3, turnNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("largest_army");
                }
            }
        }
        return false;
    }

    // U - VPCounter
    public void updateVictoryPoints(Long accountId, Long gameId, Long turnNumber, Long vpCards) throws SQLException {
        String sql = "UPDATE player_state SET victory_point = ? WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vpCards);
            stmt.setLong(2, accountId);
            stmt.setLong(3, gameId);
            stmt.setLong(4, turnNumber);
            stmt.executeUpdate();
        }
    }

    // D - deleteInHand
    public void deleteInHand(Long accountId, Long gameId, Long turnNumber, String resource, Long amount) throws SQLException {
        String sql = "UPDATE player_state SET " + resource + " = " + resource + " - ? WHERE account_id = ? AND game_id = ? AND turn_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, amount);
            stmt.setLong(2, accountId);
            stmt.setLong(3, gameId);
            stmt.setLong(4, turnNumber);
            stmt.executeUpdate();
        }
    }

    // Helper: Get all players in a game
    public List<PlayerState> getPlayers(Long gameId, Long turnNumber) throws SQLException {
        List<PlayerState> players = new ArrayList<>();
        String sql = "SELECT * FROM player_state WHERE game_id = ? AND turn_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, gameId);
            stmt.setLong(2, turnNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PlayerState player = new PlayerState();
                    player.setAccountId(rs.getLong("account_id"));
                    player.setGameId(rs.getLong("game_id"));
                    player.setTurnNumber(rs.getLong("turn_number"));
                    player.setBrick(rs.getLong("brick"));
                    player.setOre(rs.getLong("ore"));
                    player.setSheep(rs.getLong("sheep"));
                    player.setWheat(rs.getLong("wheat"));
                    player.setWood(rs.getLong("wood"));
                    player.setVictoryPoint(rs.getLong("victory_point"));
                    player.setKnight(rs.getLong("knight"));
                    player.setMonopoly(rs.getLong("monopoly"));
                    player.setYearOfPlenty(rs.getLong("year_of_plenty"));
                    player.setRoadBuilding(rs.getLong("road_building"));
                    player.setNumSettlements(rs.getLong("num_settlements"));
                    player.setNumRoads(rs.getLong("num_roads"));
                    player.setNumCities(rs.getLong("num_cities"));
                    player.setNumLongestContinuousRoad(rs.getLong("num_longest_continuous_road"));
                    player.setLargestArmy(rs.getBoolean("largest_army"));
                    player.setLongestRoad(rs.getBoolean("longest_road"));
                    players.add(player);
                }
            }
        }
        return players;
    }
}
