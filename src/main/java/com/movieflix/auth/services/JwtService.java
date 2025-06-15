package com.movieflix.auth.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {

    // Khóa bí mật dùng để ký và xác thực JWT (mã hóa Base64)
    private static final String SECRET_KEY = "BF7FD11ACE545745B7BA1AF98B6F156D127BC7BB544BAB6A4FD74E4FC7";

    // Lấy username từ JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // Subject thường chứa username
    }

    // Trích xuất một claim bất kỳ từ token thông qua một hàm xử lý
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // Trích xuất toàn bộ claims
        return claimsResolver.apply(claims); // Trả về claim cụ thể
    }

    // Trích xuất toàn bộ dữ liệu (claims) từ JWT
    private Claims extractAllClaims(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey()) // Thiết lập khóa xác thực
                .build()
                .parseClaimsJws(token) // Phân tích token
                .getBody(); // Lấy phần payload (nội dung) của token
        log.info("Token claims: {}", claims);
        return claims;
    }

    // Lấy ra khóa ký từ chuỗi bí mật (decode base64)
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY); // Giải mã Base64
        return Keys.hmacShaKeyFor(keyBytes); // Tạo khóa HMAC-SHA256
    }

    // Tạo JWT không có thông tin thêm
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Tạo JWT token với thông tin thêm (extraClaims) và userDetails
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        // Lấy danh sách quyền (authorities) và gộp thành chuỗi
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        log.info("Generating token for user {} with authorities: {}", userDetails.getUsername(), authorities);

        extraClaims.put("authorities", authorities); // Đưa vào phần claim

        return Jwts
                .builder()
                .setClaims(extraClaims) // Gán thông tin thêm
                .setSubject(userDetails.getUsername()) // Gán username
                .setIssuedAt(new Date(System.currentTimeMillis())) // Thời gian phát hành
                .setExpiration(new Date(System.currentTimeMillis() + 25 * 100000)) // Hạn sử dụng (ví dụ: 25 * 100000ms)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Ký bằng thuật toán SHA256
                .compact(); // Tạo token
    }

    // Kiểm tra token có hợp lệ không (khớp username và chưa hết hạn)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            log.info("Validating token for username: {} against userDetails username: {}", username, userDetails.getUsername());

            Claims claims = extractAllClaims(token);
            log.info("Token claims: {}", claims);
            log.info("Token expiration: {}", claims.getExpiration());
            log.info("Current time: {}", new Date());

            boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
            log.info("Token validation result - Valid: {}, Username match: {}, Not expired: {}",
                    isValid,
                    username.equals(userDetails.getUsername()),
                    !isTokenExpired(token));
            return isValid;
        } catch (Exception e) {
            log.error("Error validating token", e); // Nếu lỗi thì token không hợp lệ
            return false;
        }
    }

    // Kiểm tra token có hết hạn chưa
    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token); // Lấy ngày hết hạn
            boolean isExpired = expiration.before(new Date()); // So sánh với hiện tại
            log.info("Token expiration check - Expiration: {}, Current time: {}, Is expired: {}",
                    expiration, new Date(), isExpired);
            return isExpired;
        } catch (Exception e) {
            log.error("Error checking token expiration", e);
            return true; // Nếu lỗi thì coi như token đã hết hạn
        }
    }

    // Lấy thời gian hết hạn của token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
