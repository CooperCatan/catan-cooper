package catan;

import catan.util.DataTransferObject;

public class Account implements DataTransferObject {
    private long accountId;
    private String username;
    private String password;
    private long totalWins;
    private long totalLosses;
    private long totalGames;
    private long elo;

    @Override
    public long getId() {
        return accountId;
    }

    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public long getTotalWins() { return totalWins; }
    public void setTotalWins(long totalWins) { this.totalWins = totalWins; }

    public long getTotalLosses() { return totalLosses; }
    public void setTotalLosses(long totalLosses) { this.totalLosses = totalLosses; }

    public long getTotalGames() { return totalGames; }
    public void setTotalGames(long totalGames) { this.totalGames = totalGames; }
    
    public long getElo() { return elo; }
    public void setElo(long elo) { this.elo = elo; }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", username='" + username + '\'' +
                ", totalWins=" + totalWins +
                ", totalLosses=" + totalLosses +
                ", totalGames=" + totalGames +
                ", elo=" + elo +
                '}';
    }
} 