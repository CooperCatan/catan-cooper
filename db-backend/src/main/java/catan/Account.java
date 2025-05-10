package catan;

import catan.util.DataTransferObject;

public class Account implements DataTransferObject {

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

    public int getTotalGames() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, totalGames));
    }

    public void setTotalGames(long totalGames) {
        this.totalGames = totalGames;
    }

    public int getTotalWins() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, totalWins));
    }

    public void setTotalWins(long totalWins) {
        this.totalWins = totalWins;
    }

    public int getTotalLosses() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, totalLosses));
    }

    public void setTotalLosses(long totalLosses) {
        this.totalLosses = totalLosses;
    }

    public int getElo() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, elo));
    }

    public void setElo(long elo) {
        this.elo = elo;
    }

    @Override
    public String toString() {
        return "Account{" +
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