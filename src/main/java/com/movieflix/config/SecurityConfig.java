package com.movieflix.config;

import com.movieflix.auth.services.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

// Logger để ghi log nếu cần
@Slf4j
// Đánh dấu đây là class cấu hình
@Configuration
// Kích hoạt Spring Security
@EnableWebSecurity
// Cho phép sử dụng @PreAuthorize, @Secured ở các method
@EnableMethodSecurity
// Lombok tự tạo constructor cho các biến final
@RequiredArgsConstructor
public class SecurityConfig {

    // Filter dùng để xử lý JWT trước khi request vào controller
    private final JwtAuthenticationFilter jwtAuthFilter;

    // Service để load thông tin người dùng từ database
    private final UserDetailsService userDetailsService;

    // Cấu hình chuỗi filter của Spring Security
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì không dùng cookie session
                .csrf(AbstractHttpConfigurer::disable)
                // Cấu hình CORS (chặn hoặc cho phép domain gọi API)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Phân quyền truy cập cho từng endpoint
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập không cần đăng nhập với các endpoint này
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/file/**").permitAll()
                        .requestMatchers("/video/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        .requestMatchers("/api/v1/movie/all").permitAll()
                        .requestMatchers("/api/v1/movie/{movieId}").permitAll()
                        .requestMatchers("/api/v1/movie/allMoviesPage").permitAll()
                        .requestMatchers("/api/v1/movie/allMoviesPageSort").permitAll()
                        // Các request còn lại phải đăng nhập mới được truy cập
                        .anyRequest().authenticated()
                )
                // Cấu hình session là Stateless - không lưu session trên server
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Xác thực bằng custom provider (sử dụng DB và BCrypt)
                .authenticationProvider(authenticationProvider())
                // Thêm JWT filter vào trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cấu hình provider để xác thực người dùng bằng cách truy vấn DB
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Đọc thông tin người dùng
        authProvider.setPasswordEncoder(passwordEncoder()); // Dùng BCrypt để mã hóa mật khẩu
        return authProvider;
    }

    // Cấu hình AuthenticationManager - cần thiết khi dùng Security để đăng nhập
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Cấu hình CORS để cho phép gọi API từ các domain khác nhau (ví dụ frontend React chạy ở localhost:3000)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // Cho phép tất cả domain
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // Các method được phép
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token")); // Cho phép các header
        configuration.setExposedHeaders(Arrays.asList("x-auth-token")); // Cho phép client đọc header này từ response
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Áp dụng cho tất cả API
        return source;
    }

    // Bean để mã hóa mật khẩu người dùng bằng thuật toán BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
