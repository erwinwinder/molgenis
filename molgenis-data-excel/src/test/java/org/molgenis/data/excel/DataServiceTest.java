package org.molgenis.data.excel;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.molgenis.AttributeMetaData;
import org.molgenis.Entity;
import org.molgenis.data.DataService;
import org.molgenis.data.DataServiceImpl;
import org.molgenis.data.DataSource;
import org.molgenis.data.Repository;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataServiceTest
{
	private DataService dataService;
	private File xlsFile;
	private String url;

	@BeforeMethod
	public void setUp() throws IOException
	{
		dataService = new DataServiceImpl();
		InputStream in = getClass().getResourceAsStream("/test.xls");
		xlsFile = File.createTempFile("molgenis", ".xls");
		FileCopyUtils.copy(in, new FileOutputStream(xlsFile));
		url = "excel://" + xlsFile.getAbsolutePath();

		dataService.registerFactory(new ExcelDataSourceFactory());
		dataService.registerDataSource(url);
	}

	@AfterMethod
	public void tearDown() throws IOException
	{
		xlsFile.delete();
	}

	@Test
	public void print()
	{
		for (String url : dataService.getDataSourceUrls())
		{
			DataSource dataSource = dataService.getDataSource(url);
			System.out.println("url:" + url + ", DataSource:" + dataSource);

			for (String repoName : dataSource.getRepositoryNames())
			{
				Repository<? extends Entity> repo = dataSource.getRepositoryByName(repoName);
				System.out.println("RepoName:" + repoName + ", Repo:" + repo);
				for (AttributeMetaData attribute : repo.getEntityMetaData().getAttributes())
				{
					System.out.println("Attribute: " + attribute.getName());
				}

				for (Entity entity : repo)
				{
					for (AttributeMetaData attribute : entity.getMetaData().getAttributes())
					{
						System.out.println("Value: " + attribute.getName() + "=" + entity.get(attribute.getName()));
					}
				}
			}
		}
	}

	@Test
	public void getRepository()
	{
		Repository<? extends Entity> repo = dataService.getRepositoryByEntityName("test");
		assertEquals(repo.getName(), "test");

		repo = dataService.getRepositoryByEntityName("Blad2");
		assertEquals(repo.getName(), "Blad2");
	}
}
