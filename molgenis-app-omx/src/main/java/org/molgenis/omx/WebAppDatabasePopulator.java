package org.molgenis.omx;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.molgenis.MolgenisDatabasePopulator;
import org.molgenis.framework.db.Database;
import org.molgenis.security.MUser;
import org.molgenis.security.MUserRole;
import org.molgenis.security.MolgenisUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class WebAppDatabasePopulator extends MolgenisDatabasePopulator
{
	private static final String USER_ADMIN = "admin";
	private static final String USER_USER = "user";

	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String ROLE_USER = "ROLE_USER";

	@Autowired
	private MolgenisUserDetailsService userDetailsService;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${user.password:user}")
	private String userPassword;

	@Override
	protected void initializeApplicationDatabase(Database database) throws Exception
	{
		MUser adminUser = MUser.findByUsername(database, USER_ADMIN);
		if (adminUser == null)
		{
			MUser userUser = new MUser();
			userUser.setUsername(USER_USER);
			userUser.setPassword(userPassword);

			MUserRole userUserRole = new MUserRole();
			userUserRole.setRole(ROLE_USER);
			userUserRole.setMUser(userUser);

			userDetailsService.addUser(userUser, Collections.singleton(userUserRole));

			adminUser = new MUser();
			adminUser.setUsername(USER_ADMIN);
			adminUser.setPassword(adminPassword);

			MUserRole adminAdminRole = new MUserRole();
			adminAdminRole.setRole(ROLE_ADMIN);
			adminAdminRole.setMUser(adminUser);

			MUserRole adminUserRole = new MUserRole();
			adminUserRole.setRole(ROLE_USER);
			adminUserRole.setMUser(adminUser);

			userDetailsService.addUser(adminUser, new HashSet<MUserRole>(Arrays.asList(adminAdminRole, adminUserRole)));
		}
	}
}