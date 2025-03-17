package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private GameService gameService;

    // Creates a new game (Task: Game State Management - 3 weeks)
    @PostMapping("/create")
    public ResponseEntity<GameState> createGame(@RequestBody List<Long> userIds, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gameService.createGame(userIds, user.getId()));
    }

    // Retrieves current game state (Task: Game State Management - 3 weeks)
    @GetMapping("/{gameId}")
    public ResponseEntity<GameState> getGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getCurrentGameState(gameId));
    }
}
