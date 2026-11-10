package org.rostyslav.forewa.config;

import org.rostyslav.forewa.auth.UserAccount.UserAccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserAccountService accounts
    ) {

        OidcUserService delegate = new OidcUserService();

        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(info -> info
                                .oidcUserService(request -> {
                                    OidcUser user = delegate.loadUser(request);
                                    accounts.syncGoogleUser(user);
                                    return user;
                                })
                        )
                );

        return http.build();
    }
}
