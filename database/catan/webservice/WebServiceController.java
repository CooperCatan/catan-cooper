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

    @GetMapping("/player/{id}")
    public Player getPlayer(@PathVariable long id){
        System.out.println("Getting player with id: " + id);
        DatabaseConnectionManager dcm = new DatabaseConnectionManager("localhost","catan","postgres","password");

        try(Connection connection = dcm.getConnection()){
            PlayerDAO playerDAO = new PlayerDAO(connection);
            return playerDAO.read(id);
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
    @PostMapping("/account")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        try (Connection connection = getConnection()) {
            AccountDAO accountDAO = new AccountDAO(connection);
            return ResponseEntity.ok(accountDAO.create(account));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable long id) {
        try (Connection connection = getConnection()) {
            AccountDAO accountDAO = new AccountDAO(connection);
            Account account = accountDAO.read(id);
            if (account != null) {
                return ResponseEntity.ok(account);
            }
            return ResponseEntity.notFound().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/account/{id}/username")
    public ResponseEntity<Void> updateUsername(@PathVariable long id, @RequestBody String newUsername) {
        try (Connection connection = getConnection()) {
            AccountDAO accountDAO = new AccountDAO(connection);
            accountDAO.changeUsername(id, newUsername);
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/account/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable long id, @RequestBody String newPassword) {
        try (Connection connection = getConnection()) {
            AccountDAO accountDAO = new AccountDAO(connection);
            accountDAO.changePassword(id, newPassword);
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/account/{id}/elo")
    public ResponseEntity<Void> updateElo(@PathVariable long id, @RequestBody int newElo) {
        try (Connection connection = getConnection()) {
            AccountDAO accountDAO = new AccountDAO(connection);
            accountDAO.updateElo(id, newElo);
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/account/{id}")
    public ResponseEntity<Account> deleteAccount(@PathVariable long id) {
        try (Connection connection = getConnection()) {
            AccountDAO accountDAO = new AccountDAO(connection);
            Account deletedAccount = accountDAO.delete(id);
            if (deletedAccount != null) {
                return ResponseEntity.ok(deletedAccount);
            }
            return ResponseEntity.notFound().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/player-state")
    public ResponseEntity<PlayerState> createPlayerState(@RequestBody PlayerState playerState) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            return ResponseEntity.ok(playerStateDAO.create(playerState));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/player-state/{accountId}/{gameId}/{turnNumber}")
    public ResponseEntity<PlayerState> getPlayerState(
            @PathVariable long accountId,
            @PathVariable long gameId,
            @PathVariable long turnNumber) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            PlayerState state = playerStateDAO.readPlayerState(accountId, gameId, turnNumber);
            if (state != null) {
                return ResponseEntity.ok(state);
            }
            return ResponseEntity.notFound().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/player-state/{accountId}/{gameId}/{turnNumber}/resource/{resource}")
    public ResponseEntity<Boolean> hasResource(
            @PathVariable long accountId,
            @PathVariable long gameId,
            @PathVariable long turnNumber,
            @PathVariable String resource) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            return ResponseEntity.ok(playerStateDAO.hasResource(accountId, gameId, turnNumber, resource));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/player-state/{accountId}/{gameId}/{turnNumber}/victory-points")
    public ResponseEntity<Integer> getVictoryPoints(
            @PathVariable long accountId,
            @PathVariable long gameId,
            @PathVariable long turnNumber) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            return ResponseEntity.ok(playerStateDAO.getVictoryPoints(accountId, gameId, turnNumber));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/player-state/{accountId}/{gameId}/{turnNumber}/board-gain")
    public ResponseEntity<Void> updateBoardGain(
            @PathVariable long accountId,
            @PathVariable long gameId,
            @PathVariable long turnNumber,
            @RequestBody Map<String, Integer> resources) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            playerStateDAO.updateBoardGain(accountId, gameId, turnNumber, resources);
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/player-state/{accountId}/{gameId}/{turnNumber}/robber-loss")
    public ResponseEntity<Void> updateRobberLoss(
            @PathVariable long accountId,
            @PathVariable long gameId,
            @PathVariable long turnNumber,
            @RequestBody Map<String, Integer> resources) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            playerStateDAO.updateRobberLoss(accountId, gameId, turnNumber, resources);
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/player-state/{accountId}/{gameId}/{turnNumber}/yop-gain")
    public ResponseEntity<Void> updateYearOfPlentyGain(
            @PathVariable long accountId,
            @PathVariable long gameId,
            @PathVariable long turnNumber,
            @RequestBody Map<String, String> resources) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            playerStateDAO.updateYearOfPlentyGain(accountId, gameId, turnNumber, 
                resources.get("resource1"), resources.get("resource2"));
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/player-state/{accountId}/{gameId}/{turnNumber}/mono-gain")
    public ResponseEntity<Void> updateMonopolyGain(
            @PathVariable long accountId,
            @PathVariable long gameId,
            @PathVariable long turnNumber,
            @RequestBody Map<String, Object> monopolyData) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            playerStateDAO.updateMonopolyGain(accountId, gameId, turnNumber,
                (String) monopolyData.get("resource"),
                (Integer) monopolyData.get("amount"));
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/player-state/{accountId}/{gameId}/{turnNumber}/longest-road")
    public ResponseEntity<Void> updateLongestRoad(
            @PathVariable long accountId,
            @PathVariable long gameId,
            @PathVariable long turnNumber,
            @RequestBody boolean hasLongestRoad) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            playerStateDAO.updateLongestRoad(accountId, gameId, turnNumber, hasLongestRoad);
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/player-state/{accountId}/{gameId}/{turnNumber}/largest-army")
    public ResponseEntity<Void> updateLargestArmy(
            @PathVariable long accountId,
            @PathVariable long gameId,
            @PathVariable long turnNumber,
            @RequestBody boolean hasLargestArmy) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            playerStateDAO.updateLargestArmy(accountId, gameId, turnNumber, hasLargestArmy);
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/player-state/{accountId}/{gameId}/{turnNumber}")
    public ResponseEntity<PlayerState> deletePlayerState(
            @PathVariable long accountId,
            @PathVariable long gameId,
            @PathVariable long turnNumber) {
        try (Connection connection = getConnection()) {
            PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
            PlayerState deletedState = playerStateDAO.deletePlayerState(accountId, gameId, turnNumber);
            if (deletedState != null) {
                return ResponseEntity.ok(deletedState);
            }
            return ResponseEntity.notFound().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private Connection getConnection() throws SQLException {
        DatabaseConnectionManager dcm = new DatabaseConnectionManager("localhost", "catan", "postgres", "password");
        return dcm.getConnection();
    }
}