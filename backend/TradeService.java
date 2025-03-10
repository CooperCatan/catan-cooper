package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TradeService {
    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    private PlayerStateRepository playerStateRepository;
    @Autowired
    private GameWebSocketController webSocketController;

    // Proposes a trade (Task: Trading System - 3 weeks)
    public Trade proposeTrade(Long gameId, Long fromPlayerId, Long toPlayerId, String givenResource, Long givenAmount, String receivedResource, Long receivedAmount) {
        GameState current = getCurrentGameState(gameId);
        PlayerState fromPlayer = playerStateRepository.findByGameIdAndTurnNumberAndAccountId(gameId, current.getTurnNumber(), fromPlayerId)
            .orElseThrow(() -> new IllegalStateException("Player not found"));

        Trade trade = new Trade();
        trade.setGameId(gameId);
        trade.setTurnNumber(current.getTurnNumber());
        trade.setFromPlayerId(fromPlayerId);
        trade.setToPlayerId(toPlayerId);
        trade.setGivenResource(givenResource);
        trade.setGivenAmount(givenAmount);
        trade.setReceivedResource(receivedResource);
        trade.setReceivedAmount(receivedAmount);
        tradeRepository.save(trade);

        webSocketController.sendTradeUpdate(current, trade);
        return trade;
    }

    private GameState getCurrentGameState(Long gameId) {
        Long maxTurn = gameStateRepository.findMaxTurnNumber(gameId);
        return gameStateRepository.findByGameIdAndTurnNumber(gameId, maxTurn)
            .orElseThrow(() -> new IllegalStateException("Game not found"));
    }
}
