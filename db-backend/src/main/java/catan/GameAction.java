package catan;

import catan.util.DataTransferObject;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GameAction implements DataTransferObject {
    private long actionId;
    private long gameId;
    private long turn;
    private long accountId;

    private String actionType;
    private GameStateDAO gameStateDAO;
    private PlayerStateDAO playerStateDAO;

    public GameAction(GameStateDAO gameStateDAO, PlayerStateDAO playerStateDAO) {
        this.gameStateDAO = gameStateDAO;
        this.playerStateDAO = playerStateDAO;
    }

    @Override
    public long getId() {
        return actionId;
    }

    public long getGameId() {
        return gameId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getTurn() {
        return turn;
    }

    public String getActionType() {
        return actionType;
    }

    public void executeAction(String action) {
        //The following code to find the GameState and PlayerState has been supplied by Github Copilot
        GameState gameState = gameStateDAO.findById(this.gameId);
        if (gameState == null) {
            System.err.println("GameState not found for gameId: " + this.gameId);
            return;
        }
        Gson gson = new Gson();
        List<Hex> hexes = gameState.deserialize(gameState.getJsonHexes(), gameState.getJsonVertices(), gameState.getJsonEdges(), gson);
        PlayerState playerState = playerStateDAO.findById(this.accountId);
        if (playerState == null) {
            System.err.println("PlayerState not found for accountId: " + this.accountId);
            return;
        }

        switch (action) {
            case "SETTLEMENT":
                //Check if the player can pay for the settlement, subtract funds if possible
                if (playerState.paySettlement()) {
                    //Check if the player can place the settlement, do so if possible, refund if not
                    if(!gameState.placeSettlement(vertex, accountId)) {
                        playerState.refundSettlement();
                    }
                }
                break;
            case "CITY":
                //Check if the player can pay for the city, subtract funds if possible
                if (playerState.payCity()) {
                    //Check if the player can place the city, do so if possible, refund if not
                    if(!gameState.placeCity(vertex, accountId)) {
                        playerState.refundCity();
                    }
                }
                break;
            case "ROAD":
                //Check if the player can pay for the road, subtract funds if possible
                if (playerState.payRoad()) {
                    //Check if the player can place the road, do so if possible, refund if not
                    if(!gameState.placeRoad(vertex1, vertex2, accountId)) {
                        playerState.refundRoad();
                    }
                }
                break;
            case "PURCHASE":
                //Check if the player can pay for the settlement, subtract funds if possible
                if (playerState.payCard()) {
                    //Check if the player can place the settlement, do so if possible, refund if not
                    int card = gameState.getCard();
                    if(card != 0) {
                        switch (card) {
                            case 1:
                                playerState.setKnight(playerState.getKnight() + 1);
                                gameState.setBankKnight(gameState.getBankKnight() - 1);
                                break;
                            case 2:
                                playerState.setMonopoly(playerState.getMonopoly() + 1);
                                gameState.setBankMonopoly(gameState.getBankMonopoly() - 1);
                                break;
                            case 3:
                                playerState.setYearOfPlenty(playerState.getYearOfPlenty() + 1);
                                gameState.setBankYearOfPlenty(gameState.getBankYearOfPlenty() - 1);
                                break;
                            case 4:
                                playerState.setVictoryPoint(playerState.getVictoryPoint() + 1);
                                gameState.setBankVictoryPoint(gameState.getBankVictoryPoint() - 1);
                                break;
                            case 5:
                                playerState.setRoadBuilding(playerState.getRoadBuilding() + 1);
                                gameState.setBankRoadBuilding(gameState.getBankRoadBuilding() - 1);
                                break;
                            default:
                                System.err.println("Invalid card: " + card);
                        }
                    } else {
                        playerState.refundCard();
                    }
                }
                break;
            case "USE":
                //If the player has the right card to use
                int card = playerState.useCard();
                if(card != 0) {
                    //Then the gameState updates to reflect that
                    switch (card) {
                        case 1:
                            playerState.setKnightUsed(playerState.getKnightUsed() + 1);
                            //TODO -- ROBBER MECHANICS
                            break;
                        case 2:
                            break;
                        default:
                            System.err.println("Invalid card: " + card);
                            break;
                    }
                }
                break;
            case "END":
                gameState.incrementTurn();
                gameStateDAO.update(gameState);
                //We have to return here because the increment in turn increases playerState values.
                //If we update playerState here, one of the players won't get any resources
                return;
                break;
            default:
                System.err.println("Invalid action: " + action);
                break;
        }

        //Update the GameState by passing it a version where all the parameters have accounted for the action
        gameStateDAO.update(gameState);
        playerStateDAO.update(playerState);
    }
}