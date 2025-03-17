package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class TurnService {
    @Autowired
    private GameStateRepository gameStateRepository;
    @Autowired
    private PlayerStateRepository playerStateRepository;
    @Autowired
    private GameActionRepository actionRepository;
    @Autowired
    private GameWebSocketController webSocketController;

    private final Random random = new Random();

    // Rolls dice and advances turn (Task: Turn-Based System - 3 weeks)
    public GameState rollDice(Long gameId, Long playerId) {
        GameState current = getCurrentGameState(gameId);
        PlayerState player = playerStateRepository.findByGameIdAndTurnNumberAndAccountId(gameId, current.getTurnNumber(), playerId)
            .orElseThrow(() -> new IllegalStateException("Player not found"));

        int diceRoll = random.nextInt(6) + 1 + random.nextInt(6) + 1;
        GameState nextState = copyGameState(current, current.getTurnNumber() + 1);
        nextState = gameStateRepository.save(nextState);

        GameAction action = new GameAction();
        action.setGameId(gameId);
        action.setTurnNumber(nextState.getTurnNumber());
        action.setActionType("roll_dice_" + diceRoll);
        actionRepository.save(action);

        if (diceRoll == 7) {
            handleRobber(nextState, player); // TBD
        } else {
            distributeResources(nextState, diceRoll); // TBD
        }

        webSocketController.sendTurnUpdate(nextState);
        return nextState;
    }

    private GameState getCurrentGameState(Long gameId) {
        Long maxTurn = gameStateRepository.findMaxTurnNumber(gameId);
        return gameStateRepository.findByGameIdAndTurnNumber(gameId, maxTurn)
            .orElseThrow(() -> new IllegalStateException("Game not found"));
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

    private void handleRobber(GameState state, PlayerState player) { /* TBD */ }
    private void distributeResources(GameState state, int diceRoll) { /* TBD */ }
}
