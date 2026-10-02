package com.graduation.auth.config;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.security.oauth2.jose.jws.MacAlgorithm; import org.springframework.security.oauth2.jwt.*; import javax.crypto.SecretKey; import javax.crypto.spec.SecretKeySpec; import java.nio.charset.StandardCharsets;
@Configuration
public class JwtConfig{
 @Bean SecretKey jwtSecretKey(@Value("${jwt.secret}") String secret){ byte[] b=secret.getBytes(StandardCharsets.UTF_8); if(b.length<32) throw new IllegalStateException("JWT_SECRET phải có ít nhất 32 bytes để dùng HS256"); return new SecretKeySpec(b,"HmacSHA256"); }
 @Bean JwtEncoder jwtEncoder(SecretKey key){ return NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build(); }
}
