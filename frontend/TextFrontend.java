package catan;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TextFrontend {

    private static final String API_BASE_URL = "http://localhost:8080/api";
    private static String firebaseIdToken = null; // Store Firebase ID Token
    private static Long currentGameId = null;
    private static Long currentAccountId = null; // Still useful for focusing commands

    private static final HttpClient client = HttpClient.newBuilder()
                                                    .version(HttpClient.Version.HTTP_1_1)
                                                    .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=======================================");
        System.out.println(" Catan Text Interface (Firebase Auth)");
        System.out.println("=======================================");
        System.out.println("Interact with the backend API.");
        System.out.println("NOTE: Login must be performed externally to get a Firebase ID Token.");
        System.out.println("Use 'set_token <token>' to provide the token to this client.");
        System.out.println("Type 'help' for available commands, 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("exit")) {
                break;
            }
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                processCommand(line);
            } catch (Exception e) {
                System.err.println("[Error] Failed to process command: " + e.getMessage());
            }
        }
        scanner.close();
        System.out.println("Goodbye!");
    }

    private static void processCommand(String line) throws Exception {
        String[] parts = line.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = (parts.length > 1) ? parts[1] : "";

        switch (command) {
            case "help":
                printHelp();
                break;
            case "set_token":
                handleSetToken(args);
                break;
            case "register": 
                handleRegister(args);
                break;
            case "get_account":
                handleGetAccount(args);
                break;
            case "create_game":
                 if (checkAuth()) handleCreateGame();
                 break;
            case "delete_game":
                 if (checkAuth()) handleDeleteGame(args);
                 break;
            case "add_player":
                 if (checkAuth()) handleAddPlayer(args);
                 break;
            case "gain":
                 if (checkAuth()) handleGainResources(args);
                 break;
            case "robber_loss":
                 if (checkAuth()) handleRobberLoss(args);
                 break;
            case "set_focus":
                handleSetFocus(args);
                break;
            case "build":
                 if (checkAuth() && checkGameFocus()) handleBuildAction(args);
                 break;
            case "buy":
                 if (checkAuth() && checkGameFocus()) handleBuyAction(args);
                 break;
            case "use":
                 if (checkAuth() && checkGameFocus()) handleUseAction(args);
                 break;
            case "end_turn":
                 if (checkAuth() && checkGameFocus()) handleEndTurnAction();
                 break;
            default:
                System.out.println("Unknown command: '" + command + "'. Type 'help' for options.");
        }
    }

    private static void printHelp() {
        System.out.println("\nAvailable Commands:");
        System.out.println("  help                            - Show this help message");
        System.out.println("  set_token <firebase_id_token>   - Set the Firebase ID token obtained externally");
        System.out.println("  register <username>             - Create a new account (Backend needs update)");
        System.out.println("  get_account <id>                - Get account details by ID (Requires token)");
        System.out.println("  create_game                     - Create a new game instance (Requires token)");
        System.out.println("  delete_game <gameId>            - Delete a game instance (Requires token)");
        System.out.println("  add_player <gameId> <accountId> - Adds player to game turn 0 (Requires token)");
        System.out.println("  gain <gId> <accId> <res> <set> <city> - Gain resources (Requires token)");
        System.out.println("  robber_loss <gId> <accId> <turn>  - Apply robber loss (Requires token)");
        System.out.println("  set_focus game <id>             - Set the current game ID context");
        System.out.println("  set_focus account <id>          - Set the current account ID context");
        System.out.println("  --- Game Actions (Requires token & focused game ID) ---");
        System.out.println("  build settlement <vertexId>     - Build a settlement");
        System.out.println("  build city <vertexId>           - Build a city");
        System.out.println("  build road <v1Id> <v2Id>        - Build a road");
        System.out.println("  buy card                        - Buy a development card");
        System.out.println("  use <cardType>                  - Use a development card (e.g., use knight)");
        System.out.println("  end_turn                        - End your current turn");
        System.out.println("  exit                            - Quit the application\n");
        System.out.println("Notes:");
        System.out.println(" - Login must happen externally. Use 'set_token' to provide the Firebase ID token.");
        System.out.println(" - Backend MUST be updated to verify Firebase ID tokens for auth to work.");
        System.out.println(" - Register command assumes backend is updated to not require a password.");
    }

     private static boolean checkAuth() {
         if (firebaseIdToken == null || firebaseIdToken.trim().isEmpty()) {
             System.out.println("[Error] No Firebase ID token set. Use 'set_token <token>' after logging in externally.");
             return false;
         }
         return true;
     }

      private static boolean checkGameFocus() {
          if (currentGameId == null) {
              System.out.println("[Error] No game focus set. Use 'set_focus game <id>'.");
              return false;
          }
          return true;
      }

    private static void handleSetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            System.out.println("Usage: set_token <firebase_id_token>");
            firebaseIdToken = null;
        } else {
            firebaseIdToken = token.trim();
            System.out.println("Firebase ID token set.");
        }
    }

    private static void handleSetFocus(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("Usage: set_focus <game|account> <id>");
            return;
        }
        String type = parts[0].toLowerCase();
        try {
            long id = Long.parseLong(parts[1]);
            if ("game".equals(type)) {
                currentGameId = id;
                System.out.println("Game focus set to ID: " + currentGameId);
            } else if ("account".equals(type)) {
                currentAccountId = id;
                System.out.println("Account focus set to ID: " + currentAccountId);
            } else {
                System.out.println("Usage: set_focus <game|account> <id>");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID provided: " + parts[1]);
        }
    }

    private static void handleRegister(String args) throws Exception {
        String username = args.trim();
        if (username.isEmpty()) {
            System.out.println("Usage: register <username>");
            return;
        }

        Map<String, String> payload = Map.of("username", username);
        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = buildRequest("POST", "/account", jsonBody);

        System.out.println("Sending request to POST " + request.uri());
        System.out.println("NOTE: Assumes backend /api/account is updated for Firebase (e.g., no password needed, links to Firebase UID from verified token).");
        sendRequestAndPrintResponse(request);
    }

    private static void handleGetAccount(String args) throws Exception {
        long accountId;
        try {
            accountId = Long.parseLong(args.trim());
        } catch (NumberFormatException e) {
             if (currentAccountId != null) {
                 accountId = currentAccountId;
                 System.out.println("Using focused account ID: " + accountId);
             } else {
                 System.out.println("Usage: get_account <id> (or use 'set_focus account <id>')");
                 return;
             }
        }

        HttpRequest request = buildRequest("GET", "/account/" + accountId, null);

        System.out.println("Sending request to GET " + request.uri());
        sendRequestAndPrintResponse(request);
    }

     private static void handleCreateGame() throws Exception {
         HttpRequest request = buildRequest("POST", "/games", ""); // Empty body often needed for POST with token

         System.out.println("Sending request to POST " + request.uri());
         HttpResponse<String> response = sendRequestAndGetResponse(request);
         printFormattedResponse(response);

         if (response.statusCode() == 200 || response.statusCode() == 201) {
             try {
                 Map<String, Object> responseMap = objectMapper.readValue(response.body(), new TypeReference<>() {});
                 if (responseMap.containsKey("gameId")) {
                     currentGameId = ((Number) responseMap.get("gameId")).longValue();
                     System.out.println("[Info] Game created. Focused game ID set to: " + currentGameId);
                 }
             } catch (Exception e) {
                 System.err.println("[Warning] Could not parse gameId from response: " + e.getMessage());
             }
         }
     }

     private static void handleDeleteGame(String args) throws Exception {
         long gameId;
          try {
            gameId = Long.parseLong(args.trim());
        } catch (NumberFormatException e) {
             if (currentGameId != null) {
                 gameId = currentGameId;
                 System.out.println("Using focused game ID: " + gameId);
             } else {
                 System.out.println("Usage: delete_game <id> (or use 'set_focus game <id>')");
                 return;
             }
        }

         HttpRequest request = buildRequest("DELETE", "/games/" + gameId, null);

         System.out.println("Sending request to DELETE " + request.uri());
         sendRequestAndPrintResponse(request);
         if (currentGameId != null && currentGameId == gameId) {
             currentGameId = null;
             System.out.println("[Info] Focused game ID cleared.");
         }
     }

     private static void handleAddPlayer(String args) throws Exception {
         String[] parts = args.trim().split("\\s+");
         long gameId, accountId;

         try {
             if (parts.length == 2) {
                 gameId = Long.parseLong(parts[0]);
                 accountId = Long.parseLong(parts[1]);
             } else if (parts.length == 0 && currentGameId != null && currentAccountId != null) {
                 gameId = currentGameId;
                 accountId = currentAccountId;
                 System.out.println("Using focused game ID: " + gameId + " and account ID: " + accountId);
             } else {
                 System.out.println("Usage: add_player <gameId> <accountId> (or use 'set_focus game <id>' and 'set_focus account <id>')");
                 return;
             }
         } catch (NumberFormatException e) {
             System.out.println("Invalid ID provided.");
             System.out.println("Usage: add_player <gameId> <accountId>");
             return;
         }

         HttpRequest request = buildRequest("POST", "/games/" + gameId + "/players/" + accountId + "/hand", "{}");

         System.out.println("Sending request to POST " + request.uri());
         sendRequestAndPrintResponse(request);
     }

      private static void handleGainResources(String args) throws Exception {
            String[] parts = args.trim().split("\\s+");
            long gameId, accountId;
            String resourceType;
            int numSettlements, numCities;

            try {
                 if (parts.length == 5) {
                     gameId = Long.parseLong(parts[0]);
                     accountId = Long.parseLong(parts[1]);
                     resourceType = parts[2];
                     numSettlements = Integer.parseInt(parts[3]);
                     numCities = Integer.parseInt(parts[4]);
                 } else if (parts.length == 3 && currentGameId != null && currentAccountId != null) {
                     gameId = currentGameId;
                     accountId = currentAccountId;
                     resourceType = parts[0];
                     numSettlements = Integer.parseInt(parts[1]);
                     numCities = Integer.parseInt(parts[2]);
                     System.out.println("Using focused game ID: " + gameId + " and account ID: " + accountId);
                 } else {
                      System.out.println("Usage: gain <gameId> <accountId> <resource> <settlements> <cities>");
                      System.out.println("   or: gain <resource> <settlements> <cities> (uses focused IDs)");
                      return;
                 }
            } catch (NumberFormatException e) {
                 System.out.println("Invalid number format in arguments.");
                 return;
            } catch (ArrayIndexOutOfBoundsException e) {
                 System.out.println("Missing arguments.");
                  System.out.println("Usage: gain <gameId> <accountId> <resource> <settlements> <cities>");
                 return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("resourceType", resourceType);
            payload.put("numSettlements", numSettlements);
            payload.put("numCities", numCities);
            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = buildRequest("POST", "/games/" + gameId + "/players/" + accountId + "/gain", jsonBody);

            System.out.println("Sending request to POST " + request.uri());
            sendRequestAndPrintResponse(request);
      }

       private static void handleRobberLoss(String args) throws Exception {
           String[] parts = args.trim().split("\\s+");
           long gameId, accountId, turnNumber;

           try {
                if (parts.length == 3) {
                    gameId = Long.parseLong(parts[0]);
                    accountId = Long.parseLong(parts[1]);
                    turnNumber = Long.parseLong(parts[2]);
                } else if (parts.length == 1 && currentGameId != null && currentAccountId != null) {
                     gameId = currentGameId;
                     accountId = currentAccountId;
                     turnNumber = Long.parseLong(parts[0]);
                     System.out.println("Using focused game ID: " + gameId + " and account ID: " + accountId);
                } else {
                     System.out.println("Usage: robber_loss <gameId> <accountId> <turnNumber>");
                     System.out.println("   or: robber_loss <turnNumber> (uses focused IDs)");
                     return;
                }
           } catch (NumberFormatException e) {
                System.out.println("Invalid number format in arguments.");
                return;
           } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Missing arguments.");
                System.out.println("Usage: robber_loss <gameId> <accountId> <turnNumber>");
                return;
           }

           Map<String, Object> payload = Map.of("turnNumber", turnNumber);
           String jsonBody = objectMapper.writeValueAsString(payload);

           HttpRequest request = buildRequest("POST", "/games/" + gameId + "/players/" + accountId + "/robber", jsonBody);

           System.out.println("Sending request to POST " + request.uri());
           sendRequestAndPrintResponse(request);
       }

     private static void handleBuildAction(String args) throws Exception {
         String[] parts = args.trim().split("\\s+");
         if (parts.length < 2) {
             System.out.println("Usage: build <settlement|city|road> [parameters...]");
             return;
         }
         String buildType = parts[0].toLowerCase();
         String actionType = "";
         Map<String, Object> details = new HashMap<>();

         try {
             switch (buildType) {
                 case "settlement":
                     if (parts.length != 2) { System.out.println("Usage: build settlement <vertexId>"); return; }
                     actionType = "SETTLEMENT";
                     details.put("vertex", Integer.parseInt(parts[1]));
                     break;
                 case "city":
                      if (parts.length != 2) { System.out.println("Usage: build city <vertexId>"); return; }
                     actionType = "CITY";
                     details.put("vertex", Integer.parseInt(parts[1]));
                     break;
                 case "road":
                      if (parts.length != 3) { System.out.println("Usage: build road <vertex1Id> <vertex2Id>"); return; }
                     actionType = "ROAD";
                     details.put("vertex1", Integer.parseInt(parts[1]));
                     details.put("vertex2", Integer.parseInt(parts[2]));
                     break;
                 default:
                     System.out.println("Unknown build type: " + buildType);
                     return;
             }
         } catch (NumberFormatException e) {
             System.out.println("Invalid vertex ID provided.");
             return;
         }

         sendActionRequest(actionType, details);
     }

      private static void handleBuyAction(String args) throws Exception {
           if (!args.equalsIgnoreCase("card")) {
               System.out.println("Usage: buy card");
               return;
           }
           sendActionRequest("PURCHASE", Map.of());
      }

      private static void handleUseAction(String args) throws Exception {
           String cardType = args.trim().toUpperCase();
            if (cardType.isEmpty()) {
                System.out.println("Usage: use <cardType> (e.g., use knight)");
                return;
            }
            Map<String, Object> details = Map.of("cardType", cardType);
            sendActionRequest("USE", details);
      }

      private static void handleEndTurnAction() throws Exception {
            sendActionRequest("END", Map.of());
      }

      private static void sendActionRequest(String actionType, Map<String, Object> details) throws Exception {
           if (!checkGameFocus()) return; // Use helper

           Map<String, Object> payload = new HashMap<>();
           payload.put("actionType", actionType);
           payload.put("details", details);
           String jsonBody = objectMapper.writeValueAsString(payload);

           HttpRequest request = buildRequest("POST", "/games/" + currentGameId + "/action", jsonBody);

           System.out.println("Sending action request to POST " + request.uri());
           System.out.println("Payload: " + jsonBody);
           sendRequestAndPrintResponse(request);
      }


    private static HttpRequest buildRequest(String method, String path, String jsonBody) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + path));

        if (firebaseIdToken != null) {
            builder.header("Authorization", "Bearer " + firebaseIdToken);
        }

        if ("GET".equalsIgnoreCase(method)) {
            builder.GET();
        } else if ("POST".equalsIgnoreCase(method)) {
            builder.header("Content-Type", "application/json");
            builder.POST(jsonBody == null || jsonBody.isEmpty() ? BodyPublishers.noBody() : BodyPublishers.ofString(jsonBody));
        } else if ("DELETE".equalsIgnoreCase(method)) {
            builder.DELETE();
        }
        // Add PUT, PATCH etc. if needed

        return builder.build();
    }


    private static HttpResponse<String> sendRequestAndGetResponse(HttpRequest request) throws Exception {
         try {
              return client.send(request, HttpResponse.BodyHandlers.ofString());
         } catch (java.net.ConnectException e) {
             System.err.println("[Error] Connection refused. Is the backend server running at " + API_BASE_URL + "?");
             throw e;
         } catch (Exception e) {
              System.err.println("[Error] Failed to send request: " + e.getMessage());
              throw e;
         }
    }

    private static void sendRequestAndPrintResponse(HttpRequest request) throws Exception {
        HttpResponse<String> response = sendRequestAndGetResponse(request);
        printFormattedResponse(response);
    }

    private static void printFormattedResponse(HttpResponse<String> response) {
        System.out.println("Status Code: " + response.statusCode());
        String contentType = response.headers().firstValue("Content-Type").orElse("unknown");
        System.out.println("Content-Type: " + contentType);

        String body = response.body();
        if (body != null && !body.isEmpty()) {
            if (contentType.contains("application/json")) {
                try {
                    Object json = objectMapper.readValue(body, Object.class);
                    System.out.println("Response Body:\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
                } catch (Exception e) {
                    System.out.println("Response Body (Invalid JSON?):\n" + body);
                }
            } else {
                System.out.println("Response Body:\n" + body);
            }
        } else {
             System.out.println("Response Body: (empty)");
        }
        System.out.println("---");
    }
}


