package com.professionisti.sport.util;

import com.professionisti.sport.model.User;
// In a real app, you would use io.smallrye.jwt.build.Jwt for building
// and configure it via application.properties (e.g. issuer, keys)
// import io.smallrye.jwt.build.Jwt;
// import java.util.Set;
// import java.util.HashSet;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;


public class JwtUtil {

    private static final String JWT_SECRET_KEY = "your-super-secret-key-for-dev-only"; // IMPORTANT: Use a strong, configured key
    private static final String ISSUER = "fitness-app-backend";
    private static final long EXPIRATION_MINUTES = 60; // Token valid for 60 minutes

    // This is a highly simplified, insecure placeholder for JWT generation.
    // DO NOT USE THIS IN PRODUCTION.
    // A real implementation would use a proper JWT library (e.g., SmallRye JWT).
    public static String generateToken(User user) {
        // Header (typically {"alg": "HS256", "typ": "JWT"}) - simplified
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

        // Payload
        String payload = String.format("{\"sub\":\"%s\",\"iss\":\"%s\",\"name\":\"%s %s\",\"role\":\"%s\",\"exp\":%d}",
                user.getEmail(),
                ISSUER,
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES).getEpochSecond()
        );

        String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());

        // Signature (highly simplified and insecure - real HS256 involves HMAC)
        // This is NOT a real signature.
        String signatureInput = encodedHeader + "." + encodedPayload;
        // In a real scenario: HMACSHA256(signatureInput, JWT_SECRET_KEY)
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString( (signatureInput + JWT_SECRET_KEY).getBytes() );


        return String.format("%s.%s.%s", encodedHeader, encodedPayload, signature);
    }

    // Placeholder for token validation. Real validation is also complex.
    public static boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // In a real app: parse token, verify signature, check expiration, check issuer etc.
        // This is a dummy validation.
        String[] parts = token.split("\."); // Split on literal dot
        if (parts.length != 3) return false;

        // Extremely basic check, not secure.
        // String signatureInput = parts[0] + "." + parts[1];
        // String expectedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString( (signatureInput + JWT_SECRET_KEY).getBytes() );
        // return expectedSignature.equals(parts[2]);
        return true; // For now, assume valid if structure is okay.
    }

    // Placeholder for getting subject (email) from token.
    public static String getEmailFromToken(String token) {
         if (!validateToken(token)) return null;
         try {
            String payloadBase64 = token.split("\.")[1]; // Split on literal dot
            String payload = new String(Base64.getUrlDecoder().decode(payloadBase64));
            // Simple parsing, assumes "sub" field is present and value is quoted.
            // A proper JSON parser should be used here for robustness.
            String searchKey = "\"sub\":\"";
            int subStartIndex = payload.indexOf(searchKey);
            if (subStartIndex != -1) {
                subStartIndex += searchKey.length();
                int subEndIndex = payload.indexOf("\"", subStartIndex);
                if (subEndIndex != -1) {
                    return payload.substring(subStartIndex, subEndIndex);
                }
            }
         } catch (Exception e) {
            // Log the exception or handle it appropriately
            // e.printStackTrace();
            return null; // Invalid token format or decoding error
         }
         return null;
    }
}
