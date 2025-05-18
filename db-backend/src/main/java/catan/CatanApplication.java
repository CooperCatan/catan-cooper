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
import io.github.bucket4j.*; // rate limiting 
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger; // logging
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections; 
import java.util.stream.Collectors; // caching

@SpringBootApplication
@RestController
public class CatanApplication {
    private static final Logger logger = LoggerFactory.getLogger(CatanApplication.class);
    private final DatabaseConnectionManager dcm;
    private final FirebaseAuthService firebaseAuthService;
    private final Map<String, Bucket> createGameLimiter = new ConcurrentHashMap<>();

    // cache for getAllGames endpoint
    // private List<Game> allGamesCache = null;
    // private long lastAllGamesCacheUpdateTime = 0;
    // private static final long ALL_GAMES_CACHE_DURATION_MS = 5000; // cache for 5 seconds

    @Autowired
    public CatanApplication(FirebaseAuthService firebaseAuthService) {
        this.dcm = new DatabaseConnectionManager("db", "catan", "postgres", "password");
        this.firebaseAuthService = firebaseAuthService;
    }

    public static void main(String[] args) {
        SpringApplication.run(CatanApplication.class, args);
    }

    // rate limiter configuration - 2 requests per minute, can modify here to be less
    private Bucket createBucket() {
        return Bucket4j.builder()
            .addLimit(Bandwidth.simple(2, Duration.ofMinutes(1)))
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

    // req DTO for setup actions
    private static class SetupActionRequest {
        @JsonProperty("accountId")
        private long accountId;

        @JsonProperty("actionType") // "SETTLEMENT" or "ROAD"
        private String actionType;

        @JsonProperty("vertexId") // for settlement need vertex
        private Integer vertexId;

        @JsonProperty("edgeId") // for road 
        private String edgeId; 

        @JsonProperty("v1") // for road (vertex 1 of the edge)
        private Integer v1;
        
        @JsonProperty("v2") // for road (vertex 2 of the edge)
        private Integer v2;

        @JsonProperty("isSecondRoundPlacement") // to determine if resources should be granted for settlement during setup phase
        private boolean isSecondRoundPlacement;

        // getters and setters
        public long getAccountId() { return accountId; }
        public void setAccountId(long accountId) { this.accountId = accountId; }

        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }

        public Integer getVertexId() { return vertexId; }
        public void setVertexId(Integer vertexId) { this.vertexId = vertexId; }

        public String getEdgeId() { return edgeId; } 
        public void setEdgeId(String edgeId) { this.edgeId = edgeId; }
        
        public Integer getV1() { return v1; }
        public void setV1(Integer v1) { this.v1 = v1; }

        public Integer getV2() { return v2; }
        public void setV2(Integer v2) { this.v2 = v2; }

        public boolean isSecondRoundPlacement() { return isSecondRoundPlacement; }
        public void setSecondRoundPlacement(boolean secondRoundPlacement) { isSecondRoundPlacement = secondRoundPlacement; }
    }

