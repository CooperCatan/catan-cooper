package your.package.name;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GameService {
    @Autowired
    private GameStateRepository gameStateRepository;
    @Autowired
    private PlayerStateRepository playerStateRepository;
    @Autowired
    private BankCardsRemainingRepository bankCardsRepository;

    // Creates a new game with initial state (Task: Game State Management - 3 weeks)
    public GameState createGame(List<Long> userIds, Long creatorId) {
        if (!userIds.contains(creatorId)) userIds.add(creatorId);
        if (userIds.size() < 3 || userIds.size() > 4) {
            throw new IllegalArgumentException("Game requires 3-4 players");
        }

        GameState game = new GameState();
        game.setTurnNumber(1L); // Initial turn
        game.setRobberLocation(new JSONObject().put("x", 0).put("y", 0));
        game.setBoardState(generateInitialBoard());
        game = gameStateRepository.save(game);

        // Initialize bank cards
        BankCardsRemaining bank = new BankCardsRemaining();
        bank.setGameId(game.getGameId());
        bank.setTurnNumber(1L);
        bankCardsRepository.save(bank);

        // Add players
        for (Long userId : userIds) {
            PlayerState player = new PlayerState();
            player.setAccountId(userId);
            player.setGameId(game.getGameId());
            player.setTurnNumber(1L);
            playerStateRepository.save(player);
        }
        return game;
    }

    // Fetches current game state (Task: Game State Management - 3 weeks)
    public GameState getCurrentGameState(Long gameId) {
        Long maxTurn = gameStateRepository.findMaxTurnNumber(gameId);
        return gameStateRepository.findByGameIdAndTurnNumber(gameId, maxTurn)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private JSONObject generateInitialBoard() { /* TBD: Randomize board */ return new JSONObject(); }
}
