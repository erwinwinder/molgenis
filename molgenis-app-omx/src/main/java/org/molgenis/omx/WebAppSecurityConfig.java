package org.molgenis.omx;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.molgenis.security.MolgenisUserDetailsService;
import org.molgenis.security.RowLevelSecurityService;
import org.molgenis.security.RowLevelSecurityServiceImpl;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableTransactionManagement
public class WebAppSecurityConfig extends WebSecurityConfigurerAdapter
{
	@Bean
	public RowLevelSecurityService rowLevelSecurityService()
	{
		return new RowLevelSecurityServiceImpl(mutableAclService(), permissionEvaluator());
	}

	@Bean
	public DataSource aclDataSource()
	{
		// TODO get datasource from jpa (how?) or create shared datasource for JPA/JDBC
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost/omx?innodb_autoinc_lock_mode=2&amp;rewriteBatchedStatements=true");
		ds.setUsername("molgenis");
		ds.setPassword("molgenis");

		return ds;
	}

	@Bean
	public PlatformTransactionManager txManager()
	{
		return new DataSourceTransactionManager(aclDataSource());
	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheManagerFactoryBean()
	{
		return new EhCacheManagerFactoryBean();
	}

	@Bean
	public EhCacheFactoryBean ehCacheFactoryBean()
	{
		EhCacheFactoryBean ehCacheFactoryBean = new EhCacheFactoryBean();
		ehCacheFactoryBean.setCacheManager(ehCacheManagerFactoryBean().getObject());
		ehCacheFactoryBean.setCacheName("aclCache");

		return ehCacheFactoryBean;
	}

	@Bean
	public AclCache aclCache()
	{

		return new EhCacheBasedAclCache(ehCacheFactoryBean().getObject(), permissionGrantingStrategy(),
				aclAuthorizationStrategy());
	}

	@Bean
	public AuditLogger auditLogger()
	{
		return new ConsoleAuditLogger();
	}

	@Bean
	public PermissionGrantingStrategy permissionGrantingStrategy()
	{
		return new DefaultPermissionGrantingStrategy(auditLogger());
	}

	@Bean
	public AclAuthorizationStrategy aclAuthorizationStrategy()
	{
		return new AclAuthorizationStrategyImpl(new GrantedAuthority[]
		{ new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_ADMIN"),
				new SimpleGrantedAuthority("ROLE_ADMIN") });
	}

	@Bean
	public LookupStrategy lookupStrategy()
	{
		return new BasicLookupStrategy(aclDataSource(), aclCache(), aclAuthorizationStrategy(),
				permissionGrantingStrategy());
	}

	@Bean
	public MutableAclService mutableAclService()
	{
		JdbcMutableAclService mutableAclService = new JdbcMutableAclService(aclDataSource(), lookupStrategy(),
				aclCache());
		mutableAclService.setSidIdentityQuery("SELECT LAST_INSERT_ID()");
		mutableAclService.setClassIdentityQuery("SELECT LAST_INSERT_ID()");

		return mutableAclService;
	}

	@Bean
	public PermissionEvaluator permissionEvaluator()
	{
		return new AclPermissionEvaluator(mutableAclService());
	}

	/*
	 * public ExpressionHandler expressionHandler() { return new DefaultMethodSecurityExpressionHandler(); }
	 */

	// private List<? extends GrantedAuthority> getAllRoles()
	// {
	// // TODO from MolgenisUserDetailsService
	// return Arrays.asList(new SimpleGrantedAuthority("ROLE_SU"), new SimpleGrantedAuthority("ROLE_ADMIN"),
	// new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_READ_DATASET"));
	// }

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception
	{
		return super.authenticationManagerBean();
	}

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
				.regexMatchers(".*UploadWizard").hasRole("ADMIN").antMatchers("/img/**").permitAll().anyRequest()
				.authenticated().and().formLogin().loginUrl("/login").defaultSuccessUrl("/").loginPage("/login")
				.permitAll().and().logout().logoutUrl("/logout").permitAll().logoutSuccessUrl("/login");
	}

}
