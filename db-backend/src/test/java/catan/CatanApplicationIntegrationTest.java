package catan;
/*
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CatanApplicationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private DatabaseConnectionManager dcm;

    @MockBean
    private Connection connection;

    @MockBean
    private PreparedStatement preparedStatement;

    @MockBean
    private ResultSet resultSet;

    // executes before mockitos 
    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        when(dcm.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        
        // mock successful account creation to test downstream
        when(resultSet.next()).thenReturn(true);
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
        when(preparedStatement.execute()).thenReturn(true);

        // test request validity 
        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testUser\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk());

        // test missing username
        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAccountById() throws Exception {
        mockMvc.perform(get("/api/account/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateUsername() throws Exception {
        when(preparedStatement.execute()).thenReturn(true);

        mockMvc.perform(patch("/api/account/1/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newUsername\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateElo() throws Exception {
        when(preparedStatement.execute()).thenReturn(true);

        mockMvc.perform(patch("/api/account/1/elo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"elo\":1250}"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteAccount() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        mockMvc.perform(delete("/api/account/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"deleted\":true}"));
    }

    @Test
    void testRecordWinAndLoss() throws Exception {
        when(preparedStatement.execute()).thenReturn(true);

        // test recording win through api
        mockMvc.perform(post("/api/account/1/win"))
                .andExpect(status().isOk());

        // test recording loss through api
        mockMvc.perform(post("/api/account/1/loss"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllAccounts() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}  */