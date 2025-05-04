package catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AccountDAOTest {
    private Connection connection;
    private AccountDAO accountDAO;

    // sets up test db, executes before mock calls
    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        
        connection.createStatement().execute(
            "CREATE SEQUENCE IF NOT EXISTS account_id_seq START WITH 1 INCREMENT BY 1"
        );
        
        connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS account (" +
            "account_id BIGINT DEFAULT account_id_seq.NEXTVAL PRIMARY KEY," +
            "username VARCHAR(255) NOT NULL," +
            "email VARCHAR(255) NOT NULL," +
            "total_games BIGINT DEFAULT 0," +
            "total_wins BIGINT DEFAULT 0," +
            "total_losses BIGINT DEFAULT 0," +
            "elo BIGINT DEFAULT 1000)"
        );
        
        accountDAO = new AccountDAO(connection);
    }

    @Test
    void testCreateAndFindById() throws SQLException {
        Account newAccount = new Account();
        newAccount.setUsername("testUser");
        newAccount.setEmail("test@example.com");
        newAccount.setTotalGames(0L);
        newAccount.setTotalWins(0L);
        newAccount.setTotalLosses(0L);
        newAccount.setElo(1000L);

        Account createdAccount = accountDAO.create(newAccount);
        assertNotNull(createdAccount);
        assertTrue(createdAccount.getId() > 0);
        assertEquals("testUser", createdAccount.getUsername());
        assertEquals("test@example.com", createdAccount.getEmail());

        Account foundAccount = accountDAO.findById(createdAccount.getId());
        assertNotNull(foundAccount);
        assertEquals(createdAccount.getId(), foundAccount.getId());
        assertEquals(createdAccount.getUsername(), foundAccount.getUsername());
        assertEquals(createdAccount.getEmail(), foundAccount.getEmail());
    }

    @Test
    void testDelete() throws SQLException {
        Account newAccount = new Account();
        newAccount.setUsername("deleteTest");
        newAccount.setEmail("delete@example.com");
        Account createdAccount = accountDAO.create(newAccount);

        boolean deleted = accountDAO.delete(createdAccount.getId());
        assertTrue(deleted);

        Account foundAccount = accountDAO.findById(createdAccount.getId());
        assertEquals(0L, foundAccount.getId());
    }

    @Test
    void testFindAll() throws SQLException {
        Account account1 = new Account();
        account1.setUsername("user1");
        account1.setEmail("user1@example.com");
        accountDAO.create(account1);

        Account account2 = new Account();
        account2.setUsername("user2");
        account2.setEmail("user2@example.com");
        accountDAO.create(account2);

        List<Account> accounts = accountDAO.findAll();
        assertNotNull(accounts);
        assertEquals(2, accounts.size());
    }

    @Test
    void testUpdateUsername() throws SQLException {
        Account newAccount = new Account();
        newAccount.setUsername("oldUsername");
        newAccount.setEmail("update@example.com");
        Account createdAccount = accountDAO.create(newAccount);

        Account updatedAccount = accountDAO.updateUsername(createdAccount.getId(), "newUsername");
        assertNotNull(updatedAccount);
        assertEquals("newUsername", updatedAccount.getUsername());
    }

    @Test
    void testUpdateElo() throws SQLException {
        Account newAccount = new Account();
        newAccount.setUsername("eloTest");
        newAccount.setEmail("elo@example.com");
        newAccount.setElo(1000L);
        Account createdAccount = accountDAO.create(newAccount);

        Account updatedAccount = accountDAO.updateElo(createdAccount.getId(), 1200L);
        assertNotNull(updatedAccount);
        assertEquals(1200L, updatedAccount.getElo());
    }
} 