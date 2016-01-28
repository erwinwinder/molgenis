package org.molgenis.data.mysql;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import javax.sql.DataSource;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.model.MolgenisModelException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MySqlUpdateAttributeMetaDataTest
{
	private AsyncJdbcTemplate jdbcTemplate;
	private MysqlRepository repo;

	@BeforeMethod
	public void setUp()
	{
		jdbcTemplate = mock(AsyncJdbcTemplate.class);
		repo = new MysqlRepository(mock(DataService.class), mock(MySqlEntityFactory.class), mock(DataSource.class),
				jdbcTemplate);
	}

	@Test
	public void testUpdateAttributeSqlNillableNotUnique() throws MolgenisModelException, IOException
	{
		try
		{
			DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("TestEntity");
			entityMeta.addAttribute("id").setIdAttribute(true).setNillable(false);
			entityMeta.addAttribute("test").setDataType(MolgenisFieldTypes.INT).setNillable(false).setUnique(true);
			repo.setMetaData(entityMeta);

			AttributeMetaData attr = new DefaultAttributeMetaData("test").setNillable(true).setUnique(false);
			repo.updateAttributeMetaData(attr);

			verify(jdbcTemplate).execute("ALTER TABLE `TestEntity` DROP INDEX `test_unique`");
			verify(jdbcTemplate).execute("ALTER TABLE `TestEntity` MODIFY COLUMN `test` TEXT;");
		}
		finally
		{
			repo.close();
		}
	}

	@Test
	public void testUpdateAttributeSqlNotNillable() throws MolgenisModelException, IOException
	{
		try
		{
			DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("TestEntity");
			entityMeta.addAttribute("id").setIdAttribute(true).setNillable(false);
			entityMeta.addAttribute("test").setDataType(MolgenisFieldTypes.INT).setNillable(false).setUnique(true);
			repo.setMetaData(entityMeta);

			AttributeMetaData attr = new DefaultAttributeMetaData("test").setNillable(false).setUnique(true);
			repo.updateAttributeMetaData(attr);

			verify(jdbcTemplate).execute("ALTER TABLE `TestEntity` MODIFY COLUMN `test` VARCHAR(255) NOT NULL;");
			verifyNoMoreInteractions(jdbcTemplate);
		}
		finally
		{
			repo.close();
		}
	}
}
