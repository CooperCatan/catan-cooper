package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class TradeService {
    @Autowired
    private TradeDAO tradeDAO;
    @Autowired
    private GameStateDAO gameStateDAO;
    @Autowired
    private GameWebSocketController webSocketController;

    // Proposes a trade (Task: Trading System - 3 weeks)
    public Trade proposeTrade(Long gameId, Long fromPlayerId, Long toPlayerId, String givenResource, Long givenAmount, String receivedResource, Long receivedAmount) throws SQLException {
        GameState current = getCurrentGameState(gameId);
        Trade trade = tradeDAO.createPlayerTrade(gameId, current.getTurnNumber(), fromPlayerId, toPlayerId, givenResource, givenAmount, receivedResource, receivedAmount);
        webSocketController.sendTradeUpdate(current, trade);
        return trade;
    }

    private GameState getCurrentGameState(Long gameId) throws SQLException {
        GameState game = gameStateDAO.getCurrentGameState(gameId);
        if (game == null) {
            throw new IllegalStateException("Game not found");
        }
        return game;
    }
}
