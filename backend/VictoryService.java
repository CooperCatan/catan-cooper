package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VictoryService {
    @Autowired
    private GameStateRepository gameStateRepository;
    @Autowired
    private PlayerStateRepository playerStateRepository;
    @Autowired
    private GameWebSocketController webSocketController;

    // Checks for victory (Task: Victory Condition Logic - 2 weeks)
    public boolean checkVictory(Long gameId) {
        GameState current = getCurrentGameState(gameId);
        List<PlayerState> players = playerStateRepository.findByGameIdAndTurnNumber(gameId, current.getTurnNumber());
        for (PlayerState player : players) {
            long totalVP = player.getNumSettlements() + (player.getNumCities() * 2) + player.getVictoryPoint()
                + (player.getLargestArmy() ? 2 : 0) + (player.getLongestRoad() ? 2 : 0);
            if (totalVP >= 10) {
                current.setWinnerId(player.getAccountId());
                current.setIsGameOver(true);
                gameStateRepository.save(current);
                webSocketController.sendVictoryUpdate(current);
                return true;
            }
        }
        return false;
    }

    private GameState getCurrentGameState(Long gameId) {
        Long maxTurn = gameStateRepository.findMaxTurnNumber(gameId);
        return gameStateRepository.findByGameIdAndTurnNumber(gameId, maxTurn)
            .orElseThrow(() -> new IllegalStateException("Game not found"));
    }
}
