package catan;

import catan.util.DataAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO extends DataAccessObject<Account> {

    private static final String CREATE = "INSERT INTO accounts (username, password) VALUES (?, ?)"; // create acct w user and pass
    private static final String READ = "SELECT * FROM accounts WHERE id = ?"; // read acct by id 
    private static final String UPDATE = "UPDATE accounts SET username = ?, password = ? WHERE id = ?"; // update acct by id
    private static final String DELETE = "DELETE FROM accounts WHERE id = ?"; // delete acct by id
    
    public AccountDAO(Connection connection){
        super(connection);
    }

    @Override
    public Account create(Account account){
        try(PreparedStatement stmt = connection.prepareStatement(CREATE)){
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            stmt.executeUpdate();
            return account;
        }
    }



    public Account read(long id){
        try(PreparedStatement stmt = connection.prepareStatement(READ)){
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? new Account(rs) : null;
        }
    }

    public Account update(Account account){
        try(PreparedStatement stmt = connection.prepareStatement(UPDATE)){
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            stmt.setLong(3, account.getId());
        }
    }

    public Account delete(Account account){
        try(PreparedStatement stmt = connection.prepareStatement(DELETE)){
            stmt.setLong(1, account.getId());
            stmt.executeUpdate();
            return account;
        }
    }
}