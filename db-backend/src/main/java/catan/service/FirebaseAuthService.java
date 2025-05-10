package catan.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {
    
    public String verifyToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        return decodedToken.getEmail();
    }
    
    public void deleteUser(String email) throws FirebaseAuthException {
        var user = FirebaseAuth.getInstance().getUserByEmail(email);
        if (user != null) {
            FirebaseAuth.getInstance().deleteUser(user.getUid());
        }
    }
} 