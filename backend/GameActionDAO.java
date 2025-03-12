package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class GameActionDAO {
    @Autowired
    private DataSource dataSource;

    // C - Create action
    public void createAction(Long gameId, Long turnNumber, String actionType) throws SQLException {
        String sql = "INSERT INTO game_action (game_id, turn_number, action_type) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, gameId);
            stmt.setLong(2, turnNumber);
            stmt.setString(3, actionType);
            stmt.executeUpdate();
        }
    }
}
