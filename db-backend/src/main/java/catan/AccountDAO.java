package catan;

import catan.util.DataAccessObject;

import java.sql.*;

public class AccountDAO extends DataAccessObject<Account> {

    private static final String GET_ONE = "SELECT * FROM account WHERE account_id=?";
    private static final String INSERT = "INSERT INTO account (username, email, total_games, total_wins, total_losses, elo) " +
            "VALUES (?, ?, ?, ?, ?, ?) RETURNING account_id";
    private static final String DELETE = "DELETE FROM account WHERE account_id=?";
    private static final String CHECK_USERNAME = "SELECT COUNT(*) FROM account WHERE username = ?";
    private static final String CHECK_EMAIL = "SELECT COUNT(*) FROM account WHERE email = ?";
    private static final String FIND_BY_EMAIL = "SELECT * FROM account WHERE email = ?";
    private static final String UPDATE_USERNAME = "UPDATE account SET username = ? WHERE email = ?";
    private static final String DELETE_BY_EMAIL = "DELETE FROM account WHERE email = ?";

    public AccountDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Account findById(long id) {
        try (PreparedStatement stmt = this.connection.prepareStatement(GET_ONE)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public Account findByEmail(String email) {
        try (PreparedStatement stmt = this.connection.prepareStatement(FIND_BY_EMAIL)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean checkEmailExists(String email) {
        try (PreparedStatement stmt = this.connection.prepareStatement(CHECK_EMAIL)) {
            stmt.setString(1, email);
            System.out.println("Executing SQL: " + CHECK_EMAIL + " with email: " + email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Found count: " + count);
                return count > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(1);
                return findById(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
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

    public boolean updateUsername(String email, String newUsername) {
        try (PreparedStatement stmt = this.connection.prepareStatement(UPDATE_USERNAME)) {
            stmt.setString(1, newUsername);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean deleteByEmail(String email) {
        try (PreparedStatement stmt = this.connection.prepareStatement(DELETE_BY_EMAIL)) {
            stmt.setString(1, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Account extractFromResultSet(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getLong("account_id"));
        account.setUsername(rs.getString("username"));
        account.setEmail(rs.getString("email"));
        account.setTotalGames(rs.getLong("total_games"));
        account.setTotalWins(rs.getLong("total_wins"));
        account.setTotalLosses(rs.getLong("total_losses"));
        account.setElo(rs.getLong("elo"));
        return account;
    }
}
