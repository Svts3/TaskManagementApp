package com.example.taskmanagementapp.security;

import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
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

    private Map<String, String> getTokenClaims(UserDetails userDetails){
        Map<String, String> claims = new HashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("id", String.valueOf(((User)userDetails).getId()));
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


    private Key generateKey(){
        byte[]decodedSecretKey = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(decodedSecretKey);
    }


}
