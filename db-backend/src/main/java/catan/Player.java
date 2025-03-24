package catan;

import catan.util.DataTransferObject;

public class Player implements DataTransferObject {

    private long playerId;
    private String userName;
    private String password;
    private int totalGames;
    private int totalWins;
    private int totalLosses;

    public long getId() {
        return playerId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public int getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(int totalGames) {
        this.totalGames = totalGames;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public int getTotalLosses() {
        return totalLosses;
    }

    public void setTotalLosses(int totalLosses) {
        this.totalLosses = totalLosses;
    }


    @Override
    public String toString() {
        return "Player{" +
                "playerId=" + playerId +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", totalGames=" + totalGames + '\'' +
                ", totalWins=" + totalWins + '\'' +
                ", totalLosses=" + totalLosses +
                '}';
    }
}