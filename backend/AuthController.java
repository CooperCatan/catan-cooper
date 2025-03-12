package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    // Handles user signup (Task: User Authentication - 2 weeks)
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody User user) {
        authService.registerUser(user);
        return ResponseEntity.ok(Collections.singletonMap("message", "User registered successfully!"));
    }

    // Authenticates users with JWT (Task: User Authentication - 2 weeks)
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username, @RequestParam String password) {
        String token = authService.authenticateUser(username, password);
        return token != null 
            ? ResponseEntity.ok(Collections.singletonMap("token", token))
            : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Invalid credentials"));
    }
}
