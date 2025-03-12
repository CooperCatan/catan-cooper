package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Random;

@Service
public class TurnService {
    @Autowired
    private GameStateDAO gameStateDAO;
    @Autowired
    private PlayerStateDAO playerStateDAO;
    @Autowired
    private GameActionDAO gameActionDAO;
    @Autowired
    private GameWebSocketController webSocketController;

    private final Random random = new Random();

    // Rolls dice and advances turn (Task: Turn-Based System - 3 weeks)
    public GameState rollDice(Long gameId, Long playerId) throws SQLException {
        GameState current = getCurrentGameState(gameId);
        playerStateDAO.getPlayers(gameId, current.getTurnNumber()); // Validate player exists

        int diceRoll = random.nextInt(6) + 1 + random.nextInt(6) + 1;
        GameState nextState = copyGameState(current, current.getTurnNumber() + 1);
        // Insert new game state (TBD: Full JDBC implementation)
        gameActionDAO.createAction(gameId, nextState.getTurnNumber(), "roll_dice_" + diceRoll);

        if (diceRoll == 7) {
            handleRobber(nextState, playerId); // TBD
        } else {
            distributeResources(nextState, diceRoll); // TBD
        }

        webSocketController.sendTurnUpdate(nextState);
        return nextState;
    }

    private GameState getCurrentGameState(Long gameId) throws SQLException {
        GameState game = gameStateDAO.getCurrentGameState(gameId);
        if (game == null) {
            throw new IllegalStateException("Game not found");
        }
        return game;
    }

    private GameState copyGameState(GameState old, Long newTurn) {
        GameState next = new GameState();
        next.setGameId(old.getGameId());
        next.setTurnNumber(newTurn);
        next.setWinnerId(old.getWinnerId());
        next.setRobberLocation(old.getRobberLocation());
        next.setBoardState(old.getBoardState());
        next.setIsGameOver(old.getIsGameOver());
        return next;
    }

    private void handleRobber(GameState state, Long playerId) { /* TBD */ }
    private void distributeResources(GameState state, int diceRoll) { /* TBD */ }
}
