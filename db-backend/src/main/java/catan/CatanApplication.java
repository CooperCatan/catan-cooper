package catan;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/api/account")
public class CatanApplication {

	private final DatabaseConnectionManager dcm = new DatabaseConnectionManager("db",
			"catan", "postgres", "password");

	@PostMapping
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

	@GetMapping("/{id}")
	public Account getAccountById(@PathVariable("id") long id) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			return accountDAO.findById(id);
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to get account", e);
		}
	}

	@PatchMapping("/{id}/username")
	public Account updateUsername(@PathVariable("id") long id, @RequestBody Map<String, String> data) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			return accountDAO.updateUsername(id, data.get("username"));
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to update username", e);
		}
	}

	@PatchMapping("/{id}/password")
	public Account updatePassword(@PathVariable("id") long id, @RequestBody Map<String, String> data) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			return accountDAO.updatePassword(id, data.get("password"));
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to update password", e);
		}
	}

	@PatchMapping("/{id}/elo")
	public Account updateElo(@PathVariable("id") long id, @RequestBody Map<String, Long> data) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			return accountDAO.updateElo(id, data.get("elo"));
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to update ELO", e);
		}
	}

	@DeleteMapping("/{id}")
	public boolean deleteAccount(@PathVariable("id") long id) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			return accountDAO.delete(id);
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to delete account", e);
		}
	}

	@PostMapping("/{id}/win")
	public ResponseEntity<?> recordWin(@PathVariable("id") long id) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			Account updatedAccount = accountDAO.incrementWins(id);
			return ResponseEntity.ok(updatedAccount);
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
				.body("Failed to record win: " + e.getMessage());
		}
	}

	@PostMapping("/{id}/loss")
	public ResponseEntity<?> recordLoss(@PathVariable("id") long id) {
		try (Connection connection = dcm.getConnection()) {
			AccountDAO accountDAO = new AccountDAO(connection);
			Account updatedAccount = accountDAO.incrementLosses(id);
			return ResponseEntity.ok(updatedAccount);
		} catch(SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
				.body("Failed to record loss: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(CatanApplication.class, args);
	}
}
