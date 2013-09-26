package org.molgenis.data.excel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.AttributeMetaData;
import org.molgenis.Entity;
import org.molgenis.data.excel.ExcelSheetReader.RowIndexEntity;
import org.molgenis.io.processor.CellProcessor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExcelSheetReaderTest
{
	private ExcelReader excelReader;
	private ExcelSheetReader excelSheetReader;

	@BeforeMethod
	public void setUp() throws IOException
	{
		excelReader = new ExcelReader(this.getClass().getResourceAsStream("/test.xls"), "test");
		excelSheetReader = excelReader.getSheet("test");
	}

	@AfterMethod
	public void tearDown() throws IOException
	{
		excelReader.close();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ExcelSheetReader()
	{
		new ExcelSheetReader(null, null);
	}

	@Test
	public void addCellProcessor_header()
	{
		CellProcessor processor = mock(CellProcessor.class);
		when(processor.processHeader()).thenReturn(true);
		when(processor.process("col1")).thenReturn("col1");
		when(processor.process("col2")).thenReturn("col2");

		excelSheetReader.addCellProcessor(processor);
		for (@SuppressWarnings("unused")
		Entity entity : excelSheetReader)
		{
		}
		verify(processor).process("col1");
		verify(processor).process("col2");
	}

	@Test
	public void addCellProcessor_data()
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
		excelSheetReader.addCellProcessor(processor);
		for (Entity entity : excelSheetReader)
			entity.get("col2");

		verify(processor).process("val2");
		verify(processor).process("val4");
		verify(processor).process("val6");
	}

	@Test
	public void colNames() throws IOException
	{
		Iterator<AttributeMetaData> it = excelSheetReader.getEntityMetaData().getAttributes().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col1");
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col2");
	}

	@Test
	public void getName()
	{
		assertEquals(excelSheetReader.getName(), "test");
	}

	@Test
	public void getNrRows()
	{
		assertEquals(excelSheetReader.getNrRows(), 5);
	}

	@Test
	public void iterator()
	{
		Iterator<RowIndexEntity> it = excelSheetReader.iterator();
		assertTrue(it.hasNext());

		Entity row1 = it.next();
		assertEquals(row1.get("col1"), "val1");
		assertEquals(row1.get("col2"), "val2");
		assertTrue(it.hasNext());

		Entity row2 = it.next();
		assertEquals(row2.get("col1"), "val3");
		assertEquals(row2.get("col2"), "val4");
		assertTrue(it.hasNext());

		Entity row3 = it.next();
		assertEquals(row3.get("col1"), "XXX");
		assertEquals(row3.get("col2"), "val6");
		assertTrue(it.hasNext());

		// test number cell (col1) and formula cell (col2)
		Entity row4 = it.next();
		assertEquals(row4.get("col1"), "1.2");
		assertEquals(row4.get("col2"), "2.4");
		assertFalse(it.hasNext());
	}

	@Test
	public void colNamesAndIterator() throws IOException
	{
		Iterator<AttributeMetaData> headerIt = excelSheetReader.getEntityMetaData().getAttributes().iterator();
		assertTrue(headerIt.hasNext());
		assertEquals(headerIt.next().getName(), "col1");
		assertTrue(headerIt.hasNext());
		assertEquals(headerIt.next().getName(), "col2");

		Iterator<RowIndexEntity> it = excelSheetReader.iterator();
		assertTrue(it.hasNext());

		Entity row1 = it.next();
		assertEquals(row1.get("col1"), "val1");
		assertEquals(row1.get("col2"), "val2");
		assertTrue(it.hasNext());

		Entity row2 = it.next();
		assertEquals(row2.get("col1"), "val3");
		assertEquals(row2.get("col2"), "val4");
		assertTrue(it.hasNext());

		Entity row3 = it.next();
		assertEquals(row3.get("col1"), "XXX");
		assertEquals(row3.get("col2"), "val6");
		assertTrue(it.hasNext());

		// test number cell (col1) and formula cell (col2)
		Entity row4 = it.next();
		assertEquals(row4.get("col1"), "1.2");
		assertEquals(row4.get("col2"), "2.4");
		assertFalse(it.hasNext());
	}
}
