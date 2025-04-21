package catan;

import catan.util.DataAccessObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO extends DataAccessObject<Account> {

    private static final String GET_ONE = "SELECT * FROM account WHERE account_id=?";
    private static final String GET_ALL = "SELECT * FROM account";
    private static final String INSERT = "INSERT INTO account (username, email, total_games, total_wins, total_losses, elo) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String DELETE = "DELETE FROM account WHERE account_id=?";
    private static final String UPDATE_USERNAME = "UPDATE account SET username=? WHERE account_id=?"; // this is not being used right now, firebase has UUIDs and this method will only change the display name and now how it is handled by firebase, low priority to fix
    private static final String UPDATE_ELO = "UPDATE account SET elo=? WHERE account_id=?";
    private static final String INCREMENT_WINS = "UPDATE account SET total_wins = total_wins + 1, total_games = total_games + 1 WHERE account_id=?";
    private static final String INCREMENT_LOSSES = "UPDATE account SET total_losses = total_losses + 1, total_games = total_games + 1 WHERE account_id=?";
    private static final String CHECK_USERNAME = "SELECT COUNT(*) FROM account WHERE username = ?";

    public AccountDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Account findById(long id) {
        Account account = new Account();
        try (PreparedStatement stmt = this.connection.prepareStatement(GET_ONE)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                account.setId(rs.getLong("account_id"));
                account.setUsername(rs.getString("username"));
                account.setEmail(rs.getString("email"));
                account.setTotalGames(rs.getLong("total_games"));
                account.setTotalWins(rs.getLong("total_wins"));
                account.setTotalLosses(rs.getLong("total_losses"));
                account.setElo(rs.getLong("elo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return account;
    }

    public boolean checkUsernameExists(String username) {
        try (PreparedStatement stmt = this.connection.prepareStatement(CHECK_USERNAME)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Account create(Account dto) {
        try (PreparedStatement stmt = this.connection.prepareStatement(INSERT)) {
            stmt.setString(1, dto.getUsername());
            stmt.setString(2, dto.getEmail());
            stmt.setLong(3, dto.getTotalGames());
            stmt.setLong(4, dto.getTotalWins());
            stmt.setLong(5, dto.getTotalLosses());
            stmt.setLong(6, dto.getElo());
            
            stmt.execute();
            int id = this.getLastVal(ACCOUNT_SEQUENCE);
            return this.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(long id) {
        try (PreparedStatement stmt = this.connection.prepareStatement(DELETE)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Account updateUsername(long id, String newUsername) {
        try (PreparedStatement stmt = this.connection.prepareStatement(UPDATE_USERNAME)) {
            stmt.setString(1, newUsername);
            stmt.setLong(2, id);
            stmt.execute();
            return this.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Account updateElo(long id, long newElo) {
        try (PreparedStatement stmt = this.connection.prepareStatement(UPDATE_ELO)) {
            stmt.setLong(1, newElo);
            stmt.setLong(2, id);
            stmt.execute();
            return this.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Account incrementWins(long id) {
        try (PreparedStatement stmt = this.connection.prepareStatement(INCREMENT_WINS)) {
            stmt.setLong(1, id);
            stmt.execute();
            return this.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Account incrementLosses(long id) {
        try (PreparedStatement stmt = this.connection.prepareStatement(INCREMENT_LOSSES)) {
            stmt.setLong(1, id);
            stmt.execute();
            return this.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        try (Statement stmt = this.connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(GET_ALL);
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getLong("account_id"));
                account.setUsername(rs.getString("username"));
                account.setEmail(rs.getString("email"));
                account.setTotalGames(rs.getLong("total_games"));
                account.setTotalWins(rs.getLong("total_wins"));
                account.setTotalLosses(rs.getLong("total_losses"));
                account.setElo(rs.getLong("elo"));
                accounts.add(account);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return accounts;
    }
}
