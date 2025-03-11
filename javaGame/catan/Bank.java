package catan;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Bank {
    //Bank Resources
    private int wool;
    private int ore;
    private int wood;
    private int grain;
    private int bricks;

    //Dev Cards
    private int knights;
    private int monopoly;
    private int yop;
    private int vpCards;
    private int freeRoads;

    //The random seed for the deck
    private final Random random;

    public Bank() {
        this.wool = 19;
        this.ore = 19;
        this.wood = 19;
        this.grain = 19;
        this.bricks = 19;

        this.knights = 14;
        this.monopoly = 2;
        this.yop = 2;
        this.vpCards = 5;
        this.freeRoads = 2;

        this.random = new Random();
    }

    public boolean getWool() {
        if(this.wool == 0) {
            return false;
        } else {
            this.wool--;
            return true;
        }
    }

    public boolean getOre() {
        if(this.ore == 0) {
            return false;
        } else {
            this.ore--;
            return true;
        }
    }

    public boolean getWood() {
        if(this.wood == 0) {
            return false;
        } else {
            this.wood--;
            return true;
        }
    }

    public boolean getGrain() {
        if(this.grain == 0) {
            return false;
        } else {
            this.grain--;
            return true;
        }
    }

    public boolean getBricks() {
        if(this.bricks == 0) {
            return false;
        } else {
            this.bricks--;
            return true;
        }
    }

    //Will return 0 on failure, 1 on Knight, 2 on Monopoly, 3 on YoP, 4 on VP, 5 on Free Roads
    public int devCard() {
        List<Integer> weightedDeck = new ArrayList<>();

        //Add each card to the list based on its remaining count
        for (int i = 0; i < this.knights; i++) weightedDeck.add(1);
        for (int i = 0; i < this.monopoly; i++) weightedDeck.add(2);
        for (int i = 0; i < this.yop; i++) weightedDeck.add(3);
        for (int i = 0; i < this.vpCards; i++) weightedDeck.add(4);
        for (int i = 0; i < this.freeRoads; i++) weightedDeck.add(5);

        if (weightedDeck.isEmpty()) {
            return 0; // No more dev cards left
        }

        //Randomly select a card from the weighted list
        int selectedCard = weightedDeck.get(this.random.nextInt(weightedDeck.size()));

        //Reduce the count of the selected card
        switch (selectedCard) {
            case 1 -> this.knights--;
            case 2 -> this.monopoly--;
            case 3 -> this.yop--;
            case 4 -> this.vpCards--;
            case 5 -> this.freeRoads--;
        }

        return selectedCard;
    }
}
