// spring boot controller for catan webservice
// will contain all the API endpoints
// this must be done last after all DAOs are created

package catan.webservice;

import catan.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


@CrossOrigin
@RestController
@RequestMapping("/api")

public class WebServiceController {
    private final DatabaseConnectionManager connectionManager;

    public WebServiceController() {
        this.connectionManager = new DatabaseConnectionManager();
    }

    @PostMapping("/account")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        try {
            AccountDAO accountDAO = new AccountDAO();
            accountDAO.create(account);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable long id) {
        try {
            AccountDAO accountDAO = new AccountDAO();
            Account account = accountDAO.findById(id);
            if (account != null) {
                return ResponseEntity.ok(account);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/account/{id}")
    public ResponseEntity<Void> updateAccount(@PathVariable long id, @RequestBody Account account) {
        try {
            AccountDAO accountDAO = new AccountDAO();
            account.setId(id);
            accountDAO.update(account);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/account/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable long id) {
        try {
            AccountDAO accountDAO = new AccountDAO();
            accountDAO.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/player-state")
    public ResponseEntity<PlayerState> createPlayerState(@RequestBody PlayerState playerState) {
        try {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO();
            playerStateDAO.create(playerState);
            return ResponseEntity.ok(playerState);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/player-state/{id}")
    public ResponseEntity<PlayerState> getPlayerState(@PathVariable long id) {
        try {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO();
            PlayerState state = playerStateDAO.findById(id);
            if (state != null) {
                return ResponseEntity.ok(state);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/player-state/{id}")
    public ResponseEntity<Void> updatePlayerState(@PathVariable long id, @RequestBody PlayerState playerState) {
        try {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO();
            playerState.setId(id);
            playerStateDAO.update(playerState);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/player-state/{id}")
    public ResponseEntity<Void> deletePlayerState(@PathVariable long id) {
        try {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO();
            playerStateDAO.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/game-action")
    public ResponseEntity<GameAction> createGameAction(@RequestBody GameAction gameAction) {
        try {
            GameActionDAO gameActionDAO = new GameActionDAO();
            gameActionDAO.create(gameAction);
            return ResponseEntity.ok(gameAction);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/game-action/{id}")
    public ResponseEntity<GameAction> getGameAction(@PathVariable long id) {
        try {
            GameActionDAO gameActionDAO = new GameActionDAO();
            GameAction action = gameActionDAO.findById(id);
            if (action != null) {
                return ResponseEntity.ok(action);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/game-action/{id}")
    public ResponseEntity<Void> updateGameAction(@PathVariable long id, @RequestBody GameAction gameAction) {
        try {
            GameActionDAO gameActionDAO = new GameActionDAO();
            gameAction.setId(id);
            gameActionDAO.update(gameAction);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/game-action/{id}")
    public ResponseEntity<Void> deleteGameAction(@PathVariable long id) {
        try {
            GameActionDAO gameActionDAO = new GameActionDAO();
            gameActionDAO.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/game-state")
    public ResponseEntity<GameState> createGameState(@RequestBody GameState gameState) {
        try {
            GameStateDAO gameStateDAO = new GameStateDAO();
            gameStateDAO.create(gameState);
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/game-state/{id}")
    public ResponseEntity<GameState> getGameState(@PathVariable long id) {
        try {
            GameStateDAO gameStateDAO = new GameStateDAO();
            GameState state = gameStateDAO.findById(id);
            if (state != null) {
                return ResponseEntity.ok(state);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/game-state/{id}")
    public ResponseEntity<Void> updateGameState(@PathVariable long id, @RequestBody GameState gameState) {
        try {
            GameStateDAO gameStateDAO = new GameStateDAO();
            gameState.setId(id);
            gameStateDAO.update(gameState);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/game-state/{id}")
    public ResponseEntity<Void> deleteGameState(@PathVariable long id) {
        try {
            GameStateDAO gameStateDAO = new GameStateDAO();
            gameStateDAO.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/trade")
    public ResponseEntity<Trade> createTrade(@RequestBody Trade trade) {
        try {
            TradeDAO tradeDAO = new TradeDAO();
            tradeDAO.create(trade);
            return ResponseEntity.ok(trade);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/trade/{id}")
    public ResponseEntity<Trade> getTrade(@PathVariable long id) {
        try {
            TradeDAO tradeDAO = new TradeDAO();
            Trade trade = tradeDAO.findById(id);
            if (trade != null) {
                return ResponseEntity.ok(trade);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/trade/{id}")
    public ResponseEntity<Void> updateTrade(@PathVariable long id, @RequestBody Trade trade) {
        try {
            TradeDAO tradeDAO = new TradeDAO();
            trade.setId(id);
            tradeDAO.update(trade);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/trade/{id}")
    public ResponseEntity<Void> deleteTrade(@PathVariable long id) {
        try {
            TradeDAO tradeDAO = new TradeDAO();
            tradeDAO.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}