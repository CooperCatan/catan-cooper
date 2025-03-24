package catan;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.HttpStatus;

@SpringBootApplication
@RestController
public class CatanApplication {

	private final DatabaseConnectionManager dcm = new DatabaseConnectionManager("db",
			"catan", "postgres", "password");

	@PostMapping("/api/account")
	public ResponseEntity<?> createAccount(@RequestBody Map<String, String> accountData) {
		if (accountData.get("username") == null || accountData.get("username").trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Username is required");
		}
		if (accountData.get("password") == null || accountData.get("password").trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Password is required");
		}

		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			Account account = new Account();
			
			account.setUsername(accountData.get("username").trim());
			account.setPassword(accountData.get("password"));

			// initialize stats to 0 upon account creation
			account.setTotalGames(0);
			account.setTotalWins(0);
			account.setTotalLosses(0);
			account.setElo(1000); // default ELO, can change later
			
			Account createdAccount = accountDAO.create(account);
			return ResponseEntity.ok(createdAccount);
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
				.body("Failed to create account: " + e.getMessage());
		}
	}

	@GetMapping("/api/account/{id}")
	public ResponseEntity<Account> getAccountById(@PathVariable("id") long id) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			Account account = accountDAO.findById(id);
			if (account != null) {
				return ResponseEntity.ok(account);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	@PatchMapping("/api/account/{id}/username")
	public ResponseEntity<Account> updateUsername(@PathVariable("id") long id, @RequestBody Map<String, String> data) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			Account account = accountDAO.updateUsername(id, data.get("username"));
			if (account != null) {
				return ResponseEntity.ok(account);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	@PatchMapping("/api/account/{id}/password")
	public ResponseEntity<Account> updatePassword(@PathVariable("id") long id, @RequestBody Map<String, String> data) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			Account account = accountDAO.updatePassword(id, data.get("password"));
			if (account != null) {
				return ResponseEntity.ok(account);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	@PatchMapping("/api/account/{id}/elo")
	public ResponseEntity<Account> updateElo(@PathVariable("id") long id, @RequestBody Map<String, Long> data) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			Account account = accountDAO.updateElo(id, data.get("elo"));
			if (account != null) {
				return ResponseEntity.ok(account);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	@DeleteMapping("/api/account/{id}")
	public ResponseEntity<Map<String, Boolean>> deleteAccount(@PathVariable("id") long id) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			boolean deleted = accountDAO.delete(id);
			return ResponseEntity.ok(Map.of("deleted", deleted));
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("deleted", false));
		}
	}

	@PostMapping("/api/account/{id}/win")
	public ResponseEntity<?> recordWin(@PathVariable("id") long id) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			Account updatedAccount = accountDAO.incrementWins(id);
			if (updatedAccount != null) {
				return ResponseEntity.ok(updatedAccount);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
				.body("Failed to record win: " + e.getMessage());
		}
	}

	@PostMapping("/api/account/{id}/loss")
	public ResponseEntity<?> recordLoss(@PathVariable("id") long id) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			Account updatedAccount = accountDAO.incrementLosses(id);
			if (updatedAccount != null) {
				return ResponseEntity.ok(updatedAccount);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
				.body("Failed to record loss: " + e.getMessage());
		}
	}

	@PostMapping("/api/games")
	public ResponseEntity<Object> createGame() {
		try (Connection connection = dcm.getConnection()) {
			System.out.println("DEBUG - Creating new game state");
			GameState gameState = new GameState();
			gameState.setTurnNumber(0); // start with turn 0
			gameState.setBoardState(null); // will be set to {} in DAO
			gameState.setRobberLocation(null); // will be set to {"hex": "desert"} in DAO
			gameState.setGameOver(false);
			gameState.setBankBrick(19);
			gameState.setBankOre(19);
			gameState.setBankSheep(19);
			gameState.setBankWheat(19);
			gameState.setBankWood(19);
			gameState.setBankYearOfPlenty(2);
			gameState.setBankMonopoly(2);
			gameState.setBankRoadBuilding(2);
			gameState.setBankVictoryPoint(5);
			gameState.setBankKnight(14);

			System.out.println("DEBUG - Game state initialized with default values");
			GameStateDAO dao = new GameStateDAO(connection);
			
			try {
				GameState created = dao.create(gameState);
				if (created == null) {
					System.err.println("ERROR - Failed to create game state: result was null");
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of(
							"error", "Failed to create game state",
							"message", "Database operation completed but returned null"
						));
				}
				System.out.println("DEBUG - Game created successfully with ID: " + created.getGameId());
				return ResponseEntity.ok(created);
			} catch (RuntimeException e) {
				System.err.println("ERROR - Failed to create game state: " + e.getMessage());
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of(
						"error", "Failed to create game state",
						"message", e.getMessage()
					));
			}
		} catch (SQLException e) {
			System.err.println("ERROR - Database connection error: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(
					"error", "Database connection error",
					"message", e.getMessage()
				));
		}
	}

	@GetMapping("/api/games/{gameId}/longest-road")
	public ResponseEntity<Map<String, Object>> getLongestRoadHolder(@PathVariable long gameId) {
		try (Connection connection = dcm.getConnection()) {
			GameStateDAO dao = new GameStateDAO(connection);
			GameState gameState = dao.findById(gameId);
			if (gameState == null) {
				return ResponseEntity.notFound().build();
			}
			
			Long accountId = dao.findLongestRoadHolder(gameId, gameState.getTurnNumber());
			Map<String, Object> response = new HashMap<>();
			response.put("gameId", gameId);
			response.put("turnNumber", gameState.getTurnNumber());
			response.put("longestRoadHolder", accountId);
			
			return ResponseEntity.ok(response);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/api/games/{gameId}/largest-army")
	public ResponseEntity<Map<String, Object>> getLargestArmyHolder(@PathVariable long gameId) {
		try (Connection connection = dcm.getConnection()) {
			GameStateDAO dao = new GameStateDAO(connection);
			GameState gameState = dao.findById(gameId);
			if (gameState == null) {
				return ResponseEntity.notFound().build();
			}
			
			Long accountId = dao.findLargestArmyHolder(gameId, gameState.getTurnNumber());
			Map<String, Object> response = new HashMap<>();
			response.put("gameId", gameId);
			response.put("turnNumber", gameState.getTurnNumber());
			response.put("largestArmyHolder", accountId);
			
			return ResponseEntity.ok(response);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/api/games/{gameId}")
	public ResponseEntity<Map<String, Boolean>> deleteGame(@PathVariable long gameId) {
		try (Connection connection = dcm.getConnection()) {
			GameStateDAO dao = new GameStateDAO(connection);
			boolean deleted = dao.delete(gameId);
			return ResponseEntity.ok(Map.of("deleted", deleted));
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("deleted", false));
		}
	}

	@PostMapping("/api/games/{gameId}/players/{accountId}/hand")
	public ResponseEntity<PlayerState> createPlayerHand(
			@PathVariable long gameId,
			@PathVariable long accountId,
			@RequestBody Map<String, Long> data) {
		try (Connection connection = dcm.getConnection()) {
			PlayerStateDAO dao = new PlayerStateDAO(connection);
			PlayerState playerState = dao.createEmptyHand(accountId, gameId, data.get("turnNumber"));
			if (playerState != null) {
				return ResponseEntity.ok(playerState);
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/api/games/{gameId}/players/{accountId}/resources")
	public ResponseEntity<Map<String, Object>> checkResources(
			@PathVariable long gameId,
			@PathVariable long accountId,
			@RequestParam long turnNumber,
			@RequestParam String resourceType,
			@RequestParam long quantity) {
		try (Connection connection = dcm.getConnection()) {
			PlayerStateDAO dao = new PlayerStateDAO(connection);
			boolean hasEnough = dao.hasEnoughResources(accountId, gameId, turnNumber, resourceType, quantity);
			Map<String, Object> response = new HashMap<>();
			response.put("hasEnough", hasEnough);
			response.put("resourceType", resourceType);
			response.put("quantityRequested", quantity);
			return ResponseEntity.ok(response);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/api/games/{gameId}/players/{accountId}/gain")
	public ResponseEntity<PlayerState> gainResources(
			@PathVariable long gameId,
			@PathVariable long accountId,
			@RequestBody Map<String, Object> data) {
		try (Connection connection = dcm.getConnection()) {
			PlayerStateDAO dao = new PlayerStateDAO(connection);
			PlayerState updated = dao.updateOnBoardGain(
				accountId,
				gameId,
				Long.parseLong(data.get("turnNumber").toString()),
				data.get("resourceType").toString(),
				Integer.parseInt(data.get("numSettlements").toString()),
				Integer.parseInt(data.get("numCities").toString())
			);
			if (updated != null) {
				return ResponseEntity.ok(updated);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/api/games/{gameId}/players/{accountId}/robber")
	public ResponseEntity<PlayerState> robberLoss(
			@PathVariable long gameId,
			@PathVariable long accountId,
			@RequestBody Map<String, Long> data) {
		try (Connection connection = dcm.getConnection()) {
			PlayerStateDAO dao = new PlayerStateDAO(connection);
			PlayerState updated = dao.updateOnRobberLoss(accountId, gameId, data.get("turnNumber"));
			if (updated != null) {
				return ResponseEntity.ok(updated);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/api/games/{gameId}/players/{accountId}/hand")
	public ResponseEntity<Map<String, Boolean>> deletePlayerHand(
			@PathVariable long gameId,
			@PathVariable long accountId,
			@RequestParam long turnNumber) {
		try (Connection connection = dcm.getConnection()) {
			PlayerStateDAO dao = new PlayerStateDAO(connection);
			boolean deleted = dao.deletePlayerHand(accountId, gameId, turnNumber);
			return ResponseEntity.ok(Map.of("deleted", deleted));
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("deleted", false));
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(CatanApplication.class, args);
	}
}