    // real-time validation endpoints for username checking on sign on
    @PostMapping("/api/account/check-username")
    public ResponseEntity<?> checkUsername(@RequestBody CheckUsernameRequest request) {
        logger.info("[API_CALL] POST /api/account/check-username");
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

    // real-time validation endpoints for email checking on sign on
    @PostMapping("/api/account/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody CheckEmailRequest request) {
        logger.info("[API_CALL] POST /api/account/check-email");
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

    // create new account endpoint
    @PostMapping("/api/account")
    public ResponseEntity<?> createAccount(
            @RequestBody CreateAccountRequest request,
            @RequestHeader("Authorization") String idToken) {
        logger.info("[API_CALL] POST /api/account");
        try {
            // verify firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));
            if (!email.equals(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email mismatch");
            }

            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                
                // check if username or email exists inside of firebase
                boolean usernameExists = accountDAO.checkUsernameExists(request.getUsername());
                boolean emailExists = accountDAO.checkEmailExists(request.getEmail());

                if (usernameExists || emailExists) {
                    // delete firebase user if db account creation fails    
                    firebaseAuthService.deleteUser(email);
                    return ResponseEntity.badRequest().body("Username or email already exists");
                }

                // create new account on backend        
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
                    // delete Firebase user if DB account creation fails
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

    // get account by email for sign-in verification
    @PostMapping("/api/account/by-email")
    public ResponseEntity<?> getAccountByEmail(
            @RequestBody CheckEmailRequest request,
            @RequestHeader("Authorization") String idToken) {
        logger.info("[API_CALL] POST /api/account/by-email");
        try {
            // verify firebase token
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

    // update username endpoint
    @PatchMapping("/api/account/username")
    public ResponseEntity<?> updateUsername(
            @RequestBody UpdateUsernameRequest request,
            @RequestHeader("Authorization") String idToken) {
        logger.info("[API_CALL] PATCH /api/account/username");
        try {
            // verify firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));

            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                
                // check if new username exists
                boolean usernameExists = accountDAO.checkUsernameExists(request.getNewUsername());
                if (usernameExists) {
                    return ResponseEntity.badRequest().body("Username already exists");
                }

                // update username
                boolean updated = accountDAO.updateUsername(email, request.getNewUsername());
                if (updated) {
                    // return the updated account to update page
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

    // delete account endpoint
    @DeleteMapping("/api/account")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String idToken) {
        logger.info("[API_CALL] DELETE /api/account");
        try {
            // verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));

            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                
                // delete from database
                boolean deleted = accountDAO.deleteByEmail(email);
                if (deleted) {
                    // delete from Firebase
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

    // get all games endpoint for displaying on GameLobby page
    @GetMapping("/api/games")
    public ResponseEntity<?> getAllGames() {
        logger.info("[API_CALL] GET /api/games");
        try (Connection conn = dcm.getConnection()) {
            GameDAO gameDAO = new GameDAO(conn);
            AccountDAO accountDAO = new AccountDAO(conn);
            
            // delete empty games in progress 
            gameDAO.deleteEmptyGames();
            
            // Directly fetch all games from the DAO
            List<Game> games = gameDAO.findAll();
            logger.info("[GET_ALL_GAMES] Fetched {} games from DB.", games.size());
            
            // Enhance games with player information for display on UI
            for (Game game : games) {
                List<Account> players = new ArrayList<>();
                if (game.getPlayerList() != null) {
                    for (Long playerId : game.getPlayerList()) {
                        Account player = accountDAO.findById(playerId);
                        if (player != null) {
                            players.add(player);
                        }
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

    // create new game endpoint with rate limiting
    @PostMapping("/api/games")
    public ResponseEntity<?> createGame(
            @RequestBody CreateGameRequest request,
            @RequestHeader("Authorization") String idToken) {
        logger.info("[API_CALL] POST /api/games");
        try {
            System.out.println("[DEBUG] Starting game creation process");
            // verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));
            System.out.println("[DEBUG] Firebase token verified for email: " + email);
            
            // rate limiting
            Bucket bucket = createGameLimiter.computeIfAbsent(email, k -> createBucket());
            if (!bucket.tryConsume(1)) {
                return ResponseEntity.status(429).body("Please wait before creating another game (Maximum: 3 games per minute)");
            }
            try (Connection conn = dcm.getConnection()) {
                
                // get users acct
                AccountDAO accountDAO = new AccountDAO(conn);
                Account account = accountDAO.findByEmail(email);
                if (account == null) {
                    return ResponseEntity.badRequest().body("Account not found");
                }

                // create new game 
                GameDAO gameDAO = new GameDAO(conn);
                Game newGame = new Game();
                newGame.setGameName(request.getGameName());
                newGame.setInProgress(false);
                newGame.setIsGameOver(false);
                
                // initialize all the required fields with hexes json, vertices json, and edges json
                newGame.setJsonHexes("[]");
                newGame.setJsonVertices("[]");
                newGame.setJsonEdges("[]");
                newGame.setJsonPlayers("[]");
                newGame.setBankBrick(19);
                newGame.setBankOre(19);
                newGame.setBankSheep(19);
                newGame.setBankWheat(19);
                newGame.setBankWood(19);
                newGame.setBankYearOfPlenty(2);
                newGame.setBankMonopoly(2);
                newGame.setBankRoadBuilding(2);
                newGame.setBankVictoryPoint(5);
                newGame.setBankKnight(14);
                
                // create the game in the database first
                Game created = gameDAO.create(newGame);
                if (created == null) {
                    return ResponseEntity.internalServerError().body("Failed to create game");
                }
                
                //  add the creator to the player list
                created = gameDAO.addPlayer(created.getId(), account.getId());
                if (created == null) {
                    return ResponseEntity.internalServerError().body("Failed to add player to game");
                }
                
                // get  full game data with player information
                List<Account> players = new ArrayList<>();
                for (Long playerId : created.getPlayerList()) {
                    Account player = accountDAO.findById(playerId);
                    if (player != null) {
                        players.add(player);
                    }
                }
                created.setPlayers(players);
                
                return ResponseEntity.ok().body(created);
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Unexpected error occurred");
        }
    }

    // join game endpoint
    @PostMapping("/api/games/{gameId}/players")
    public ResponseEntity<?> joinGame(
            @PathVariable long gameId,
            @RequestHeader("Authorization") String idToken) {
        logger.info("[API_CALL] POST /api/games/{}/players", gameId);
        try {
            // verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));

            try (Connection conn = dcm.getConnection()) {
                // get user's account ID via email
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

                // check if game is joinable
                if (game.isGameOver() || game.isInProgress() || game.getPlayerList().size() >= 4) {
                    return ResponseEntity.badRequest().body("Game cannot be joined");
                }

                // check if player is already in the game
                if (game.getPlayerList().contains(account.getId())) {
                    return ResponseEntity.badRequest().body("Already in game");
                }

                // add player to game
                game = gameDAO.addPlayer(gameId, account.getId());
                if (game == null) {
                    return ResponseEntity.internalServerError().body("Failed to join game");
                }

                // get the full game data with player information
                List<Account> players = new ArrayList<>();
                for (Long playerId : game.getPlayerList()) {
                    Account player = accountDAO.findById(playerId);
                    if (player != null) {
                        players.add(player);
                    }
                }
                game.setPlayers(players);

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

    // leave game endpoint
    @DeleteMapping("/api/games/{gameId}/players")
    public ResponseEntity<?> leaveGame(
            @PathVariable long gameId,
            @RequestHeader("Authorization") String idToken) {
        logger.info("[API_CALL] DELETE /api/games/{}/players", gameId);
        try {
            // verify Firebase token
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));

            try (Connection conn = dcm.getConnection()) {
                // get user's account ID
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

                // remove player from game
                game = gameDAO.removePlayer(gameId, account.getId());
                
                // if game is empty after player leaves, delete it
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

    // get username endpoint
    @GetMapping("/api/account/username")
    public ResponseEntity<?> getUsername(@RequestHeader("Authorization") String idToken) {
        logger.info("[API_CALL] GET /api/account/username");
        try {
            // verify Firebase token
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

    // get all accounts endpoint for leaderboard display
    @GetMapping("/api/accounts")
    public ResponseEntity<?> getAllAccounts(@RequestHeader("Authorization") String idToken) {
        logger.info("[API_CALL] GET /api/accounts");
        try {
            // verify Firebase token
            firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));

            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                List<Account> accounts = accountDAO.findAll();
                return ResponseEntity.ok().body(accounts);
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error");
        }
    }
    // start game endpoint
    @PostMapping("/api/games/{gameId}/start")
    public ResponseEntity<?> startGame(
            @PathVariable long gameId,
            @RequestHeader("Authorization") String idToken) {
        logger.info("[API_CALL] POST /api/games/{}/start", gameId);
        logger.info("[START_GAME] Received request to start game ID: {}", gameId);
        try {
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));
            logger.info("[START_GAME] Token verified for email: {}", email);

            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                Account account = accountDAO.findByEmail(email);
                if (account == null) {
                    logger.warn("[START_GAME] Account not found for email: {}", email);
                    return ResponseEntity.badRequest().body("Account not found");
                }
                logger.info("[START_GAME] Account ID: {} found for email: {}", account.getId(), email);

                GameDAO gameDAO = new GameDAO(conn);
                Game game = gameDAO.findById(gameId);

                if (game == null) {
                    logger.warn("[START_GAME] Game not found with ID: {}", gameId);
                    return ResponseEntity.notFound().build();
                }
                logger.info("[START_GAME] Game found: {}. Current inProgress: {}", game.getGameName(), game.isInProgress());

                if (game.isInProgress()) {
                    logger.warn("[START_GAME] Game ID: {} is already in progress.", gameId);
                    return ResponseEntity.badRequest().body("Game already in progress");
                }

                if (game.getPlayerList() == null || game.getPlayerList().isEmpty()) {
                    logger.warn("[START_GAME] Game ID: {} has no players.", gameId);
                    return ResponseEntity.badRequest().body("Game has no players");
                }

                if (!game.getPlayerList().get(0).equals(account.getId())) {
                    logger.warn("[START_GAME] User {} is not the host of game ID: {}. Host is: {}", account.getId(), gameId, game.getPlayerList().get(0));
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the host can start the game");
                }
                logger.info("[START_GAME] User {} is confirmed as host for game ID: {}", account.getId(), gameId);

                logger.info("[START_GAME] Initializing GameEngine for game ID: {}", gameId);
                GameEngine engine = new GameEngine(gameId); // gameEngine constructor loads/generates board
                logger.info("[START_GAME] GameEngine initialized for game ID: {}", gameId);
                
                game.setJsonHexes(engine.getJsonHexes());
                game.setJsonVertices(engine.getJsonVertices());
                game.setJsonEdges(engine.getJsonEdges());
                game.setJsonPlayers(engine.getJsonPlayers()); 
                game.setInProgress(true);
                game.setRobberLocation(engine.getRobberLocation()); 
                game.setSetupPhaseComplete(false); // init game as currently in setup phase

                game.setSettlementsPlacedInSetupCount(new HashMap<>());
                game.setRoadsPlacedInSetupCount(new HashMap<>());

                List<Long> playerIds = game.getPlayerList();
                List<Long> setupOrder = new ArrayList<>();

                // Round 1: P1, P2, ..., PN place their first settlement
                // Round 2: P1, P2, ..., PN place their first road
                // Round 3: PN, ..., P2, P1 place their second settlement (collect resources)
                // Round 4: PN, ..., P2, P1 place their second road

                // For a single player (P1): S1, R1, S2 (resources), R2
                // For two players (P1, P2): P1(S1), P2(S1), P1(R1), P2(R1), P2(S2), P1(S2), P2(R2), P1(R2)

                // Add players for first settlement pass (forward)
                setupOrder.addAll(playerIds);
                // Add players for first road pass (forward)
                setupOrder.addAll(playerIds);

                // Prepare for reverse order passes
                List<Long> reversePlayerIds = new ArrayList<>(playerIds);
                Collections.reverse(reversePlayerIds);

                // Add players for second settlement pass (reverse) - these get resources
                setupOrder.addAll(reversePlayerIds);
                // Add players for second road pass (reverse)
                setupOrder.addAll(reversePlayerIds);
                
                game.setSetupPlacementOrder(setupOrder);
                game.setCurrentSetupPlacementIndex(0);

                if (!setupOrder.isEmpty()) {
                    game.setCurrentTurnPlayerId(setupOrder.get(0));
                    logger.info("[START_GAME] Setup order generated. First turn in setup: PlayerID {}", setupOrder.get(0));
                } else {
                    logger.warn("[START_GAME] Setup order is empty, cannot set initial turn player for setup.");
                    // this case should ideally not happen if there are players in the game.
                }
                // setup phase over

                logger.info("[START_GAME] Persisting updated game state to DB for game ID: {}", gameId);
                gameDAO.updateGameSetupState(game); 
                logger.info("[START_GAME] Game state persisted for game ID: {}", gameId);
                
                Game updatedGame = gameDAO.findById(gameId);
                 if (updatedGame.getPlayers() == null || updatedGame.getPlayers().isEmpty()) {
                    List<Account> playersInGame = new ArrayList<>();
                    if (updatedGame.getPlayerList() != null) {
                        for (Long playerId : updatedGame.getPlayerList()) {
                            Account playerAccount = accountDAO.findById(playerId);
                            if (playerAccount != null) {
                                playersInGame.add(playerAccount);
                            }
                        }
                    }
                    updatedGame.setPlayers(playersInGame);
                }


                Map<String, Object> response = new HashMap<>();
                response.put("game", updatedGame); // send the updated game DTO
                
                List<Edge> edgesToSend = engine.getEdges();
                if (edgesToSend != null && !edgesToSend.isEmpty()) {
                    logger.debug("[START_GAME_DEBUG] Edges being prepared for response. Count: {}. First few:", edgesToSend.size());
                    for (int i = 0; i < Math.min(5, edgesToSend.size()); i++) {
                         Edge edge = edgesToSend.get(i);
                         logger.debug("[START_GAME_DEBUG]   - Edge ID: {}, ConnectedVertices: {}", 
                                      edge.getId(), 
                                      (edge.getConnectedVertices() != null ? edge.getConnectedVertices().toString() : "null"));
                    }
                    try {
                        String edgesJsonString = new ObjectMapper().writeValueAsString(edgesToSend);
                        logger.debug("[START_GAME_DEBUG] JSON string for edges in response (truncated): {}", 
                                     edgesJsonString.substring(0, Math.min(500, edgesJsonString.length())) + (edgesJsonString.length() > 500 ? "..." : ""));
                    } catch (JsonProcessingException e) {
                         logger.warn("[START_GAME_DEBUG] Could not serialize edges to JSON for logging: {}", e.getMessage());
                    }
                } else {
                    logger.warn("[START_GAME_DEBUG] edges list from GameEngine is null or empty before sending response.");
                }

                List<Vertex> verticesToSend = engine.getVertices();
                if (verticesToSend != null && !verticesToSend.isEmpty()) {
                    logger.debug("[START_GAME_DEBUG] Vertices being prepared for response. Count: {}. First few:", verticesToSend.size());
                    for (int i = 0; i < Math.min(5, verticesToSend.size()); i++) {
                         Vertex vertex = verticesToSend.get(i);
                         logger.debug("[START_GAME_DEBUG]   - Vertex ID: {}, Owner: {}, Type: {}", 
                                      vertex.getId(), vertex.getOwnerId(), vertex.getBuildingType());
                    }
                    if(verticesToSend.size() > 0) {
                        Vertex lastVertex = verticesToSend.get(verticesToSend.size() - 1);
                         logger.debug("[START_GAME_DEBUG]   - Last Vertex ID: {}, Owner: {}, Type: {}", 
                                      lastVertex.getId(), lastVertex.getOwnerId(), lastVertex.getBuildingType());
                    }
                    try {
                        String verticesJsonString = new ObjectMapper().writeValueAsString(verticesToSend);
                        logger.debug("[START_GAME_DEBUG] JSON string for vertices in response (truncated): {}", 
                                     verticesJsonString.substring(0, Math.min(500, verticesJsonString.length())) + (verticesJsonString.length() > 500 ? "..." : ""));
                    } catch (JsonProcessingException e) {
                         logger.warn("[START_GAME_DEBUG] Could not serialize vertices to JSON for logging: {}", e.getMessage());
                    }
                } else {
                    logger.warn("[START_GAME_DEBUG] vertices list from GameEngine is null or empty before sending response.");
                }

                response.put("boardState", Map.of(
                        "hexes", engine.getHexes(),
                        "vertices", verticesToSend, 
                        "edges", edgesToSend, 
                        "players", engine.getPlayers() 
                ));
                logger.info("[START_GAME] Successfully started game ID: {} and sending response.", gameId);
                return ResponseEntity.ok().body(response);

            } catch (SQLException e) {
                logger.error("[START_GAME] SQL Exception for game ID: {}: {}", gameId, e.getMessage(), e);
                return ResponseEntity.internalServerError().body("Database error during game start");
            } catch (RuntimeException e) { 
                 logger.error("[START_GAME] Runtime Exception for game ID: {}: {}", gameId, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error initializing game engine: " + e.getMessage());
            }
        } catch (FirebaseAuthException e) {
            logger.error("[START_GAME] Firebase Auth Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (Exception e) { 
            logger.error("[START_GAME] Unexpected generic exception for game ID: {}: {}", gameId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }

        
    }

    @GetMapping("/api/games/{gameId}")
    public ResponseEntity<?> getGameById(
            @PathVariable long gameId,
            @RequestHeader(value = "Authorization", required = false) String idToken) {
        logger.info("[API_CALL] GET /api/games/{}", gameId);
        try {
            try (Connection conn = dcm.getConnection()) {
                GameDAO gameDAO = new GameDAO(conn);
                Game game = gameDAO.findById(gameId);

                if (game == null) {
                    return ResponseEntity.notFound().build();
                }

                AccountDAO accountDAO = new AccountDAO(conn);
                List<Account> playersInGame = new ArrayList<>();
                if (game.getPlayerList() != null) {
                    for (Long playerId : game.getPlayerList()) {
                        Account playerAccount = accountDAO.findById(playerId);
                        if (playerAccount != null) {
                            playersInGame.add(playerAccount);
                        } else {
                        }
                    }
                }
                game.setPlayers(playersInGame); // set the transient list of Account objects

                
                if (game.isInProgress() && game.getJsonHexes() != null && !game.getJsonHexes().equals("[]")) {
                    GameEngine engine;
                    try {
                        engine = new GameEngine(gameId); 
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.ok().body(Map.of("game", game, "error", "Could not load board state"));
                    }
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("game", game); // game DTO (name, flags, bank, List<Account> for players)
                    response.put("boardState", Map.of(
                        "hexes", engine.getHexes(),         // list<Hex>
                        "vertices", engine.getVertices(),   // list<Vertex>
                        "edges", engine.getEdges(),         // list<Edge>
                        "players", engine.getPlayers()      // Map<Long, Player (in-game state)>
                    ));
                    response.put("robberLocation", engine.getRobberLocation());
                    return ResponseEntity.ok().body(response);
                } else {
                    return ResponseEntity.ok().body(Map.of("game", game)); // only basic game data (lobby view)
                }

        } catch (SQLException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("Database error: " + e.getMessage());
            } 
        } catch (Exception e) { 
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Unexpected server error: " + e.getMessage());
        }
    }

    @PostMapping("/api/games/{gameId}/setup-action")
    public ResponseEntity<?> performSetupAction(
            @PathVariable long gameId,
            @RequestHeader("Authorization") String idToken,
            @RequestBody SetupActionRequest request) {
        logger.info("[API_CALL] POST /api/games/{}/setup-action", gameId);
        logger.info("[SETUP_ACTION] Received for game ID: {}, action: {}, accountId: {}", gameId, request.getActionType(), request.getAccountId());
        try {
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));
            Account currentAccount;
            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                currentAccount = accountDAO.findByEmail(email);
                if (currentAccount == null || currentAccount.getId() != request.getAccountId()) {
                    logger.warn("[SETUP_ACTION] Account ID mismatch or invalid token. Token email: {}, Request accountId: {}", email, request.getAccountId());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account ID mismatch or invalid token");
                }
            }
            logger.info("[SETUP_ACTION] Account {} verified for action.", request.getAccountId());

            GameEngine engine = new GameEngine(gameId); // loads current game state
            boolean success = false;
            Integer locationId = null;

            if ("SETTLEMENT".equalsIgnoreCase(request.getActionType())) {
                if (request.getVertexId() == null) {
                    logger.warn("[SETUP_ACTION] VertexId is null for SETTLEMENT action.");
                    return ResponseEntity.badRequest().body("Vertex ID is required for settlement placement.");
                }
                locationId = request.getVertexId();
                logger.info("[SETUP_ACTION] Calling engine.performSetupAction for SETTLEMENT at vertex {} by account {}.", 
                            locationId, request.getAccountId());
                success = engine.performSetupAction(request.getAccountId(), request.getActionType(), locationId, request.isSecondRoundPlacement());
            } else if ("ROAD".equalsIgnoreCase(request.getActionType())) {
                if (request.getEdgeId() == null) {
                     logger.warn("[SETUP_ACTION] EdgeId is null for ROAD action.");
                    return ResponseEntity.badRequest().body("Edge ID is required for road placement.");
                }
                try {
                    locationId = Integer.parseInt(request.getEdgeId());
                } catch (NumberFormatException e) {
                    logger.warn("[SETUP_ACTION] Invalid EdgeId format: {}. Must be an integer.", request.getEdgeId());
                    return ResponseEntity.badRequest().body("Invalid Edge ID format. Must be an integer.");
                }
                logger.info("[SETUP_ACTION] Calling engine.performSetupAction for ROAD on edge {} by account {}.", 
                            locationId, request.getAccountId());
                success = engine.performSetupAction(request.getAccountId(), request.getActionType(), locationId, false); // isSecondRoundPlacement is not relevant for roads
            } else {
                logger.warn("[SETUP_ACTION] Unknown action type: {}", request.getActionType());
                return ResponseEntity.badRequest().body("Unknown action type");
            }

            if (success) {
                logger.info("[SETUP_ACTION] Action successful for game {}. Persisting and returning updated state.", gameId);
                Game updatedGame; // fetch the fully updated game state after engine.performSetupAction persisted it
                try (Connection conn = dcm.getConnection()) {
                    GameDAO gameDAO = new GameDAO(conn);
                    updatedGame = gameDAO.findById(gameId);
                    if (updatedGame.getPlayers() == null || updatedGame.getPlayers().isEmpty()) { 
                        AccountDAO accountDAO = new AccountDAO(conn); // re-fetch accounts for the player list
                        List<Account> playersInGame = new ArrayList<>();
                        if (updatedGame.getPlayerList() != null) {
                            for (Long playerId : updatedGame.getPlayerList()) {
                                Account playerAccount = accountDAO.findById(playerId);
                                if (playerAccount != null) {
                                    playersInGame.add(playerAccount);
                                }
                            }
                        }
                        updatedGame.setPlayers(playersInGame);
                    }
                }

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("game", updatedGame); // this game DTO now includes up-to-date setup phase fields
                responseMap.put("boardState", Map.of(
                        "hexes", engine.getHexes(),
                        "vertices", engine.getVertices(), 
                        "edges", engine.getEdges(),     
                        "players", engine.getPlayers()  
                ));
                responseMap.put("currentTurnPlayerId", engine.getCurrentTurnPlayerId());
                responseMap.put("isSetupPhaseComplete", engine.isSetupPhaseComplete());

                return ResponseEntity.ok().body(responseMap);
            } else {
                logger.warn("[SETUP_ACTION] Action failed for game {}. Engine returned false.", gameId);
                return ResponseEntity.badRequest().body("Action failed (e.g., invalid placement, not your turn, or setup rules violation)");
            }

        } catch (FirebaseAuthException e) {
            logger.error("[SETUP_ACTION] Firebase Auth error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            logger.error("[SETUP_ACTION] SQL error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error");
        } catch (RuntimeException e) {
            logger.error("[SETUP_ACTION] Runtime error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Game logic error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("[SETUP_ACTION] Unexpected generic error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    // DTO for dice roll request - only needs accountId for turn verification
    private static class DiceRollRequest {
        @JsonProperty("accountId")
        private long accountId;

        public long getAccountId() { return accountId; }
        public void setAccountId(long accountId) { this.accountId = accountId; }
    }

    // DTO for dice roll response
    private static class DiceRollResponse {
        public int dice1;
        public int dice2;
        public int sum;

        public DiceRollResponse(int d1, int d2, int s) {
            this.dice1 = d1;
            this.dice2 = d2;
            this.sum = s;
        }
        // getters are needed for json serialization if fields are private
        public int getDice1() { return dice1; }
        public int getDice2() { return dice2; }
        public int getSum() { return sum; }
    }

    @PostMapping("/api/games/{gameId}/roll-dice")
    public ResponseEntity<?> rollDice(
            @PathVariable long gameId,
            @RequestHeader("Authorization") String idToken,
            @RequestBody DiceRollRequest request) {
        logger.info("[API_CALL] POST /api/games/{}/roll-dice", gameId);
        logger.info("[ROLL_DICE] Received for game ID: {}, accountId: {}", gameId, request.getAccountId());
        try {
            String email = firebaseAuthService.verifyToken(idToken.replace("Bearer ", ""));
            Account currentAccount;
            Game gameForTurnCheck;
            try (Connection conn = dcm.getConnection()) {
                AccountDAO accountDAO = new AccountDAO(conn);
                currentAccount = accountDAO.findByEmail(email);
                if (currentAccount == null || currentAccount.getId() != request.getAccountId()) {
                    logger.warn("[ROLL_DICE] Account ID mismatch or invalid token. Token email: {}, Request accountId: {}", email, request.getAccountId());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account ID mismatch or invalid token");
                }

                GameDAO gameDAO = new GameDAO(conn); // new connection for this DAO
                gameForTurnCheck = gameDAO.findById(gameId);
                if (gameForTurnCheck == null) {
                    logger.warn("[ROLL_DICE] Game not found: {}", gameId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
                }
                if (gameForTurnCheck.getPlayerList().size() > 1 && !gameForTurnCheck.getPlayerList().get(0).equals(request.getAccountId())) {
                }

            } // connection for account/game check closes here
            
            logger.info("[ROLL_DICE] Account {} verified for dice roll in game {}.", request.getAccountId(), gameId);

            GameEngine engine = new GameEngine(gameId); // loads current game state
            GameEngine.DiceRollResult diceResult = engine.rollDiceAndDistributeResources(request.getAccountId());

            // fetch the updated game state to return
            Game updatedGame;
            try (Connection conn = dcm.getConnection()) {
                GameDAO gameDAO = new GameDAO(conn);
                updatedGame = gameDAO.findById(gameId);
                 if (updatedGame.getPlayers() == null || updatedGame.getPlayers().isEmpty()) {
                    AccountDAO accountDAO = new AccountDAO(conn);
                    List<Account> playersInGame = new ArrayList<>();
                    if (updatedGame.getPlayerList() != null) {
                        for (Long playerId : updatedGame.getPlayerList()) {
                            Account playerAccount = accountDAO.findById(playerId);
                            if (playerAccount != null) {
                                playersInGame.add(playerAccount);
                            }
                        }
                    }
                    updatedGame.setPlayers(playersInGame);
                }
            } // connection for fetching updated game closes here

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("game", updatedGame); // contains currentDiceRoll updated by engine
            responseMap.put("boardState", Map.of(
                    "hexes", engine.getHexes(),
                    "vertices", engine.getVertices(),
                    "edges", engine.getEdges(),
                    "players", engine.getPlayers() // player resources updated
            ));
            responseMap.put("diceRoll", new DiceRollResponse(diceResult.getDice1(), diceResult.getDice2(), diceResult.getSum()));

            logger.info("[ROLL_DICE] Dice roll processed for game {}. Player {} rolled {} + {} = {}. Returning updated state.", 
                        gameId, request.getAccountId(), diceResult.getDice1(), diceResult.getDice2(), diceResult.getSum());
            return ResponseEntity.ok().body(responseMap);

        } catch (FirebaseAuthException e) {
            logger.error("[ROLL_DICE] Firebase Auth error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (SQLException e) {
            logger.error("[ROLL_DICE] SQL error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error");
        } catch (RuntimeException e) {
            logger.error("[ROLL_DICE] Runtime error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Game logic error: " + e.getMessage()); 
        } catch (Exception e) {
            logger.error("[ROLL_DICE] Unexpected generic error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}
