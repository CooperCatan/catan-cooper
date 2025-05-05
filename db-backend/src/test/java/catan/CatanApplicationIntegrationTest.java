package catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") 
class CatanApplicationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;


    @MockBean
    private Connection connection; 
    @MockBean
    private PreparedStatement preparedStatement;
    @MockBean
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();



        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        

        when(resultSet.next()).thenReturn(true).thenReturn(false); 
        when(resultSet.getLong("account_id")).thenReturn(1L);
        when(resultSet.getString("username")).thenReturn("testUser");
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getLong("total_games")).thenReturn(0L);
        when(resultSet.getLong("total_wins")).thenReturn(0L);
        when(resultSet.getLong("total_losses")).thenReturn(0L);
        when(resultSet.getLong("elo")).thenReturn(1000L);
    }

    @Test
    void testCreateAccount() throws Exception {
        when(connection.prepareStatement(contains("INSERT INTO account")))
            .thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(true); 

        ResultSet findRs = mock(ResultSet.class);
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
             .thenReturn(preparedStatement); 
        when(preparedStatement.executeQuery()).thenReturn(findRs);
        when(findRs.next()).thenReturn(true);
        when(findRs.getLong("account_id")).thenReturn(1L); 
        when(findRs.getString("username")).thenReturn("testUser");
        when(findRs.getString("email")).thenReturn("test@example.com");
        when(findRs.getLong("elo")).thenReturn(1000L);
        when(findRs.getLong("total_games")).thenReturn(0L);
        when(findRs.getLong("total_wins")).thenReturn(0L);
        when(findRs.getLong("total_losses")).thenReturn(0L);

        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testUser\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void testGetAccountById() throws Exception {
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet); 
        when(resultSet.next()).thenReturn(true).thenReturn(false); 
        when(resultSet.getLong("account_id")).thenReturn(1L);
        when(resultSet.getString("username")).thenReturn("testUser");
        
        mockMvc.perform(get("/api/account/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void testUpdateUsername() throws Exception {
        when(connection.prepareStatement(contains("UPDATE account SET username=?")))
            .thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(false); 

        ResultSet updatedRs = mock(ResultSet.class);
         when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
             .thenReturn(preparedStatement); 
        when(preparedStatement.executeQuery()).thenReturn(updatedRs);
        when(updatedRs.next()).thenReturn(true);
        when(updatedRs.getLong("account_id")).thenReturn(1L);
        when(updatedRs.getString("username")).thenReturn("newUsername"); 

        mockMvc.perform(patch("/api/account/1/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newUsername\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newUsername"));
    }

    @Test
    void testUpdateElo() throws Exception {
        when(connection.prepareStatement(contains("UPDATE account SET elo=?")))
            .thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(false);

        ResultSet updatedRs = mock(ResultSet.class);
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
             .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(updatedRs);
        when(updatedRs.next()).thenReturn(true);
        when(updatedRs.getLong("account_id")).thenReturn(1L);
        when(updatedRs.getLong("elo")).thenReturn(1250L);
        // ... mock rest of fields ...

        mockMvc.perform(patch("/api/account/1/elo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"elo\":1250}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elo").value(1250));
    }

    @Test
    void testDeleteAccount() throws Exception {
         when(connection.prepareStatement(contains("DELETE FROM account")))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        mockMvc.perform(delete("/api/account/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void testRecordWinAndLoss() throws Exception {
       when(connection.prepareStatement(contains("UPDATE account SET total_wins")))
            .thenReturn(preparedStatement);
       when(preparedStatement.execute()).thenReturn(false); 
       
       ResultSet winRs = mock(ResultSet.class);
       PreparedStatement findStmtWin = mock(PreparedStatement.class);
       when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
            .thenReturn(findStmtWin); 
       when(findStmtWin.executeQuery()).thenReturn(winRs);
       when(winRs.next()).thenReturn(true);
       when(winRs.getLong("account_id")).thenReturn(1L);
       when(winRs.getLong("total_wins")).thenReturn(1L);
       when(winRs.getLong("total_games")).thenReturn(1L);

        mockMvc.perform(post("/api/account/1/win"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWins").value(1))
                .andExpect(jsonPath("$.totalGames").value(1));

       when(connection.prepareStatement(contains("UPDATE account SET total_losses")))
            .thenReturn(preparedStatement);
       when(preparedStatement.execute()).thenReturn(false);

       ResultSet lossRs = mock(ResultSet.class);
       PreparedStatement findStmtLoss = mock(PreparedStatement.class);
        when(connection.prepareStatement(contains("SELECT * FROM account WHERE account_id=?")))
             .thenReturn(findStmtLoss);
       when(findStmtLoss.executeQuery()).thenReturn(lossRs);
       when(lossRs.next()).thenReturn(true);
       when(lossRs.getLong("account_id")).thenReturn(1L);
       when(lossRs.getLong("total_losses")).thenReturn(1L); 
       when(lossRs.getLong("total_games")).thenReturn(2L); 
       when(lossRs.getLong("total_wins")).thenReturn(1L); 

        mockMvc.perform(post("/api/account/1/loss"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLosses").value(1))
                .andExpect(jsonPath("$.totalGames").value(2));
    }

    @Test
    void testGetAllAccounts() throws Exception {
        Statement stmt = mock(Statement.class); 
        when(connection.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(contains("SELECT * FROM account"))).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("account_id")).thenReturn(1L, 2L);
        when(resultSet.getString("username")).thenReturn("user1", "user2");
        when(resultSet.getString("email")).thenReturn("user1@example.com", "user2@example.com");
        when(resultSet.getLong("elo")).thenReturn(1000L, 1100L);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }
}
