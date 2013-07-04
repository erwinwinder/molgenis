package org.molgenis.omx;

import org.molgenis.security.MolgenisUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebAppSecurityConfig extends WebSecurityConfigurerAdapter
{

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	@Override
	protected UserDetailsService userDetailsService()
	{
		return new MolgenisUserDetailsService(passwordEncoder());
	}

	@Override
	@Bean
	public UserDetailsService userDetailsServiceBean() throws Exception
	{
		return userDetailsService();
	}

	@Override
	protected void registerAuthentication(AuthenticationManagerBuilder auth)
	{
		try
		{
			auth.userDetailsService(userDetailsServiceBean());

			DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
			daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
			daoAuthenticationProvider.setUserDetailsService(userDetailsServiceBean());
			auth.authenticationProvider(daoAuthenticationProvider);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http.authorizeUrls().antMatchers("/css/**").permitAll().antMatchers("/js/**").permitAll()
				.regexMatchers(".*Admin").hasRole("ADMIN").antMatchers("/img/**").permitAll().anyRequest()
				.authenticated().and().formLogin().loginUrl("/login").defaultSuccessUrl("/").loginPage("/login")
				.permitAll().and().logout().logoutUrl("/logout").permitAll().logoutSuccessUrl("/login");
	}

}
