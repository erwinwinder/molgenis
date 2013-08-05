package org.molgenis.omx;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.config.annotation.authentication.configurers.InMemoryClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.OAuth2ServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.OAuth2ServerConfigurer;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.TokenServicesUserApprovalHandler;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;

@Configuration
@Order(1)
public class WebAppOAuth2ServerConfiguration extends OAuth2ServerConfigurerAdapter
{
	private static final String RESOURCE_ID = "molgenis-api";

	@Bean
	@DependsOn("springSecurityFilterChain")
	public TokenServicesUserApprovalHandler userApprovalHandler() throws Exception
	{
		TokenServicesUserApprovalHandler handler = new TokenServicesUserApprovalHandler();
		handler.setTokenServices(tokenServices());
		return handler;
	}

	@Override
	@Bean
	public AuthorizationEndpoint authorizationEndpoint() throws Exception
	{
		AuthorizationEndpoint ep = super.authorizationEndpoint();
		ep.setTokenGranter(authorizationTokenGranter());

		return ep;
	}

	@Override
	@Bean
	public TokenEndpoint tokenEndpoint() throws Exception
	{
		TokenEndpoint te = super.tokenEndpoint();
		te.setTokenGranter(authorizationTokenGranter());

		return te;
	}

	@Override
	@Bean
	public AuthorizationCodeTokenGranter authorizationTokenGranter() throws Exception
	{
		// TODO Auto-generated method stub
		return super.authorizationTokenGranter();
	}

	@Override
	protected void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception
	{
		auth.apply(new InMemoryClientDetailsServiceConfigurer())

		.withClient("molgenis")

		.resourceIds(RESOURCE_ID)

		.authorizedGrantTypes("authorization_code", "password")
		// .authorizedGrantTypes("password")

				.authorities("ROLE_CLIENT")

				.scopes("read")

				// .redirectUris("/html/search.html")

				// .accessTokenValiditySeconds(10) // seconds

				// .refreshTokenValiditySeconds(refreshTokenValiditySeconds)

				.secret("secret");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http.authorizeUrls()
				.antMatchers("/oauth/authorize")
				.permitAll()
				.antMatchers("/plugin/dataexplorer/oauth")
				.permitAll()
				.antMatchers("/oauth/token")
				.fullyAuthenticated()

				// .regexMatchers(HttpMethod.DELETE, "/oauth/users/([^/].*?)/tokens/.*")
				// .access("#oauth2.clientHasRole('ROLE_CLIENT') and (hasRole('ROLE_USER') or #oauth2.isClient()) and #oauth2.hasScope('write')")
				// .regexMatchers(HttpMethod.GET, "/oauth/users/.*")
				// .access("#oauth2.clientHasRole('ROLE_CLIENT') and (hasRole('ROLE_USER') or #oauth2.isClient()) and #oauth2.hasScope('read')")
				// .regexMatchers(HttpMethod.GET, "/oauth/clients/.*")
				// .access("#oauth2.clientHasRole('ROLE_CLIENT') and #oauth2.isClient() and #oauth2.hasScope('read')")

				// .antMatchers("/search/**").hasAnyAuthority("ROLE_CLIENT", "ROLE_SU")

				// .antMatchers("/search/**").hasAnyAuthority("ROLE_USER", "SCOPE_READ")

				.regexMatchers("/search")
				.access("#oauth2.isClient() and #oauth2.hasScope('read')")
				// .regexMatchers("/search").hasRole("CLIENT")
				// .access("#oauth2.clientHasRole('ROLE_CLIENT')")

				.and().requestMatchers()
				.antMatchers("/search/**", "/oauth/token", "/oauth/clients/**", "/oauth/users/**")

				.and().apply(new OAuth2ServerConfigurer()).resourceId(RESOURCE_ID);

	}

	public class SparklrUserApprovalHandler extends TokenServicesUserApprovalHandler
	{

		private Collection<String> autoApproveClients = Arrays.asList("molgenis");

		private boolean useTokenServices = true;

		/**
		 * @param useTokenServices
		 *            the useTokenServices to set
		 */
		public void setUseTokenServices(boolean useTokenServices)
		{
			this.useTokenServices = useTokenServices;
		}

		/**
		 * @param autoApproveClients
		 *            the auto approve clients to set
		 */
		public void setAutoApproveClients(Collection<String> autoApproveClients)
		{
			this.autoApproveClients = autoApproveClients;
		}

		@Override
		public AuthorizationRequest updateBeforeApproval(AuthorizationRequest authorizationRequest,
				Authentication userAuthentication)
		{
			return super.updateBeforeApproval(authorizationRequest, userAuthentication);
		}

		/**
		 * Allows automatic approval for a white list of clients in the implicit grant case.
		 * 
		 * @param authorizationRequest
		 *            The authorization request.
		 * @param userAuthentication
		 *            the current user authentication
		 * 
		 * @return Whether the specified request has been approved by the current user.
		 */
		@Override
		public boolean isApproved(AuthorizationRequest authorizationRequest, Authentication userAuthentication)
		{
			return true;
			// If we are allowed to check existing approvals this will short circuit the decision
			/*
			 * if (useTokenServices && super.isApproved(authorizationRequest, userAuthentication)) { return true; }
			 * 
			 * if (!userAuthentication.isAuthenticated()) { return false; }
			 * 
			 * String flag = authorizationRequest.getApprovalParameters().get(AuthorizationRequest.USER_OAUTH_APPROVAL);
			 * boolean approved = flag != null && flag.toLowerCase().equals("true");
			 * 
			 * return approved || (authorizationRequest.getResponseTypes().contains("token") && autoApproveClients
			 * .contains(authorizationRequest.getClientId()));
			 */
		}

	}
}
