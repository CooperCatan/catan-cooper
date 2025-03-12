package catan;

import catan.util.DataAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AccountDAO extends DataAccessObject<Account> {

    private static final String CREATE = "INSERT INTO account (username, password, total_wins, total_losses, total_games, elo) VALUES (?, ?, 0, 0, 0, 1000)";
    private static final String FIND_BY_ID = "SELECT * FROM account WHERE account_id = ?";
    private static final String UPDATE_USERNAME = "UPDATE account SET username = ? WHERE account_id = ?";
    private static final String UPDATE_PASSWORD = "UPDATE account SET password = ? WHERE account_id = ?";
    private static final String UPDATE_ELO = "UPDATE account SET elo = ? WHERE account_id = ?";
    private static final String DELETE = "DELETE FROM account WHERE account_id = ?";

    public AccountDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Account create(Account account) {
        try (PreparedStatement stmt = connection.prepareStatement(CREATE, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setAccountId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating account failed, no ID obtained.");
                }
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating account: " + e.getMessage(), e);
        }
    }

    @Override
    public Account findById(long id) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? new Account(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding account by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Account update(Account account) {
        // Generic update method that can call specific update methods
        updateUsername(account);
        updatePassword(account);
        updateElo(account);
        return account;
    }

    public Account updateUsername(Account account) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_USERNAME)) {
            stmt.setString(1, account.getUsername());
            stmt.setLong(2, account.getAccountId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating username failed, no rows affected.");
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating username: " + e.getMessage(), e);
        }
    }

    public Account updatePassword(Account account) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_PASSWORD)) {
            stmt.setString(1, account.getPassword());
            stmt.setLong(2, account.getAccountId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating password failed, no rows affected.");
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating password: " + e.getMessage(), e);
        }
    }

    public Account updateElo(Account account) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_ELO)) {
            stmt.setLong(1, account.getElo());
            stmt.setLong(2, account.getAccountId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating ELO failed, no rows affected.");
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating ELO: " + e.getMessage(), e);
        }
    }

    @Override
    public Account delete(Account account) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setLong(1, account.getAccountId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting account failed, no rows affected.");
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting account: " + e.getMessage(), e);
        }
    }
}