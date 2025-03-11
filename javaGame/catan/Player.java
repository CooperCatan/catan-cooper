package catan;

public class Player {
    private final int pid;
    private int vp;
    private int wood;
    private int sheep;
    private int wheat;
    private int ore;
    private int bricks;
    private int knightCards;
    private int knightUsed;
    private int yop;
    private int monopoly;
    private int vpCards;
    private int freeRoads;

    public Player(int pid) {
        this.pid = pid;
        this.vp = 0;
        this.wood = 0;
        this.sheep = 0;
        this.wheat = 0;
        this.ore = 0;
        this.bricks = 0;
    }

    public int getPID() {
        return this.pid;
    }

    public int getVP() {
        return this.vp;
    }

    public void setVP(int vp) {
        this.vp = vp;
    }

    public int getWood() {
        return this.wood;
    }

    public int getSheep() {
        return this.sheep;
    }

    public int getWheat() {
        return this.wheat;
    }

    public int getOre() {
        return this.ore;
    }

    public int getBricks() {
        return this.bricks;
    }

    public void setWood(int wood) {
        this.wood = wood;
    }

    public void setSheep(int sheep) {
        this.sheep = sheep;
    }

    public void setWheat(int wheat) {
        this.wheat = wheat;
    }

    public void setOre(int ore) {
        this.ore = ore;
    }

    public void setBricks(int bricks) {
        this.bricks = bricks;
    }
}
