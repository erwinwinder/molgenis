package org.molgenis.omx;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.persistence.Query;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisDatabasePopulator;
import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.security.EntityPermission;
import org.molgenis.security.MUser;
import org.molgenis.security.MUserRole;
import org.molgenis.security.MolgenisUserDetailsService;
import org.molgenis.security.RowLevelSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.FileCopyUtils;

public class WebAppDatabasePopulator extends MolgenisDatabasePopulator
{
	private static final Logger logger = Logger.getLogger(WebAppDatabasePopulator.class);
	private static final String USER_ADMIN = "admin";
	private static final String USER_USER = "user";

	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String ROLE_USER = "ROLE_USER";
	private static final String ROLE_READ_DATASET = "ROLE_READ_DATASET";
	private static final String ROLE_READ_RUNTIMEPROPERTY = "ROLE_READ_RUNTIMEPROPERTY";
	private static final String ROLE_WRITE_RUNTIMEPROPERTY = "ROLE_WRITE_RUNTIMEPROPERTY";
	private static final String ROLE_SU = "ROLE_SU";

	@Autowired
	private RowLevelSecurityService rowLevelSecurityService;

	@Autowired
	private MolgenisUserDetailsService userDetailsService;

	@Autowired
	@Qualifier("unauthorizedPrototypeDatabase")
	private Database database;

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

			MUserRole userReadDataSetRole = new MUserRole();
			userReadDataSetRole.setRole(ROLE_READ_DATASET);
			userReadDataSetRole.setMUser(userUser);

			MUserRole userReadRuntimePropertyRole = new MUserRole();
			userReadRuntimePropertyRole.setRole(ROLE_READ_RUNTIMEPROPERTY);
			userReadRuntimePropertyRole.setMUser(userUser);

			MUserRole userWriteRuntimePropertyRole = new MUserRole();
			userWriteRuntimePropertyRole.setRole(ROLE_WRITE_RUNTIMEPROPERTY);
			userWriteRuntimePropertyRole.setMUser(userUser);

			userDetailsService.addUser(userUser, Arrays.asList(userUserRole, userReadDataSetRole,
					userReadRuntimePropertyRole, userWriteRuntimePropertyRole));

			adminUser = new MUser();
			adminUser.setUsername(USER_ADMIN);
			adminUser.setPassword(adminPassword);

			MUserRole adminAdminRole = new MUserRole();
			adminAdminRole.setRole(ROLE_ADMIN);
			adminAdminRole.setMUser(adminUser);

			MUserRole adminUserRole = new MUserRole();
			adminUserRole.setRole(ROLE_USER);
			adminUserRole.setMUser(adminUser);

			MUserRole adminSuRole = new MUserRole();
			adminSuRole.setRole(ROLE_SU);
			adminSuRole.setMUser(adminUser);

			userDetailsService.addUser(adminUser, Arrays.asList(adminAdminRole, adminUserRole, adminSuRole));

			InputStream in = this.getClass().getClassLoader().getResourceAsStream("acl_structure.sql");
			database.beginTx();
			try
			{
				String sql = FileCopyUtils.copyToString(new InputStreamReader(in));
				for (String statement : sql.split(";"))
				{
					Query query = database.getEntityManager().createNativeQuery(statement);
					query.executeUpdate();
				}
				database.commitTx();
			}
			catch (Exception e)
			{
				logger.error("Error creating acl schema", e);
				database.rollbackTx();
			}
			finally
			{
				IOUtils.closeQuietly(in);
			}

			// Login admin to be able to grant row level security
			UserDetails user = userDetailsService.loadUserByUsername(USER_ADMIN);
			Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);

			Protocol protocol = new Protocol();
			protocol.setIdentifier("ACLProtocol");
			protocol.setName("ACLProtocol");
			database.add(protocol);
			// protocol = Protocol.findByIdentifier(database, protocol.getIdentifier());

			rowLevelSecurityService.addPermission(USER_ADMIN, protocol, EntityPermission.OWNS);

			DataSet dataSet = new DataSet();
			dataSet.setIdentifier("ACLDataSet");
			dataSet.setName("ACLDataSet");
			dataSet.setProtocolUsed(protocol);
			database.add(dataSet);
			// dataSet = DataSet.findByIdentifier(database, dataSet.getIdentifier());

			rowLevelSecurityService.addPermission(USER_ADMIN, dataSet, EntityPermission.READ);
		}
	}
}