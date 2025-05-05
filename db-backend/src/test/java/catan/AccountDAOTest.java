package catan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;
    
    @Mock
    private Statement statement;

    @InjectMocks
    private AccountDAO accountDAO;

    private Account createTestAccount(long id, String username, String email, long elo) {
        Account account = new Account();
        account.setId(id);
        account.setUsername(username);
        account.setEmail(email);
        account.setTotalGames(10L);
        account.setTotalWins(5L);
        account.setTotalLosses(3L);
        account.setElo(elo);
        return account;
    }

    @Test
    void testFindById_Success() throws SQLException {
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("account_id")).thenReturn(1L);
        when(resultSet.getString("username")).thenReturn("foundUser");
        when(resultSet.getString("email")).thenReturn("found@example.com");
        when(resultSet.getLong("total_games")).thenReturn(10L);
        when(resultSet.getLong("total_wins")).thenReturn(5L);
        when(resultSet.getLong("total_losses")).thenReturn(3L);
        when(resultSet.getLong("elo")).thenReturn(1200L);

        Account foundAccount = accountDAO.findById(1L);

        assertNotNull(foundAccount);
        assertEquals(1L, foundAccount.getId());
        assertEquals("foundUser", foundAccount.getUsername());
        assertEquals(1200L, foundAccount.getElo());
        assertEquals(10L, foundAccount.getTotalGames());
        assertEquals(5L, foundAccount.getTotalWins());
        assertEquals(3L, foundAccount.getTotalLosses());

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testFindById_NotFound() throws SQLException {
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Account foundAccount = accountDAO.findById(99L);

        assertNotNull(foundAccount);
        assertEquals(0L, foundAccount.getId(), "ID should be 0 when account not found");
        verify(preparedStatement).setLong(1, 99L);
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    void testCreate() throws SQLException {
        Account newAccountDto = new Account();
        newAccountDto.setUsername("createTest");
        newAccountDto.setEmail("create@example.com");
        newAccountDto.setElo(1000L);

        long expectedNewId = 5L;

        when(connection.prepareStatement(contains("INSERT INTO account")))
            .thenReturn(preparedStatement);

        ResultSet lastValResultSet = mock(ResultSet.class);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(contains("SELECT last_value FROM account_id_seq")))
            .thenReturn(lastValResultSet);
        when(lastValResultSet.next()).thenReturn(true);
        when(lastValResultSet.getInt(1)).thenReturn((int)expectedNewId);

        PreparedStatement findStmt = mock(PreparedStatement.class);
        ResultSet findRs = mock(ResultSet.class);
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
            .thenReturn(findStmt);
        when(findStmt.executeQuery()).thenReturn(findRs);
        when(findRs.next()).thenReturn(true);
        when(findRs.getLong("account_id")).thenReturn(expectedNewId);
        when(findRs.getString("username")).thenReturn("createTest");
        when(findRs.getString("email")).thenReturn("create@example.com");
        when(findRs.getLong("elo")).thenReturn(1000L);
        when(findRs.getLong("total_games")).thenReturn(0L);
        when(findRs.getLong("total_wins")).thenReturn(0L);
        when(findRs.getLong("total_losses")).thenReturn(0L);

        Account createdAccount = accountDAO.create(newAccountDto);

        assertNotNull(createdAccount);
        assertEquals(expectedNewId, createdAccount.getId());
        assertEquals("createTest", createdAccount.getUsername());
        assertEquals(1000L, createdAccount.getElo());

        verify(preparedStatement).setString(1, "createTest");
        verify(preparedStatement).setString(2, "create@example.com");
        verify(preparedStatement).setLong(3, 0L);
        verify(preparedStatement).setLong(4, 0L);
        verify(preparedStatement).setLong(5, 0L);
        verify(preparedStatement).setLong(6, 1000L);
        verify(preparedStatement).execute();

        verify(statement).executeQuery(anyString());
        verify(findStmt).setLong(1, expectedNewId);
        verify(findStmt).executeQuery();
    }

    @Test
    void testDelete_Success() throws SQLException {
        when(connection.prepareStatement(contains("DELETE FROM account WHERE account_id=?")))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean deleted = accountDAO.delete(1L);

        assertTrue(deleted);
        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete_NotFound() throws SQLException {
        when(connection.prepareStatement(contains("DELETE FROM account WHERE account_id=?")))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean deleted = accountDAO.delete(99L);

        assertFalse(deleted);
        verify(preparedStatement).setLong(1, 99L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testFindAll() throws SQLException {
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(contains("SELECT * FROM account"))).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("account_id")).thenReturn(1L, 2L);
        when(resultSet.getString("username")).thenReturn("user1", "user2");
        when(resultSet.getString("email")).thenReturn("user1@example.com", "user2@example.com");
        when(resultSet.getLong("elo")).thenReturn(1000L, 1100L);

        List<Account> accounts = accountDAO.findAll();

        assertNotNull(accounts);
        assertEquals(2, accounts.size());
        assertEquals(1L, accounts.get(0).getId());
        assertEquals("user1", accounts.get(0).getUsername());
        assertEquals(2L, accounts.get(1).getId());
        assertEquals("user2", accounts.get(1).getUsername());

        verify(statement).executeQuery(anyString());
        verify(resultSet, times(3)).next();
    }

    @Test
    void testUpdateUsername() throws SQLException {
        long accountId = 1L;
        String newUsername = "newUsername";

        when(connection.prepareStatement(contains("UPDATE account SET username=?")))
            .thenReturn(preparedStatement);

        ResultSet findRs = mock(ResultSet.class);
        PreparedStatement findStmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
            .thenReturn(findStmt);
        when(findStmt.executeQuery()).thenReturn(findRs);
        when(findRs.next()).thenReturn(true);
        when(findRs.getLong("account_id")).thenReturn(accountId);
        when(findRs.getString("username")).thenReturn(newUsername);
        when(findRs.getString("email")).thenReturn("update@example.com");
        when(findRs.getLong("elo")).thenReturn(1200L);

        Account updatedAccount = accountDAO.updateUsername(accountId, newUsername);

        assertNotNull(updatedAccount);
        assertEquals(newUsername, updatedAccount.getUsername());
        assertEquals(accountId, updatedAccount.getId());

        verify(preparedStatement).setString(1, newUsername);
        verify(preparedStatement).setLong(2, accountId);
        verify(preparedStatement).execute();

        verify(findStmt).setLong(1, accountId);
        verify(findStmt).executeQuery();
    }

    @Test
    void testUpdateElo() throws SQLException {
        long accountId = 1L;
        long newElo = 1250L;

        when(connection.prepareStatement(contains("UPDATE account SET elo=?")))
            .thenReturn(preparedStatement);

        ResultSet findRs = mock(ResultSet.class);
        PreparedStatement findStmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
            .thenReturn(findStmt);
        when(findStmt.executeQuery()).thenReturn(findRs);
        when(findRs.next()).thenReturn(true);
        when(findRs.getLong("account_id")).thenReturn(accountId);
        when(findRs.getLong("elo")).thenReturn(newElo);
        when(findRs.getString("username")).thenReturn("eloUser");
        when(findRs.getString("email")).thenReturn("elo@example.com");

        Account updatedAccount = accountDAO.updateElo(accountId, newElo);

        assertNotNull(updatedAccount);
        assertEquals(newElo, updatedAccount.getElo());
        assertEquals(accountId, updatedAccount.getId());

        verify(preparedStatement).setLong(1, newElo);
        verify(preparedStatement).setLong(2, accountId);
        verify(preparedStatement).execute();

        verify(findStmt).setLong(1, accountId);
        verify(findStmt).executeQuery();
    }
    
    @Test
    void testIncrementWins() throws SQLException {
        long accountId = 1L;
        long initialWins = 5L;
        long initialGames = 10L;

        when(connection.prepareStatement(contains("UPDATE account SET total_wins = total_wins + 1")))
                .thenReturn(preparedStatement);

        ResultSet findRs = mock(ResultSet.class);
        PreparedStatement findStmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
                .thenReturn(findStmt);
        when(findStmt.executeQuery()).thenReturn(findRs);
        when(findRs.next()).thenReturn(true);
        when(findRs.getLong("account_id")).thenReturn(accountId);
        when(findRs.getLong("total_wins")).thenReturn(initialWins + 1);
        when(findRs.getLong("total_games")).thenReturn(initialGames + 1);
        when(findRs.getString("username")).thenReturn("winner");
        when(findRs.getString("email")).thenReturn("win@example.com");
        when(findRs.getLong("total_losses")).thenReturn(3L); 
        when(findRs.getLong("elo")).thenReturn(1200L); 

        Account updatedAccount = accountDAO.incrementWins(accountId);

        assertNotNull(updatedAccount);
        assertEquals(initialWins + 1, updatedAccount.getTotalWins());
        assertEquals(initialGames + 1, updatedAccount.getTotalGames());

        verify(preparedStatement).setLong(1, accountId);
        verify(preparedStatement).execute();

        verify(findStmt).setLong(1, accountId);
        verify(findStmt).executeQuery();
    }
    
    @Test
    void testIncrementLosses() throws SQLException {
        long accountId = 2L;
        long initialLosses = 3L;
        long initialGames = 10L;

        when(connection.prepareStatement(contains("UPDATE account SET total_losses = total_losses + 1")))
                .thenReturn(preparedStatement);

        ResultSet findRs = mock(ResultSet.class);
        PreparedStatement findStmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
                .thenReturn(findStmt);
        when(findStmt.executeQuery()).thenReturn(findRs);
        when(findRs.next()).thenReturn(true);
        when(findRs.getLong("account_id")).thenReturn(accountId);
        when(findRs.getLong("total_losses")).thenReturn(initialLosses + 1);
        when(findRs.getLong("total_games")).thenReturn(initialGames + 1);
        when(findRs.getString("username")).thenReturn("loser");
        when(findRs.getString("email")).thenReturn("loss@example.com");
        when(findRs.getLong("total_wins")).thenReturn(5L); 
        when(findRs.getLong("elo")).thenReturn(900L); 


        Account updatedAccount = accountDAO.incrementLosses(accountId);

        assertNotNull(updatedAccount);
        assertEquals(initialLosses + 1, updatedAccount.getTotalLosses());
        assertEquals(initialGames + 1, updatedAccount.getTotalGames());

        verify(preparedStatement).setLong(1, accountId);
        verify(preparedStatement).execute();

        verify(findStmt).setLong(1, accountId);
        verify(findStmt).executeQuery();
    }
}
