package org.molgenis.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class MolgenisUserDetailsService implements UserDetailsService
{
	@Autowired
	@Qualifier("unauthorizedPrototypeDatabase")
	private Database database;

	private final PasswordEncoder passwordEncoder;

	public MolgenisUserDetailsService(PasswordEncoder passwordEncoder)
	{
		if (passwordEncoder == null) throw new IllegalArgumentException();
		this.passwordEncoder = passwordEncoder;
	}

	public void addUser(MUser user, Set<MUserRole> roles) throws DatabaseException
	{
		database.beginTx();
		try
		{
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			database.add(user);
			database.add(new ArrayList<MUserRole>(roles));
			database.commitTx();
		}
		catch (DatabaseException e)
		{
			database.rollbackTx();
			throw e;
		}
		catch (RuntimeException e)
		{
			database.rollbackTx();
			throw e;
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		try
		{
			MUser user = MUser.findByUsername(database, username);
			if (user == null) throw new UsernameNotFoundException("unknown user '" + username + "'");

			List<? extends MUserRole> roles = MUserRole.find(database, new QueryRule(MUserRole.MUSER, Operator.EQUALS,
					user));

			return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
					roles != null ? Lists.transform(roles, new Function<MUserRole, SimpleGrantedAuthority>()
					{
						@Override
						public SimpleGrantedAuthority apply(MUserRole role)
						{
							return new SimpleGrantedAuthority(role.getRole());
						}
					}) : null);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}
}
