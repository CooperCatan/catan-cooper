package catan;

import catan.util.DataAccessObject;
<<<<<<< HEAD
import java.sql.*;
=======

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
>>>>>>> b3f1e1b1d3fd2f950e9c88745ac9dbe042cc8012

public class AccountDAO extends DataAccessObject<Account> {
    private static final String CREATE = "INSERT INTO account (username, password) VALUES (?, ?)";
    private static final String READ = "SELECT * FROM account WHERE account_id = ?";
    private static final String UPDATE_USERNAME = "UPDATE account SET username = ? WHERE account_id = ?";
    private static final String UPDATE_PASSWORD = "UPDATE account SET password = ? WHERE account_id = ?";
    private static final String UPDATE_ELO = "UPDATE account SET elo = ? WHERE account_id = ?";
    private static final String DELETE = "DELETE FROM account WHERE account_id = ?";

<<<<<<< HEAD
=======
    private static final String CREATE = "INSERT INTO account (username, password, total_wins, total_losses, total_games, elo) VALUES (?, ?, 0, 0, 0, 1000)";
    private static final String FIND_BY_ID = "SELECT * FROM account WHERE account_id = ?";
    private static final String UPDATE_USERNAME = "UPDATE account SET username = ? WHERE account_id = ?";
    private static final String UPDATE_PASSWORD = "UPDATE account SET password = ? WHERE account_id = ?";
    private static final String UPDATE_ELO = "UPDATE account SET elo = ? WHERE account_id = ?";
    private static final String DELETE = "DELETE FROM account WHERE account_id = ?";

>>>>>>> b3f1e1b1d3fd2f950e9c88745ac9dbe042cc8012
    public AccountDAO(Connection connection) {
        super(connection);
    }

    @Override
<<<<<<< HEAD
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
=======
    public Account create(Account account) {
        try (PreparedStatement stmt = connection.prepareStatement(CREATE, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setAccountId(generatedKeys.getLong(1));
>>>>>>> b3f1e1b1d3fd2f950e9c88745ac9dbe042cc8012
                } else {
                    throw new SQLException("Creating account failed, no ID obtained.");
                }
            }
<<<<<<< HEAD
            return dto;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating account", e);
=======
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating account: " + e.getMessage(), e);
>>>>>>> b3f1e1b1d3fd2f950e9c88745ac9dbe042cc8012
        }
    }

    @Override
<<<<<<< HEAD
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
=======
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
>>>>>>> b3f1e1b1d3fd2f950e9c88745ac9dbe042cc8012
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