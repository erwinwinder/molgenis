package org.molgenis.data.excel;

import java.io.IOException;

import org.molgenis.data.DataSource;
import org.molgenis.data.DataSourceFactory;
import org.molgenis.data.MolgenisDataException;

public class ExcelDataSourceFactory implements DataSourceFactory
{
	public static final String EXCEL_DATASOURCE_URL_PREFIX = "excel://";

	@Override
	public String getUrlPrefix()
	{
		return "excel";
	}

	@Override
	public DataSource create(String url)
	{
		try
		{
			return new ExcelReader(url);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Exception creating excel datasource with url [" + url + "]", e);
		}
	}

}
