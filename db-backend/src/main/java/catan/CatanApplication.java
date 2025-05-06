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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

@SpringBootApplication
@RestController
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class CatanApplication {

	private final DatabaseConnectionManager dcm = new DatabaseConnectionManager("db",
			"catan", "postgres", "password");
	// is it fine for this endpoint to be publicly exposed like this? also need to add an id to acct
	@PostMapping("/api/account")
	public ResponseEntity<?> createAccount(@RequestBody Map<String, String> accountData) {
		if (accountData == null) {
			return ResponseEntity.badRequest().body(Map.of(
				"error", "Validation failed",
				"message", "Request body is required"
			));
		}

		String username = accountData.get("username");
		if (username == null || username.trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of(
				"error", "Validation failed",
				"message", "Username is required"
			));
		}

		String email = accountData.get("email");
		if (email == null || email.trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of(
				"error", "Validation failed",
				"message", "Email is required"
			));
		}

		try (Connection connection = dcm.getConnection()) {
			try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM account WHERE username = ?")) {
				stmt.setString(1, username.trim());
				ResultSet rs = stmt.executeQuery();
				if (rs.next() && rs.getInt(1) > 0) {
					return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
						"error", "Username taken",
						"message", "This username is already in use"
					));
				}
			}

			AccountDAO accountDAO = new AccountDAO(connection);
			Account account = new Account();
			
			account.setUsername(username.trim()); // trim whitespace
			account.setEmail(email.trim()); // trim whitespace
			account.setTotalGames(0); // set to 0
			account.setTotalWins(0); // set to 0
			account.setTotalLosses(0); // set to 0
			account.setElo(1000); // set to 1000 (default)
			
			try {
				Account createdAccount = accountDAO.create(account);
				if (createdAccount == null) {
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of(
							"error", "Creation failed",
							"message", "Failed to create account in database"
						));
				}
				return ResponseEntity.ok(createdAccount);
			} catch (RuntimeException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of(
						"error", "Database error",
						"message", e.getMessage(),
						"details", "Error occurred while creating account"
					));
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(
					"error", "Database error",
					"message", e.getMessage(),
					"details", "Error occurred while connecting to database"
				));
		}
	}

	// this endpoint is not being used, use for frontend to populate accounts array on frontend
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

	// unused for now
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

	// i don't think this needs to be an api endpoint actually, wouldn't want this logic publicly exposed at all
	// game end should just modify the account object through its DAO

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

	// unused right now
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

	// i don't think this endpoint should exist. 

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

	// same with this, i don't think this endpoint needs to exist.
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

	// unused
	@PostMapping("/api/games")
	public ResponseEntity<Object> createGame() {
		try (Connection connection = dcm.getConnection()) {
			GameState gameState = new GameState();
			gameState.setTurnNumber(0);
			gameState.newGame();
			gameState.setGameOver(false);
			// default card values in standard boardgame below, hard-coded
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

			GameStateDAO dao = new GameStateDAO(connection);
			
			try {
				GameState created = dao.create(gameState);
				if (created == null) {
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of(
							"error", "Failed to create game state",
							"message", "Database operation completed but returned null"
						));
				}
				return ResponseEntity.ok(created);
			} catch (RuntimeException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of(
						"error", "Failed to create game state",
						"message", e.getMessage()
					));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(
					"error", "Database connection error",
					"message", e.getMessage()
				));
		}
	}
	// unused 
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

	// unused
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

	// unused
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

	// unused
	@PostMapping("/api/games/{gameId}/players/{accountId}/hand")
	public ResponseEntity<Object> createPlayerHand(
			@PathVariable long gameId,
			@PathVariable long accountId,
			@RequestBody Map<String, Long> data) {
		
		try (Connection connection = dcm.getConnection()) {
			GameStateDAO gameStateDAO = new GameStateDAO(connection);
			GameState gameState = gameStateDAO.findById(gameId);
			if (gameState == null) {
				System.err.println("ERROR - Game not found with ID: " + gameId);
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Game not found", "gameId", gameId));
			}

			AccountDAO accountDAO = new AccountDAO(connection);
			Account account = accountDAO.findById(accountId);
			if (account == null) {
				System.err.println("ERROR - Account not found with ID: " + accountId);
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Account not found", "accountId", accountId));
			}

			PlayerStateDAO dao = new PlayerStateDAO(connection);
			
			PlayerState existingState = dao.findPlayerState(accountId, gameId, gameState.getTurnNumber());
			if (existingState != null) {
				System.err.println("ERROR - Player state already exists for this combination");
				return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("error", "Player state already exists for this game, account, and turn"));
			}

			try {
				PlayerState playerState = dao.createEmptyHand(accountId, gameId, gameState.getTurnNumber());
				if (playerState != null) {
					return ResponseEntity.ok(playerState);
				} else {
					System.err.println("ERROR - Failed to create player state (null returned)");
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("error", "Failed to create player state"));
				}
			} catch (RuntimeException e) {
				System.err.println("ERROR - Exception while creating player state: " + e.getMessage());
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of(
						"error", "Failed to create player state",
						"message", e.getMessage()
					));
			}
		} catch (SQLException e) {
			System.err.println("ERROR - Database connection error: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(
					"error", "Database error",
					"message", e.getMessage()
				));
		}
	}

	// unused
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

	// unused
	@PostMapping("/api/games/{gameId}/players/{accountId}/gain")
	public ResponseEntity<Object> gainResources(
			@PathVariable long gameId,
			@PathVariable long accountId,
			@RequestBody Map<String, Object> data) {

		try (Connection connection = dcm.getConnection()) {
			GameStateDAO gameStateDAO = new GameStateDAO(connection);
			GameState gameState = gameStateDAO.findById(gameId);
			if (gameState == null) {
				System.err.println("ERROR - Game not found with ID: " + gameId);
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Game not found", "gameId", gameId));
			}

			if (!data.containsKey("resourceType") || 
				!data.containsKey("numSettlements") || !data.containsKey("numCities")) {
				System.err.println("ERROR - Missing required parameters in request body");
				return ResponseEntity.badRequest()
					.body(Map.of("error", "Missing required parameters. Need: resourceType, numSettlements, numCities"));
			}

			PlayerStateDAO dao = new PlayerStateDAO(connection);
			try {
				PlayerState updated = dao.updateOnBoardGain(
					accountId,
					gameId,
					gameState.getTurnNumber(),
					data.get("resourceType").toString(),
					Integer.parseInt(data.get("numSettlements").toString()),
					Integer.parseInt(data.get("numCities").toString())
				);

				if (updated != null) {
					return ResponseEntity.ok(updated);
				} else {
					System.err.println("ERROR - Player state not found for the given combination");
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of(
							"error", "Player state not found. Make sure to create a player hand first using /hand endpoint",
							"gameId", gameId,
							"accountId", accountId,
							"turnNumber", gameState.getTurnNumber()
						));
				}
			} catch (NumberFormatException e) {
				System.err.println("ERROR - Invalid number format in request parameters: " + e.getMessage());
				return ResponseEntity.badRequest()
					.body(Map.of("error", "Invalid number format in parameters"));
			} catch (RuntimeException e) {
				System.err.println("ERROR - Exception while updating resources: " + e.getMessage());
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of(
						"error", "Failed to update resources",
						"message", e.getMessage()
					));
			}
		} catch (SQLException e) {
			System.err.println("ERROR - Database connection error: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(
					"error", "Database error",
					"message", e.getMessage()
				));
		}
	}

	// unused
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

	// unused
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
	// there is no need to have an api endpoint that returns all accts in the db
	@GetMapping("/api/accounts")
	@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
	public ResponseEntity<?> getAllAccounts() {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			List<Account> accounts = accountDAO.findAll();
			if (accounts.isEmpty()) {
				return ResponseEntity.ok(List.of());
			}
			return ResponseEntity.ok(accounts);
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(
					"error", "Database error",
					"message", e.getMessage(),
					"details", "Error occurred while fetching accounts"
				));
		}
	}

	@GetMapping("/api/games/{gameId}")
	public ResponseEntity<Object> getGameState(@PathVariable long gameId) {
		try (Connection connection = dcm.getConnection()) {
			GameStateDAO gameStateDAO = new GameStateDAO(connection);
			GameState gameState = gameStateDAO.findById(gameId);
			
			if (gameState == null) {
				return ResponseEntity.notFound().build();
			}
			
			return ResponseEntity.ok(Map.of(
				"gameState", gameState, 
				"isSetupPhase", gameState.isSetupPhase(),
				"currentPlayer", gameState.getCurrentPlayer()
			));
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(
					"error", "Database error",
					"message", e.getMessage()
				));
		}
	}

	@PostMapping("/api/games/{gameId}/join")
	public ResponseEntity<Object> joinGame(@PathVariable long gameId, @RequestBody Map<String, Long> data) {
		if (!data.containsKey("accountId")) {
			return ResponseEntity.badRequest()
				.body(Map.of("error", "Missing accountId in request body")); 
		}
		Long accountId = data.get("accountId");

		try (Connection connection = dcm.getConnection()) {
			GameStateDAO gameStateDAO = new GameStateDAO(connection);
			GameState gameState = gameStateDAO.findById(gameId);
			
			if (gameState == null) {
				return ResponseEntity.notFound()
					.build();
			}

			// add player to the game's list of players
			gameState.addPlayer(accountId);

			// create initial player state
			PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
			PlayerState playerState = new PlayerState();
			playerState.setAccountId(accountId);
			playerState.setGameId(gameId);
			playerState.setTurnNumber(gameState.getTurnNumber());
			
			// init resources to 0
			playerState.setBrick(0);
			playerState.setOre(0);
			playerState.setSheep(0);
			playerState.setWheat(0);
			playerState.setWood(0);
			
			// init development cards to 0
			playerState.setKnight(0);
			playerState.setMonopoly(0);
			playerState.setYearOfPlenty(0);
			playerState.setVictoryPoint(0);
			playerState.setRoadBuilding(0);
			
			// init building counts to 0
			playerState.setNumSettlements(0);
			playerState.setNumCities(0);
			playerState.setNumRoads(0);
			
			playerStateDAO.create(playerState);
			gameStateDAO.update(gameState);

			return ResponseEntity.ok(Map.of(
				"message", "Successfully joined game",
				"gameState", gameState,
				"playerState", playerState
			));
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(
					"error", "Database error",
					"message", e.getMessage()
				));
		}
	}

	@PostMapping("/api/games/{gameId}/setup-action")
	public ResponseEntity<Object> executeSetupAction(
			@PathVariable long gameId,
			@RequestBody Map<String, Object> data) {
		
		if (!data.containsKey("accountId") || !data.containsKey("action") || 
			!data.containsKey("v1") || !data.containsKey("v2")) {
			return ResponseEntity.badRequest()
				.body(Map.of("error", "Missing required parameters"));
		}

		try (Connection connection = dcm.getConnection()) {
			GameStateDAO gameStateDAO = new GameStateDAO(connection);
			PlayerStateDAO playerStateDAO = new PlayerStateDAO(connection);
			
			long accountId = Long.parseLong(data.get("accountId").toString());
			GameAction gameAction = new GameAction(gameStateDAO, playerStateDAO);
			gameAction.setGameId(gameId);
			gameAction.setAccountId(accountId);
			
			int v1 = Integer.parseInt(data.get("v1").toString());
			int v2 = Integer.parseInt(data.get("v2").toString());
			
			gameAction.executeAction(
				data.get("action").toString(),
				v1,
				v2
			);

			GameState gameState = gameStateDAO.findById(gameId);
			PlayerState playerState = playerStateDAO.findPlayerState(accountId, gameId, gameState.getTurnNumber());

			return ResponseEntity.ok(Map.of(
				"gameState", gameState,
				"playerState", playerState
			));
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(
					"error", "Database error",
					"message", e.getMessage()
				));
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(CatanApplication.class, args);
	}
}