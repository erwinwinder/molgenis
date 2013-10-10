package org.molgenis.data.excel;

import java.io.IOException;

import org.molgenis.data.EntitySource;
import org.molgenis.data.EntitySourceFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.io.processor.TrimProcessor;

/**
 * EntitySourceFactory that creates a ExcelReader EntitySource
 * 
 * The url of a ExcelReader EntitySource is the file path prefixed with 'excel://'.
 * 
 * example: excel://Users/john/Documents/matrix.xls
 * 
 */
public class ExcelEntitySourceFactory implements EntitySourceFactory
{
	public static final String EXCEL_ENTITYSOURCE_URL_PREFIX = "excel://";

	@Override
	public String getUrlPrefix()
	{
		return "excel";
	}

	/**
	 * Creates an ExcelReader
	 */
	@Override
	public EntitySource create(String url)
	{
		try
		{
			ExcelEntitySource entitySource = new ExcelEntitySource(url);
			entitySource.addCellProcessor(new TrimProcessor());

			return entitySource;
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Exception creating excel datasource with url [" + url + "]", e);
		}
	}

}
