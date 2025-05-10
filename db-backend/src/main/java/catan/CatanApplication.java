package catan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.sql.Connection;
import java.sql.SQLException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import catan.service.FirebaseAuthService;
import com.google.firebase.auth.FirebaseAuthException;
import io.github.bucket4j.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
public class CatanApplication {
    private final DatabaseConnectionManager dcm;
    private final FirebaseAuthService firebaseAuthService;
    private final Map<String, Bucket> createGameLimiter = new ConcurrentHashMap<>();

    @Autowired
    public CatanApplication(FirebaseAuthService firebaseAuthService) {
        this.dcm = new DatabaseConnectionManager("db", "catan", "postgres", "password");
        this.firebaseAuthService = firebaseAuthService;
    }

    public static void main(String[] args) {
        SpringApplication.run(CatanApplication.class, args);
    }

    // Rate limiter configuration - 1 request per minute
    private Bucket createBucket() {
        return Bucket4j.builder()
            .addLimit(Bandwidth.simple(1, Duration.ofMinutes(1)))
            .build();
    }

    // req classes for account endpoints
    private static class CheckUsernameRequest {
        @JsonProperty("username")
        private String username;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    private static class CheckEmailRequest {
        @JsonProperty("email")
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    private static class CreateAccountRequest {
        @JsonProperty("username")
        private String username;
        
        @JsonProperty("email")
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    private static class UpdateUsernameRequest {
        @JsonProperty("newUsername")
        private String newUsername;

        public String getNewUsername() { return newUsername; }
        public void setNewUsername(String newUsername) { this.newUsername = newUsername; }
    }

    // req class for creating a game
    private static class CreateGameRequest {
        @JsonProperty("gameName")
        private String gameName;

        public String getGameName() { return gameName; }
        public void setGameName(String gameName) { this.gameName = gameName; }
    }

    // Real-time validation endpoints
    @PostMapping("/api/account/check-username")
    public ResponseEntity<?> checkUsername(@RequestBody CheckUsernameRequest request) {
        try (Connection conn = dcm.getConnection()) {
            AccountDAO accountDAO = new AccountDAO(conn);
            boolean exists = accountDAO.checkUsernameExists(request.getUsername());
            
            if (exists) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }

    @PostMapping("/api/account/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody CheckEmailRequest request) {
        try (Connection conn = dcm.getConnection()) {
            AccountDAO accountDAO = new AccountDAO(conn);
            System.out.println("Checking email: " + request.getEmail());
            boolean exists = accountDAO.checkEmailExists(request.getEmail());
            System.out.println("Email exists: " + exists);
            
            if (exists) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }

    // Create new account endpoint
    @PostMapping("/api/account")
    public ResponseEntity<?> createAccount(
            @RequestBody CreateAccountRequest request,
            @RequestHeader("Authorization") String idToken) {
        try {
            // Verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));
            if (!email.equals(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email mismatch");
            }

            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                
                // Check if username or email exists
                boolean usernameExists = accountDAO.checkUsernameExists(request.getUsername());
                boolean emailExists = accountDAO.checkEmailExists(request.getEmail());

                if (usernameExists || emailExists) {
                    // Delete Firebase user if DB account creation fails
                    firebaseAuthService.deleteUser(email);
                    return ResponseEntity.badRequest().body("Username or email already exists");
                }

                // Create new account
                Account newAccount = new Account();
                newAccount.setUsername(request.getUsername());
                newAccount.setEmail(request.getEmail());
                newAccount.setTotalGames(0L);
                newAccount.setTotalWins(0L);
                newAccount.setTotalLosses(0L);
                newAccount.setElo(1000L);

                Account created = accountDAO.create(newAccount);
                if (created != null) {
                    return ResponseEntity.ok().body(created.getId());
                } else {
                    // Delete Firebase user if DB account creation fails
                    firebaseAuthService.deleteUser(email);
                    return ResponseEntity.internalServerError().body("Failed to create account");
                }
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }

    // Get account by email for sign-in verification
    @PostMapping("/api/account/by-email")
    public ResponseEntity<?> getAccountByEmail(
            @RequestBody CheckEmailRequest request,
            @RequestHeader("Authorization") String idToken) {
        try {
            // Verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));
            if (!email.equals(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email mismatch");
            }

            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                Account account = accountDAO.findByEmail(request.getEmail());
                
                if (account != null) {
                    return ResponseEntity.ok().body(account);
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }

    // Update username endpoint
    @PatchMapping("/api/account/username")
    public ResponseEntity<?> updateUsername(
            @RequestBody UpdateUsernameRequest request,
            @RequestHeader("Authorization") String idToken) {
        try {
            // Verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));

            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                
                // Check if new username exists
                boolean usernameExists = accountDAO.checkUsernameExists(request.getNewUsername());
                if (usernameExists) {
                    return ResponseEntity.badRequest().body("Username already exists");
                }

                // Update username
                boolean updated = accountDAO.updateUsername(email, request.getNewUsername());
                if (updated) {
                    // Return the updated account
                    Account updatedAccount = accountDAO.findByEmail(email);
                    return ResponseEntity.ok().body(updatedAccount);
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }

    // Delete account endpoint
    @DeleteMapping("/api/account")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String idToken) {
        try {
            // Verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));

            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                
                // Delete from database
                boolean deleted = accountDAO.deleteByEmail(email);
                if (deleted) {
                    // Delete from Firebase
                    firebaseAuthService.deleteUser(email);
                    return ResponseEntity.ok().build();
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }

    // Get all games endpoint
    @GetMapping("/api/games")
    public ResponseEntity<?> getAllGames() {
        try (Connection conn = dcm.getConnection()) {
            GameDAO gameDAO = new GameDAO(conn);
            AccountDAO accountDAO = new AccountDAO(conn);
            
            // First, delete any empty games that have been in progress
            gameDAO.deleteEmptyGames();
            
            // Get all games
            List<Game> games = gameDAO.findAll();
            
            // Enhance games with player information
            for (Game game : games) {
                List<Account> players = new ArrayList<>();
                for (Long playerId : game.getPlayerList()) {
                    Account player = accountDAO.findById(playerId);
                    if (player != null) {
                        players.add(player);
                    }
                }
                game.setPlayers(players);
            }
            
            return ResponseEntity.ok().body(games);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }

    // Create new game endpoint with rate limiting
    @PostMapping("/api/games")
    public ResponseEntity<?> createGame(
            @RequestBody CreateGameRequest request,
            @RequestHeader("Authorization") String idToken) {
        try {
            // Verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));
            
            // Rate limiting
            Bucket bucket = createGameLimiter.computeIfAbsent(email, k -> createBucket());
            if (!bucket.tryConsume(1)) {
                return ResponseEntity.status(429).body("Please wait before creating another game");
            }

            try (Connection conn = dcm.getConnection()) {
                // Get user's account ID
                AccountDAO accountDAO = new AccountDAO(conn);
                Account account = accountDAO.findByEmail(email);
                if (account == null) {
                    return ResponseEntity.badRequest().body("Account not found");
                }

                // Create new game
                GameDAO gameDAO = new GameDAO(conn);
                Game newGame = new Game();
                newGame.setGameName(request.getGameName());
                newGame.setInProgress(false);
                newGame.setIsGameOver(false);
                
                Game created = gameDAO.create(newGame);
                if (created != null) {
                    // Add the creator to the player list
                    created = gameDAO.addPlayer(created.getId(), account.getId());
                    return ResponseEntity.ok().body(created);
                } else {
                    return ResponseEntity.internalServerError().body("Failed to create game");
                }
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }

    // Leave game endpoint
    @DeleteMapping("/api/games/{gameId}/players")
    public ResponseEntity<?> leaveGame(
            @PathVariable long gameId,
            @RequestHeader("Authorization") String idToken) {
        try {
            // Verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));

            try (Connection conn = dcm.getConnection()) {
                // Get user's account ID
                AccountDAO accountDAO = new AccountDAO(conn);
                Account account = accountDAO.findByEmail(email);
                if (account == null) {
                    return ResponseEntity.badRequest().body("Account not found");
                }

                GameDAO gameDAO = new GameDAO(conn);
                Game game = gameDAO.findById(gameId);
                if (game == null) {
                    return ResponseEntity.notFound().build();
                }

                // Remove player from game
                game = gameDAO.removePlayer(gameId, account.getId());
                
                // If game is empty after player leaves, delete it
                if (game.getPlayerList().isEmpty()) {
                    gameDAO.delete(gameId);
                    return ResponseEntity.ok().body("Game deleted");
                }
                
                return ResponseEntity.ok().body(game);
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }

    // Get username endpoint
    @GetMapping("/api/account/username")
    public ResponseEntity<?> getUsername(@RequestHeader("Authorization") String idToken) {
        try {
            // Verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));

            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                Account account = accountDAO.findByEmail(email);
                
                if (account != null) {
                    return ResponseEntity.ok().body(Map.of("username", account.getUsername()));
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }
}
