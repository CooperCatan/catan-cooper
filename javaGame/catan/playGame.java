package catan;

import java.util.List;
import java.util.Scanner;

public class playGame {
    private testBoard gameBoard;
    private Player[] playerList;
    //1 when on, 0 when over
    int gameStatus;
    int round;
    int turn;
    int turnAction;

    public playGame(testBoard bentry, Player[] pentry) {
        this.gameBoard = bentry;
        this.playerList = pentry;
        this.gameStatus = 1;
        this.round = 0;
        this.turn = 0;
        this.turnAction = 0;
    }

    //TEST MAIN
    public static void main(String[] args) {
        //Set up game and board
        testBoard board = new testBoard();
        Player p1 = new Player(1023);
        Player p2 = new Player(4176);
        Player p3 = new Player(8221);
        Player p4 = new Player(7145);
        Player[] players = {p1, p2, p3, p4};
        playGame catan = new playGame(board, players);
        int winner = 0;

        //Start game
        while(catan.gameStatus != 0) {
            for(int i = 0; i < 4; i++) {
                //Roll the dice and distribute resources


                //Prompt player for action continually
                catan.parseAction();

                //Calculate Longest Road/Biggest Army
                catan.calcLR();
                catan.calcBA();

                //Check Win conditions
                winner = catan.calcVP();
                catan.turn++;
            }
            catan.round++;
        }

        //Announce Winner
    }

    public void parseAction() {
        boolean actionSel = false;
        while(!actionSel) {
            System.out.print("Enter a valid game action: ");
            System.out.print("1: Build a Structure or Road");
            System.out.print("2: Purchase or Play a Development Card");
            System.out.print("3: Trade with the Bank or Player");
            Scanner scanner = new Scanner(System.in);
            int action = scanner.nextInt();
            switch(action) {
                case 1:

            }
        }
    }

    public void calcLR() {
        //If new longest road is found, change that players vp total

        //If new longest road, remove vp from the previous holder
    }
    public void calcBA() {
        //If new largest army is found, change that players vp total

        //If new largest army, remove vp from the previous holder
    }

    public int calcVP() {
        //If any players vp exceeds 10, set the gameStatus to 0 and return that pid
        for(int i = 0; i < this.playerList.length; i++) {
            if(this.playerList[i].getVP() >= 10) {
                this.gameStatus = 0;
                return this.playerList[i].getPID();
            } else {
                System.out.println("Player " + this.playerList[i].getPID() + " possesses: " + this.playerList[i].getVP() + " Victory Points!");
            }
        }
    }
}
