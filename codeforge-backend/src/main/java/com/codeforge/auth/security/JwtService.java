package com.codeforge.auth.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.codeforge.usuario.entity.Usuario;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "CodeForgeAISecretKey2026CodeForgeAISecretKey2026";

    private static final long EXPIRATION_TIME =
            1000L * 60 * 60 * 24; // 24 horas

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes()
        );
    }

    public String generarToken(Usuario usuario) {

        Date ahora = new Date();

        Date expiracion =
                new Date(
                        ahora.getTime()
                                + EXPIRATION_TIME
                );

        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim(
                        "idUsuario",
                        usuario.getIdUsuario()
                )
                .claim(
                        "nombre",
                        usuario.getNombre()
                )
                .claim(
                        "correo",
                        usuario.getCorreo()
                )
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    public String obtenerUsername(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean esTokenValido(String token) {

        try {

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}