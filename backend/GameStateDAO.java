package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class AccountDAO {
    @Autowired
    private DataSource dataSource;

    // C - Create Acct.
    public void createAccount(String username, String password) throws SQLException {
        String sql = "INSERT INTO account (username, password) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
        }
    }

    // R - getID
    public Long getAccountId(String username) throws SQLException {
        String sql = "SELECT account_id FROM account WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("account_id");
                }
            }
        }
        return null;
    }

    // U - ChangeUser, Change PW, update ELO
    public void updateUsername(Long accountId, String newUsername) throws SQLException {
        String sql = "UPDATE account SET username = ? WHERE account_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newUsername);
            stmt.setLong(2, accountId);
            stmt.executeUpdate();
        }
    }

    public void updatePassword(Long accountId, String newPassword) throws SQLException {
        String sql = "UPDATE account SET password = ? WHERE account_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setLong(2, accountId);
            stmt.executeUpdate();
        }
    }

    public void updateElo(Long accountId, Long newElo) throws SQLException {
        String sql = "UPDATE account SET elo = ? WHERE account_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, newElo);
            stmt.setLong(2, accountId);
            stmt.executeUpdate();
        }
    }

    // D - Delete Acct.
    public void deleteAccount(Long accountId) throws SQLException {
        String sql = "DELETE FROM account WHERE account_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.executeUpdate();
        }
    }
}
