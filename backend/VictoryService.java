package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class VictoryService {
    @Autowired
    private GameStateDAO gameStateDAO;
    @Autowired
    private PlayerStateDAO playerStateDAO;
    @Autowired
    private GameWebSocketController webSocketController;

    // Checks for victory (Task: Victory Condition Logic - 2 weeks)
    public boolean checkVictory(Long gameId) throws SQLException {
        GameState current = getCurrentGameState(gameId);
        List<PlayerState> players = playerStateDAO.getPlayers(gameId, current.getTurnNumber());
        for (PlayerState player : players) {
            long totalVP = playerStateDAO.getVictoryPoints(player.getAccountId(), gameId, current.getTurnNumber());
            if (totalVP >= 10) {
                gameStateDAO.markGameOver(gameId, player.getAccountId());
                webSocketController.sendVictoryUpdate(current);
                return true;
            }
        }
        return false;
    }

    private GameState getCurrentGameState(Long gameId) throws SQLException {
        GameState game = gameStateDAO.getCurrentGameState(gameId);
        if (game == null) {
            throw new IllegalStateException("Game not found");
        }
        return game;
    }
}
