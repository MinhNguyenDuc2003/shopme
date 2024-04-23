package com.shopme.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.shopme.security.oauth.CustomerOAuth2UserService;
import com.shopme.security.oauth.OAuth2LoginSuccessHandler;

@Configuration
public class WebSecurityConfig {
	
	@Autowired private CustomerOAuth2UserService oAuth2UserService;
	@Autowired private OAuth2LoginSuccessHandler oauthLoginHandler;
	@Autowired private DatabaseLoginSuccessHandler databaseLoginHandler;
	
	@Bean
	CustomerUserDetailsService customerUserDetailsService() {
		return new CustomerUserDetailsService();
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(customerUserDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		
		return authProvider;
	}
	

	@Bean
	SecurityFilterChain configureHttpSecurity(HttpSecurity http) throws Exception {
		http.authenticationProvider(authenticationProvider());
		http.authorizeHttpRequests((auth) -> auth.requestMatchers("/account_details", "/update_account_details", "/orders/**",
					"/cart", "/address_book/**", "/checkout", "/place_order", "/reviews/**", 
					"/process_paypal_order", "/write_review/**", "/post_review").authenticated()
					.anyRequest().permitAll()
	    ).formLogin(form -> form			
				.loginPage("/login")
				.usernameParameter("email")
				.permitAll()
				.successHandler(databaseLoginHandler))
		.oauth2Login(form -> form
				.loginPage("/login")
				.userInfoEndpoint()
				.userService(oAuth2UserService)
				.and().successHandler(oauthLoginHandler))
	    .logout( logout -> logout.permitAll())
		.rememberMe(rem -> {
			try {
				rem
					.key("AbcDefgHijKlmnOpqrs_1234567890")
					.tokenValiditySeconds(7 * 24 * 60 * 60)
					.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			);
	http.headers(header -> header.frameOptions(f -> f.sameOrigin()));
		return http.build();
	}

	@Bean
	WebSecurityCustomizer configure() throws Exception {
		return (web) -> web.ignoring().requestMatchers("/images/**", "/js/**", "/webjars/**");
	}

}
