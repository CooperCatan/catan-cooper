package catan;

import java.util.List;
import java.util.Scanner;

public class playGame {
    private final testBoard gameBoard;
    private final Player[] playerList;
    private final Bank gameBank;
    //1 when on, 0 when over
    int gameStatus;
    int round;
    int turn;

    public playGame(testBoard bEntry, Player[] pEntry, Bank bankEntry) {
        this.gameBoard = bEntry;
        this.playerList = pEntry;
        this.gameBank = bankEntry;
        this.gameStatus = 1;
        this.round = 0;
        this.turn = 0;
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
        Bank bank = new Bank();
        playGame catan = new playGame(board, players, bank);
        int winner = 0;

        //Start game
        while(catan.gameStatus != 0) {
            for(int i = 0; i < 4; i++) {
                //Roll the dice and distribute resources
                catan.rollDice();

                //Prompt player for action continually
                catan.parseAction(catan.playerList[i].getPID());

                //Calculate Longest Road/Biggest Army
                catan.calcLR(catan.playerList[i].getPID());
                catan.calcBA(catan.playerList[i].getPID());

                //Check Win conditions
                winner = catan.calcVP();
                catan.turn++;
            }
            catan.round++;
        }

        //Announce Winner
        System.out.println("Winner: " + winner);
    }

    public void rollDice() {
        int roll;
        roll = (int) (Math.random() * 6) + 1 + (int) (Math.random() * 6) + 1;
        System.out.println("The number rolled was: " + roll);
        //TODO -- ROBBER MECHANICS
    }

    public void parseAction(int pid) {
        boolean actionSel = false;
        String ans;
        while(!actionSel) {
            System.out.print("Enter a valid game action: ");
            System.out.print("1: Place a Settlement or Road");
            System.out.print("2: Purchase or Play a Development Card");
            System.out.print("3: Trade with the Bank or Player");
            System.out.print("4: End Turn");
            Scanner scanner = new Scanner(System.in);
            int action = scanner.nextInt();
            switch(action) {
                case 1:
                    int v1;
                    int v2;
                    System.out.print("1: Place a Settlement");
                    System.out.print("2: Place a City");
                    System.out.print("3: Place a Road");
                    action = scanner.nextInt();
                    switch(action) {
                        case 1:
                            System.out.print("Enter the vertex id: ");
                            ans = scanner.next();
                            v1 = Integer.parseInt(ans);
                            //v2 of build unused
                            gameBoard.build("SETTLEMENT", v1, 0, pid);
                            break;
                        case 2:
                            System.out.print("Enter the vertex id: ");
                            ans = scanner.next();
                            v1 = Integer.parseInt(ans);
                            //v2 of build unused
                            gameBoard.build("CITY", v1, 0, pid);
                            break;
                        case 3:
                            System.out.print("Enter the start vertex id: ");
                            ans = scanner.next();
                            v1 = Integer.parseInt(ans);
                            System.out.print("Enter the end vertex id: ");
                            ans = scanner.next();
                            v2 = Integer.parseInt(ans);
                            gameBoard.build("ROAD", v1, v2, pid);
                            break;
                        default:
                            System.out.println("Invalid action");
                            break;
                    }
                    break;
                case 2:
                    System.out.print("1: Purchase a Development Card");
                    System.out.print("2: Play a Development Card");
                    action = scanner.nextInt();
                    switch(action) {
                        case 1:
                            getDevCard(pid);
                        case 2:
                            //TODO -- IMPLEMENT DEV CARD FUNCTIONALITY
                    }
                    break;
                case 3:
                    System.out.print("1: Trade with the Bank");
                    System.out.print("2: Trade with a Player");
                    action = scanner.nextInt();
                    switch(action) {
                        case 1:
                            //TODO -- BANK TRADES
                        case 2:
                            //TODO -- PLAYER TRADES
                    }
                    break;
                case 4:
                    actionSel = true;
                    break;
                default:
                    System.out.println("Invalid action");
                    break;
            }
        }
    }

    public void getDevCard(int pid) {
        //TODO -- CHECK AND SUBTRACT RESOURCES
        //This generates and removes a card from the bank at random from the Dev Card Deck
        int card = gameBank.devCard();
        //Find the player that is grabbing a card
        int index;
        for(index = 0; index < playerList.length; index++) {
            if(playerList[index].getPID() == pid) {
                break;
            }
        }
        switch(card) {
            case 1:
                //TODO -- ADD CARDS TO PLAYER HAND
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            default:
                System.out.println("No cards remaining!");
                break;
        }
        System.out.print("Player " + pid + " got a: " + " ");
    }

    public void calcLR(int pid) {
        //If new longest road is found, change that players vp total
        //TODO -- LONGEST ROAD ALGORITHM
        //If new longest road, remove vp from the previous holder
    }
    public void calcBA(int pid) {
        //If new largest army is found, change that players vp total
        //TODO -- BIGGEST ARMY CALC (EASY WITH DEV CARDS)
        //If new largest army, remove vp from the previous holder
    }

    public int calcVP() {
        //If any players vp exceeds 10, set the gameStatus to 0 and return that pid
        for (Player player : this.playerList) {
            if (player.getVP() >= 10) {
                this.gameStatus = 0;
                return player.getPID();
            } else {
                System.out.println("Player " + player.getPID() + " possesses: " + player.getVP() + " Victory Points!");
            }
        }
        return 0;
    }
}
