package com.pe.finanzas.auth.service;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.system.service.ConfiguracionService; // Ajustado el import

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @Value("${jwt.expiration}")
    private Long defaultExpiration;

    private final ConfiguracionService configuracionService;

    public JwtService(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    public String generateToken(DatosPersonales user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("sexo", user.getSexo());

        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()));

        claims.put("permissions", user.getPermissionNames());

        Date now = new Date();

        Long minutesObject = configuracionService.getGlobalSessionTimeout();
        long expirationMillis = (minutesObject != null && minutesObject > 0)
                ? minutesObject * 60 * 1000
                : defaultExpiration;

        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getLogin()) // IMPORTANTE: Usamos login en Finanzas
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }
    
    public Long extractSucursalId(String token) {
        return extractAllClaims(token).get("sucursalId", Long.class);
    }
    
    public Long extractPuntoId(String token) {
        return extractAllClaims(token).get("puntoId", Long.class);
    }

    public Long getTimeRemaining(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        long remaining = expiration.getTime() - now.getTime();
        return remaining > 0 ? remaining : 0L;
    }

    public Long getTimeRemainingInSeconds(String token) {
        return getTimeRemaining(token) / 1000;
    }

    public Date getExpirationTime() {
        return new Date(System.currentTimeMillis() + defaultExpiration);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private PrivateKey getPrivateKey() {
        try {
            String keyContent = loadKeyContent(privateKeyPath);
            keyContent = keyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            log.error("Error al cargar llave privada", e);
            throw new RuntimeException("Error al cargar llave privada", e);
        }
    }

    private PublicKey getPublicKey() {
        try {
            String keyContent = loadKeyContent(publicKeyPath);
            keyContent = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            log.error("Error al cargar llave publica", e);
            throw new RuntimeException("Error al cargar llave publica", e);
        }
    }

    private String loadKeyContent(String path) throws Exception {
        String resourcePath = path.replace("classpath:", "");
        try (InputStream inputStream = new ClassPathResource(resourcePath).getInputStream()) {
            return new String(inputStream.readAllBytes());
        }
    }

    public String generateTokenWithSucursal(DatosPersonales user, Long sucursalId, String sucursalNombre) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()));
        claims.put("permissions", user.getPermissionNames());
        claims.put("sucursalId", sucursalId);
        claims.put("sucursalNombre", sucursalNombre);

        Date now = new Date();
        Long minutesObject = configuracionService.getGlobalSessionTimeout();
        long expirationMillis = (minutesObject != null && minutesObject > 0) ? minutesObject * 60 * 1000
                : defaultExpiration;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getLogin())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMillis))
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateTokenWithSucursalAndPunto(DatosPersonales user, Long sucursalId, String sucursalNombre,
            Long puntoId, String puntoNombre) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()));
        claims.put("permissions", user.getPermissionNames());
        claims.put("sucursalId", sucursalId);
        claims.put("sucursalNombre", sucursalNombre);

        if (puntoId != null) {
            claims.put("puntoId", puntoId);
            claims.put("puntoNombre", puntoNombre);
        }

        Date now = new Date();
        Long minutesObject = configuracionService.getGlobalSessionTimeout();
        long expirationMillis = (minutesObject != null && minutesObject > 0) ? minutesObject * 60 * 1000
                : defaultExpiration;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getLogin())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMillis))
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }
}