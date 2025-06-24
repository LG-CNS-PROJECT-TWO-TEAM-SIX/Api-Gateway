package com.team6.api_gateway.jwt;

import com.team6.api_gateway.jwt.authentication.JwtAuthentication;
import com.team6.api_gateway.jwt.authentication.UserPrincipal;
import com.team6.api_gateway.jwt.props.JwtConfigProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {
    private final JwtConfigProperties configProperties;
    private volatile SecretKey secretKey;

    private SecretKey getSecretKey() {
        if (secretKey == null) {
            synchronized (this) {
                if (secretKey == null) {
                    log.info("SecretKey 초기화 시도");
                    secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(configProperties.getSecretKey()));
                    log.info("SecretKey 초기화 완료");
                }
            }
        }
        return secretKey;
    }

    public JwtAuthentication validateToken(String token) {
        log.info("토큰 검증 시작: {}", token);

        String userId = null;
        final Claims claims = this.verifyAndGetClaims(token);

        if (claims == null) {
            log.warn("토큰의 클레임이 null입니다. 토큰이 유효하지 않음");
            return null;
        }

        Date expirationDate = claims.getExpiration();
        log.info("토큰 만료 시간: {}", expirationDate);

        if (expirationDate == null) {
            log.warn("토큰에 만료시간이 없습니다.");
            return null;
        }
        if (expirationDate.before(new Date())) {
            log.warn("토큰이 만료되었습니다.");
            return null;
        }

        userId = claims.get("userId", String.class);
        String tokenType = claims.get("tokenType", String.class);

        log.info("토큰 userId: {}, tokenType: {}", userId, tokenType);

        if (!"access".equals(tokenType)) {
            log.warn("토큰 타입이 access가 아닙니다. tokenType={}", tokenType);
            return null;
        }

        UserPrincipal principal = new UserPrincipal(userId);
        log.info("JwtAuthentication 생성 완료, userId={}", userId);

        return new JwtAuthentication(principal, token, getGrantedAuthorities("user"));
    }

    private Claims verifyAndGetClaims(String token) {
        Claims claims;
        try {
            log.info("토큰 서명 검증 시도");
            claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            log.info("토큰 서명 검증 성공");
        } catch (Exception e) {
            log.error("토큰 서명 검증 실패", e);
            claims = null;
        }
        return claims;
    }

    private List<GrantedAuthority> getGrantedAuthorities(String role) {
        ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if (role != null) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role));
            log.info("GrantedAuthority 추가: {}", role);
        }
        return grantedAuthorities;
    }

    public String getToken(HttpServletRequest request) {
        String authHeader = getAuthHeaderFromHeader(request);
        log.info("Authorization 헤더 값: {}", authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("헤더에서 토큰 추출 성공: {}", token);
            return token;
        }
        log.warn("Authorization 헤더가 없거나 Bearer로 시작하지 않음");
        return null;
    }

    private String getAuthHeaderFromHeader(HttpServletRequest request) {
        String header = request.getHeader(configProperties.getHeader());
        log.info("헤더 {} 값 조회: {}", configProperties.getHeader(), header);
        return header;
    }
}