package com.samatha.javachallengeindpro.controller;

import com.samatha.javachallengeindpro.dto.User;
import com.samatha.javachallengeindpro.exceptions.UserInvalidException;
import com.samatha.javachallengeindpro.model.AuthRequest;
import com.samatha.javachallengeindpro.model.AuthResponse;
import com.samatha.javachallengeindpro.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String AES_KEY = "YourAESKey1234567890123456789011";
    private static final String SECRET_KEY = "YourAESKey1234567890123456789011";
    private static final long TOKEN_EXPIRATION_TIME = 300000;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody String encryptedCredentials) {
        // Decrypt the encryptedCredentials using AES256
        // Separate the username and password from the decrypted string
        String username = encryptedCredentials.substring(0, encryptedCredentials.indexOf("+"));
        String password = encryptedCredentials.substring(encryptedCredentials.indexOf("+") + 1);
         username = decryptWithAES256(username);
         password = decryptWithAES256(password);

        // Query the database to authenticate the user
        User user = userRepository.findByUsername(username);
        if (user == null || !password.equals(user.getPassword())) {
            throw new UserInvalidException("Invalid username or password");
        }

        // Generate authentication token
        String authToken = generateAuthToken(user);

        // Return user details and authentication token in the response
        AuthResponse response = new AuthResponse(user.getId(), user.getUsername(), user.getRole(), authToken);
        return ResponseEntity.ok(response);
    }

    private String decryptWithAES256(String encryptedString) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(AES_KEY);
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedString));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("AES decryption failed", ex);
        }
    }
    private String generateAuthToken(User user) {
        try {
            // Set token expiration time
            long expirationTimeMillis = System.currentTimeMillis() + TOKEN_EXPIRATION_TIME;

            // Create claims for the token payload
            Claims claims = Jwts.claims();
            claims.put("userId", user.getId());
            claims.put("username", user.getUsername());
            claims.put("role", user.getRole());

            // Generate the JWT token
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(new Date(expirationTimeMillis))
                    .signWith(SignatureAlgorithm.HS256,SECRET_KEY.getBytes(StandardCharsets.UTF_8) )
                    .compact();

            return token;
        } catch (Exception ex) {
            throw new RuntimeException("Token generation failed", ex);
        }
    }




    @PostMapping("/getEncryptedCredentials")
    public ResponseEntity<String> getEncryptedCredentials(@RequestBody AuthRequest authRequest) {
        // Usage example
        String username = authRequest.getUsername();
        String password = authRequest.getPassword();

        String encryptedUsername = encryptWithAES256(username);
        String encryptedPassword = encryptWithAES256(password);

        System.out.println("Encrypted Username: " + encryptedUsername);
        System.out.println("Encrypted Password: " + encryptedPassword);

        return new ResponseEntity( encryptedUsername +"+"+encryptedPassword, HttpStatus.OK);
    }



    private String encryptWithAES256(String text) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(AES_KEY);
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception ex) {
            throw new RuntimeException("AES encryption failed", ex);
        }
    }
}