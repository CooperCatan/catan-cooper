package catan;

public class Player {
    private int pid;
    private int vp;

    public Player(int pid) {
        this.pid = pid;
        this.vp = 0;
    }

    public int getPID() {
        return this.pid;
    }

    public int getVP() {
        return this.vp;
    }
}
