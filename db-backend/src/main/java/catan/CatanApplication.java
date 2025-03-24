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
	public ResponseEntity<Void> deleteAccount(@PathVariable("id") long id) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			boolean deleted = accountDAO.delete(id);
			if (deleted) {
				return ResponseEntity.ok().build();
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
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
	public ResponseEntity<GameState> createGame() {
		try (Connection connection = dcm.getConnection()) {
			GameState gameState = new GameState();
			GameStateDAO dao = new GameStateDAO(connection);
			GameState created = dao.create(gameState);
			return ResponseEntity.ok(created);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
	public ResponseEntity<Void> deleteGame(@PathVariable long gameId) {
		try (Connection connection = dcm.getConnection()) {
			GameStateDAO dao = new GameStateDAO(connection);
			boolean deleted = dao.delete(gameId);
			if (deleted) {
				return ResponseEntity.ok().build();
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(CatanApplication.class, args);
	}
}
