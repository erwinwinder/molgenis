package org.molgenis.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils
{
	public static final GrantedAuthority GRANTED_AUTHORITY_SU = new SimpleGrantedAuthority("ROLE_SU");

	public static boolean isUserInRole(String role)
	{
		if (currentUserIsSu())
		{
			return true;
		}

		return getCurrentUser().getAuthorities().contains(new SimpleGrantedAuthority(role));
	}

	public static UserDetails getCurrentUser()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null)
		{
			return null;
		}

		return (UserDetails) authentication.getPrincipal();
	}

	public static boolean currentUserIsSu()
	{
		UserDetails user = getCurrentUser();
		if (user == null)
		{
			throw new IllegalStateException("No current user logged in");
		}

		return user.getAuthorities().contains(GRANTED_AUTHORITY_SU);
	}
}
