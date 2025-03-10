package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GameWebSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Broadcasts turn updates (Task: Turn-Based System - 3 weeks)
    public void sendTurnUpdate(GameState game) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("turnNumber", game.getTurnNumber());
        payload.put("gameId", game.getGameId());
        messagingTemplate.convertAndSend("/topic/game/" + game.getGameId() + "/turn", payload);
    }

    // Notifies players of trade updates (Task: Trading System - 3 weeks)
    public void sendTradeUpdate(GameState game, Trade trade) {
        messagingTemplate.convertAndSend("/topic/game/" + game.getGameId() + "/trade", trade);
    }

    // Announces winner (Task: Victory Condition Logic - 2 weeks)
    public void sendVictoryUpdate(GameState game) {
        Map<String, String> payload = Collections.singletonMap("winnerId", String.valueOf(game.getWinnerId()));
        messagingTemplate.convertAndSend("/topic/game/" + game.getGameId() + "/victory", payload);
    }
}
