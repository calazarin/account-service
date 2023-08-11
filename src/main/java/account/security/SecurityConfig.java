package account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import static account.enums.UserRoleEnum.ACCOUNTANT;
import static account.enums.UserRoleEnum.ADMINISTRATOR;
import static account.enums.UserRoleEnum.AUDITOR;
import static account.enums.UserRoleEnum.USER;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder,
                                                       UserDetailsService userDetailsService)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider())
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
            .and()
                .csrf().disable().headers().frameOptions().disable()
            .and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET,"/api/empl/payment")
                .hasAnyRole(USER.getShortName(),ACCOUNTANT.getShortName())
            .and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.POST,"/api/auth/changepass")
                .authenticated()
            .and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.POST,"/api/acct/payments")
                .hasRole(ACCOUNTANT.getShortName())
            .and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.PUT,"/api/acct/payments")
                .hasRole(ACCOUNTANT.getShortName())
            .and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET,"/api/admin/user/")
                .hasRole(ADMINISTRATOR.getShortName())
            .and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.PUT,"/api/admin/user/role")
                .hasRole(ADMINISTRATOR.getShortName())
            .and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.DELETE,"/api/admin/user/**")
                .hasRole(ADMINISTRATOR.getShortName())
            .and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.DELETE,"/api/admin/user/")
                .hasRole(ADMINISTRATOR.getShortName())
            .and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.PUT,"/api/admin/user/access")
                .hasRole(ADMINISTRATOR.getShortName())
            .and()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET,"/api/security/events/")
                .hasRole(AUDITOR.getShortName())
            .and()
                .authorizeHttpRequests()
                .anyRequest()
                .permitAll()
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler());
        return http.build();
    }

}
