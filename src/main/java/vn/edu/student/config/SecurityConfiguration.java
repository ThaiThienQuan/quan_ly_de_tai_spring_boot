package vn.edu.student.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/register", "/css/**", "/error").permitAll()
                .requestMatchers("/periods/**").hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers("/departments/**").hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers("/topics/create").hasAnyRole("GIANG_VIEN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/topics").hasAnyRole("GIANG_VIEN", "ADMIN")
                .requestMatchers("/topics/*/approve", "/topics/*/publish").hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers(HttpMethod.GET, "/groups").hasAnyRole("ADMIN", "TRUONG_KHOA", "SINH_VIEN")
                .requestMatchers("/groups/registrations/*/approve", "/groups/registrations/*/reject")
                    .hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers("/groups/**").hasRole("SINH_VIEN")
                .requestMatchers(HttpMethod.GET, "/reviews/results").hasRole("SINH_VIEN")
                .requestMatchers(HttpMethod.GET, "/reviews").hasAnyRole("ADMIN", "TRUONG_KHOA", "GIANG_VIEN", "CHU_TICH_HOI_DONG")
                .requestMatchers(HttpMethod.POST, "/reviews/councils", "/reviews/assignments")
                    .hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers(HttpMethod.POST, "/reviews/assignments/*/evaluation",
                    "/reviews/councils/*/topics/*/publish")
                    .hasAnyRole("ADMIN", "GIANG_VIEN", "CHU_TICH_HOI_DONG")
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/notifications", "/notifications/*/publish")
                    .hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers(HttpMethod.GET, "/notifications").authenticated()
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"));
        return http.build();
    }
}
