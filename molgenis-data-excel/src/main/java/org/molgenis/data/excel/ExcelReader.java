package org.molgenis.data.excel;

import static org.molgenis.data.excel.ExcelDataSourceFactory.EXCEL_DATASOURCE_URL_PREFIX;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.data.DataSource;
import org.molgenis.data.Repository;
import org.molgenis.data.excel.ExcelSheetReader.RowIndexEntity;
import org.molgenis.io.processor.CellProcessor;

public class ExcelReader implements DataSource, Closeable
{
	private final Workbook workbook;
	private final InputStream is;
	private final String url;
	private String description;

	/** process cells after reading */
	private List<CellProcessor> cellProcessors;

	public ExcelReader(InputStream is, String url) throws IOException
	{
		if (is == null) throw new IllegalArgumentException("InputStream is null");
		if (url == null) throw new IllegalArgumentException("Name is null");
		this.is = is;
		this.url = url;

		try
		{
			this.workbook = WorkbookFactory.create(is);
		}
		catch (InvalidFormatException e)
		{
			throw new IOException(e);
		}
	}

	public ExcelReader(File file) throws IOException
	{
		this(new FileInputStream(file), EXCEL_DATASOURCE_URL_PREFIX + file.getAbsolutePath());
	}

	public ExcelReader(String url) throws IOException
	{
		this(new File(url.substring(EXCEL_DATASOURCE_URL_PREFIX.length())));
	}

	public int getNumberOfSheets()
	{
		return this.workbook.getNumberOfSheets();
	}

	public String getSheetName(int i)
	{
		return this.workbook.getSheetName(i);
	}

	public ExcelSheetReader getSheet(int i)
	{
		Sheet poiSheet = workbook.getSheetAt(i);
		return poiSheet != null ? new ExcelSheetReader(poiSheet, cellProcessors) : null;
	}

	public ExcelSheetReader getSheet(String sheetName)
	{
		Sheet poiSheet = workbook.getSheet(sheetName);
		return poiSheet != null ? new ExcelSheetReader(poiSheet, cellProcessors) : null;
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public void close() throws IOException
	{
		this.is.close();
	}

	@Override
	public String getUrl()
	{
		return url;
	}

	@Override
	public Iterable<String> getRepositoryNames()
	{
		return getEntityNames();
	}

	@Override
	public Repository<RowIndexEntity> getRepositoryByName(String name)
	{
		Sheet poiSheet = workbook.getSheet(name);
		return poiSheet != null ? new ExcelSheetReader(poiSheet, cellProcessors) : null;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return new Iterable<String>()
		{
			@Override
			public Iterator<String> iterator()
			{
				return new Iterator<String>()
				{
					private final int nrSheets = getNumberOfSheets();
					private int i = 0;

					@Override
					public boolean hasNext()
					{
						return i < nrSheets;
					}

					@Override
					public String next()
					{
						return getSheetName(i++);
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	@Override
	public Repository<RowIndexEntity> getRepositoryByEntityName(String entityName)
	{
		Sheet poiSheet = workbook.getSheet(entityName);
		return poiSheet != null ? new ExcelSheetReader(poiSheet, cellProcessors) : null;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

}
