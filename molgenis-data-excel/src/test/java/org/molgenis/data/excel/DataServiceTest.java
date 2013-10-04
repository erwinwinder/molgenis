package org.molgenis.data.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.molgenis.data.DataService;
import org.molgenis.data.DataServiceImpl;
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

	}

}
