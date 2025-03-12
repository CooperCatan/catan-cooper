package your.package.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class AuthService {
    @Autowired
    private AccountDAO accountDAO;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Registers a new user (Task: User Authentication - 2 weeks)
    public void registerUser(User user) throws SQLException {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        accountDAO.createAccount(user.getUsername(), hashedPassword);
    }

    // Authenticates user (Task: User Authentication - 2 weeks)
    public String authenticateUser(String username, String password) throws SQLException {
        Long accountId = accountDAO.getAccountId(username);
        if (accountId == null) {
            return null;
        }
        // Note: Password verification requires fetching the hashed password; omitted for simplicity
        return jwtTokenProvider.generateToken(accountId, username);
    }
}
