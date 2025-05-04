package catan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void testAccountGettersAndSetters() {
        Account account = new Account();
        
        account.setId(1L);
        account.setUsername("testUser");
        account.setEmail("test@example.com");
        account.setTotalGames(10L);
        account.setTotalWins(5L);
        account.setTotalLosses(3L);
        account.setElo(1200L);

        assertEquals(1L, account.getId());
        assertEquals("testUser", account.getUsername());
        assertEquals("test@example.com", account.getEmail());
        assertEquals(10L, account.getTotalGames());
        assertEquals(5L, account.getTotalWins());
        assertEquals(3L, account.getTotalLosses());
        assertEquals(1200L, account.getElo());
    }

    @Test
    void testToString() {
        Account account = new Account();
        account.setId(1L);
        account.setUsername("testUser");
        account.setEmail("test@example.com");
        account.setTotalGames(10L);
        account.setTotalWins(5L);
        account.setTotalLosses(3L);
        account.setElo(1200L);

        String expectedString = "Account{" +
                "accountId=1" +
                ", username='testUser'" +
                ", email='test@example.com'" +
                ", totalGames=10" +
                ", totalWins=5" + 
                ", totalLosses=3" +
                ", elo=1200" +
                '}';

        assertEquals(expectedString, account.toString());
    }
} 