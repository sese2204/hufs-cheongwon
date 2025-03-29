package com.hufs_cheongwon.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hufs_cheongwon.common.exception.AuthenticationException;
import com.hufs_cheongwon.common.security.*;
import com.hufs_cheongwon.repository.AdminRepository;
import com.hufs_cheongwon.repository.BlackListRepository;
import com.hufs_cheongwon.repository.UsersRepository;
import com.hufs_cheongwon.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAdminDetailsService customAdminDetailsService;
    private final TokenService tokenService;
    private final AuthenticationException authenticationException;
    private final BlackListRepository blackListRepository;
    private final UsersRepository usersRepository;
    private final AdminRepository adminRepository;

    @Bean
    public SecurityFilterChain commonFilterChain(HttpSecurity http) throws Exception{
        //csrf, Form 로그인 방식, http basic 인증 방식 disable (세션 방식이 아닌 jwt 방식 로그인을 사용하기 때문)
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf((auth) -> auth.disable())
                .formLogin((auth) -> auth.disable())
                .httpBasic((auth) -> auth.disable())
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/admin/login","/admin/pwd","/user/test","/user/pwd/update").permitAll()
                        .requestMatchers("/petitions/{petition_id}/agree", "/petitions/{petition_id}/report", "/petitions/bookmark",
                                "/petitions/new", "/user/logout", "/user/delete").hasRole("USER")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER")
                        .requestMatchers("/super/**").hasRole("SUPER")
                        .anyRequest().permitAll())
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationException)
                );

        return http.build();
    }

    //CustomUserDetails 두 개 설정 -> 명시적으로 등록 해줘야 함
    @Bean
    public JwtUserLoginFilter jwtUserLoginFilter() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        JwtUserLoginFilter jwtUserLoginFilter = new JwtUserLoginFilter(
                providerManager,
                jwtUtil,
                objectMapper,
                tokenService);
        jwtUserLoginFilter.setAuthenticationManager(providerManager);
        jwtUserLoginFilter.setFilterProcessesUrl("/user/login");
        return jwtUserLoginFilter;
    }

    @Bean
    public JwtAdminLoginFilter jwtAdminLoginFilter() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customAdminDetailsService);
        authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        JwtAdminLoginFilter jwtAdminLoginFilter = new JwtAdminLoginFilter(
                providerManager,
                jwtUtil,
                objectMapper,
                tokenService);
        jwtAdminLoginFilter.setAuthenticationManager(providerManager);
        jwtAdminLoginFilter.setFilterProcessesUrl("/admin/login");
        return jwtAdminLoginFilter;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthorizationFilter jwtFilter() {
        return JwtAuthorizationFilter.builder()
                .jwtUtil(jwtUtil)
                .authenticationException(authenticationException)
                .blackListRepository(blackListRepository)
                .usersRepository(usersRepository)
                .adminRepository(adminRepository)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "https://hufs-petition.vercel.app",
                "https://sinmungo.shop"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Set-Cookie"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
