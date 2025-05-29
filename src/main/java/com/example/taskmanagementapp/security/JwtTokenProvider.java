package com.example.taskmanagementapp.security;

import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.service.UserPermissionService;
import com.example.taskmanagementapp.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtTokenProvider {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;


    public String generateToken(UserDetails userDetails){
        return Jwts
                .builder()
                .setClaims(getTokenClaims(userDetails))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(generateKey())
                .compact();
    }

    @Autowired
    private UserPermissionService userPermissionService;

    private Map<String, Object> getTokenClaims(UserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        User user = (User)userDetails;

        claims.put("sub", userDetails.getUsername());
        claims.put("id", String.valueOf(user.getId()));

        // Add user permissions for all workspaces (avoiding duplicates)
        Map<Long, String[]> permissions = userPermissionService.getUserWorkspacePermissions(user);

        // Optimize token size by removing any empty permission arrays
        Map<Long, String[]> filteredPermissions = new HashMap<>();
        for (Map.Entry<Long, String[]> entry : permissions.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length > 0) {
                filteredPermissions.put(entry.getKey(), entry.getValue());
            }
        }

        claims.put("permissions", filteredPermissions);

        return claims;
    }

    public Boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(generateKey()).build().parseClaimsJws(token);
            return true;
        }catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException |
                PrematureJwtException expiredJwtException){
            expiredJwtException.printStackTrace();
        }
        return false;
    }

    public String extractEmailFromToken(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(generateKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Extract user permissions from the JWT token
     * @param token JWT token
     * @return Map of workspace IDs to permission strings
     */
    @SuppressWarnings("unchecked")
    public Map<Long, String[]> extractPermissionsFromToken(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(generateKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Extract the permissions map from claims
        Map<String, Object> permissionsMap = (Map<String, Object>) claims.get("permissions");

        // Convert string keys to Long and values to String[]
        Map<Long, String[]> result = new HashMap<>();
        if (permissionsMap != null) {
            for (Map.Entry<String, Object> entry : permissionsMap.entrySet()) {
                Long workspaceId = Long.parseLong(entry.getKey());
                List<String> permList = (List<String>) entry.getValue();
                String[] permissions = permList.toArray(new String[0]);
                result.put(workspaceId, permissions);
            }
        }

        return result;
    }


    private Key generateKey(){
        byte[]decodedSecretKey = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(decodedSecretKey);
    }


}
