package org.molgenis.data.excel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;

import org.molgenis.Entity;
import org.molgenis.io.processor.CellProcessor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExcelReaderTest
{
	private ExcelReader excelReader;

	@BeforeMethod
	public void setUp() throws IOException
	{
		excelReader = new ExcelReader(this.getClass().getResourceAsStream("/test.xls"), "test");
	}

	@AfterMethod
	public void tearDown() throws IOException
	{
		excelReader.close();
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ExcelReader() throws IOException
	{
		new ExcelReader((InputStream) null, "test");
	}

	@Test
	public void addCellProcessor_header() throws IOException
	{
		CellProcessor processor = mock(CellProcessor.class);
		when(processor.processHeader()).thenReturn(true);
		when(processor.process("col1")).thenReturn("col1");
		when(processor.process("col2")).thenReturn("col2");

		excelReader.addCellProcessor(processor);

		for (String sheetName : excelReader.getRepositoryNames())
		{
			for (@SuppressWarnings("unused")
			Entity entity : excelReader.getRepositoryByName(sheetName))
			{
			}
		}

		verify(processor).process("col1");
		verify(processor).process("col2");
	}

	@Test
	public void addCellProcessor_data() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
		excelReader.addCellProcessor(processor);

		for (String sheetName : excelReader.getRepositoryNames())
		{
			for (Entity entity : excelReader.getRepositoryByName(sheetName))
			{
				entity.get("col2");
			}
		}

		verify(processor).process("val2");
		verify(processor).process("val4");
		verify(processor).process("val6");
	}

	@Test
	public void getNumberOfSheets() throws IOException
	{
		assertEquals(excelReader.getNumberOfSheets(), 2);
	}

	@Test
	public void getSheetint() throws IOException
	{
		assertNotNull(excelReader.getSheet(0));
		assertNotNull(excelReader.getSheet(1));
	}

	@Test
	public void getSheetString() throws IOException
	{
		assertNotNull(excelReader.getSheet("test"));
		assertNotNull(excelReader.getSheet("Blad2"));
		assertNull(excelReader.getSheet("doesnotexist"));
	}

}
