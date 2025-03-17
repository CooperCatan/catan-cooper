package catan;

import catan.util.DataAccessObject;
import java.sql.*;

public class AccountDAO extends DataAccessObject<Account> {
    private static final String CREATE = "INSERT INTO account (username, password) VALUES (?, ?)";
    private static final String READ = "SELECT * FROM account WHERE account_id = ?";
    private static final String UPDATE_USERNAME = "UPDATE account SET username = ? WHERE account_id = ?";
    private static final String UPDATE_PASSWORD = "UPDATE account SET password = ? WHERE account_id = ?";
    private static final String UPDATE_ELO = "UPDATE account SET elo = ? WHERE account_id = ?";
    private static final String DELETE = "DELETE FROM account WHERE account_id = ?";

    public AccountDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Account create(Account dto) {
        try (PreparedStatement stmt = connection.prepareStatement(CREATE, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dto.getUsername());
            stmt.setString(2, dto.getPassword());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating account failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    dto.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating account failed, no ID obtained.");
                }
            }
            return dto;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating account", e);
        }
    }

    @Override
    public Account read(long id) {
        try (PreparedStatement stmt = connection.prepareStatement(READ)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Account account = new Account();
                account.setId(rs.getLong("account_id"));
                account.setUsername(rs.getString("username"));
                account.setPassword(rs.getString("password"));
                account.setElo(rs.getLong("elo"));
                return account;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error reading account", e);
        }
    }

    public void changeUsername(long id, String newUsername) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_USERNAME)) {
            stmt.setString(1, newUsername);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating username", e);
        }
    }

    public void changePassword(long id, String newPassword) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_PASSWORD)) {
            stmt.setString(1, newPassword);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating password", e);
        }
    }

    public void updateElo(long id, int newElo) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_ELO)) {
            stmt.setInt(1, newElo);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating ELO", e);
        }
    }

    @Override
    public Account delete(long id) {
        Account account = read(id);
        if (account != null) {
            try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error deleting account", e);
            }
        }
        return account;
    }
}