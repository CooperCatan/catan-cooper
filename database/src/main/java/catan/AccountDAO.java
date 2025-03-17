package catan;

import catan.util.DataAccessObject;
import catan.util.DatabaseConnectionManager;
import catan.util.JDBCExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AccountDAO implements DataAccessObject<Account> {
    private final DatabaseConnectionManager connectionManager;
    private final JDBCExecutor executor;

    public AccountDAO() {
        this.connectionManager = new DatabaseConnectionManager();
        this.executor = new JDBCExecutor(connectionManager);
    }

    @Override
    public void create(Account account) {
        String sql = "INSERT INTO accounts (username, password, elo) VALUES (?, ?, ?)";
        executor.executeUpdate(sql, account.getUsername(), account.getPassword(), account.getElo());
    }

    @Override
    public Account findById(long id) {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        return executor.executeQuery(sql, this::mapResultSetToAccount, id);
    }

    @Override
    public List<Account> findAll() {
        String sql = "SELECT * FROM accounts";
        return executor.executeQueryList(sql, this::mapResultSetToAccount);
    }

    @Override
    public void update(Account account) {
        String sql = "UPDATE accounts SET username = ?, password = ?, elo = ? WHERE account_id = ?";
        executor.executeUpdate(sql, account.getUsername(), account.getPassword(), account.getElo(), account.getId());
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM accounts WHERE account_id = ?";
        executor.executeUpdate(sql, id);
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getLong("account_id"));
        account.setUsername(rs.getString("username"));
        account.setPassword(rs.getString("password"));
        account.setElo(rs.getInt("elo"));
        return account;
    }
}