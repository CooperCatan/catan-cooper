package catan;

import catan.util.DataTransferObject;

public class Account implements DataTransferObject {

    private long accountId;

    private long accountId;
    private String username;
    private String email;
    private long totalGames;
    private long totalWins;
    private long totalLosses;
    private long elo;

    @Override
    public long getId() {
        return accountId;
    }

    public void setId(long accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(long totalGames) {
        this.totalGames = totalGames;
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

    public long getElo() {
    public long getElo() {
        return elo;
    }

    public void setElo(long elo) {
    public void setElo(long elo) {
        this.elo = elo;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                "accountId=" + accountId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", totalGames=" + totalGames +
                ", totalWins=" + totalWins + 
                ", totalLosses=" + totalLosses +
                ", elo=" + elo +
                '}';
    }
}