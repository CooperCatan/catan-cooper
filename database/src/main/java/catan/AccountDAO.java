package catan;

import catan.util.DataAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO extends DataAccessObject<Account> {

    private static final String GET_ONE = "SELECT * FROM account WHERE account_id=?";
    private static final String INSERT = "INSERT INTO account (username, password, total_games, total_wins, total_losses, elo) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String DELETE = "DELETE FROM account WHERE account_id=?";
    private static final String UPDATE_USERNAME = "UPDATE account SET username=? WHERE account_id=?";
    private static final String UPDATE_PASSWORD = "UPDATE account SET password=? WHERE account_id=?";
    private static final String UPDATE_ELO = "UPDATE account SET elo=? WHERE account_id=?";
    private static final String INCREMENT_WINS = "UPDATE account SET total_wins = total_wins + 1, total_games = total_games + 1 WHERE account_id=?";
    private static final String INCREMENT_LOSSES = "UPDATE account SET total_losses = total_losses + 1, total_games = total_games + 1 WHERE account_id=?";

    public AccountDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Account findById(long id) {
        Account account = new Account();
        try(PreparedStatement statement = this.connection.prepareStatement(GET_ONE)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                account.setAccountId(rs.getLong("account_id"));
                account.setUsername(rs.getString("username"));
                account.setPassword(rs.getString("password"));
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

    @Override
    public Account create(Account dto) {
        try(PreparedStatement statement = this.connection.prepareStatement(INSERT)) {
            statement.setString(1, dto.getUsername());
            statement.setString(2, dto.getPassword());
            statement.setLong(3, dto.getTotalGames());
            statement.setLong(4, dto.getTotalWins());
            statement.setLong(5, dto.getTotalLosses());
            statement.setLong(6, dto.getElo());
            statement.execute();
            int id = this.getLastVal(ACCOUNT_SEQUENCE);
            return this.findById(id);
        } catch(SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(long id) {
        try(PreparedStatement statement = this.connection.prepareStatement(DELETE)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Account updateUsername(long id, String newUsername) {
        try(PreparedStatement statement = this.connection.prepareStatement(UPDATE_USERNAME)) {
            statement.setString(1, newUsername);
            statement.setLong(2, id);
            statement.execute();
            return this.findById(id);
        } catch(SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Account updatePassword(long id, String newPassword) {
        try(PreparedStatement statement = this.connection.prepareStatement(UPDATE_PASSWORD)) {
            statement.setString(1, newPassword);
            statement.setLong(2, id);
            statement.execute();
            return this.findById(id);
        } catch(SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Account updateElo(long id, long newElo) {
        try(PreparedStatement statement = this.connection.prepareStatement(UPDATE_ELO)) {
            statement.setLong(1, newElo);
            statement.setLong(2, id);
            statement.execute();
            return this.findById(id);
        } catch(SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Account incrementWins(long id) {
        try(PreparedStatement statement = this.connection.prepareStatement(INCREMENT_WINS)) {
            statement.setLong(1, id);
            statement.execute();
            return this.findById(id);
        } catch(SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Account incrementLosses(long id) {
        try(PreparedStatement statement = this.connection.prepareStatement(INCREMENT_LOSSES)) {
            statement.setLong(1, id);
            statement.execute();
            return this.findById(id);
        } catch(SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}