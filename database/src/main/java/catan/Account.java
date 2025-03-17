package catan;

import catan.util.DataTransferObject;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Account implements DataTransferObject {
    private long id;
    private String username;
    private String password;
    private long totalWins;
    private long totalLosses;
    private long totalGames;
    private int elo;

    public Account() {}

    public Account(ResultSet rs) throws SQLException {
        this.id = rs.getLong("account_id");
        this.username = rs.getString("username");
        this.password = rs.getString("password");
        this.totalWins = rs.getLong("total_wins");
        this.totalLosses = rs.getLong("total_losses");
        this.totalGames = rs.getLong("total_games");
        this.elo = rs.getInt("elo");
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(long totalWins) {
        this.totalWins = totalWins;
    }

    public long getTotalLosses() {
        return totalLosses;
    }

    public void setTotalLosses(long totalLosses) {
        this.totalLosses = totalLosses;
    }

    public long getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(long totalGames) {
        this.totalGames = totalGames;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", totalWins=" + totalWins +
                ", totalLosses=" + totalLosses +
                ", totalGames=" + totalGames +
                ", elo=" + elo +
                '}';
    }
}