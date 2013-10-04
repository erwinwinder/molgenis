package org.molgenis.data.omx;

import org.molgenis.data.DataSource;
import org.molgenis.data.DataSourceFactory;
import org.molgenis.data.MolgenisDataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OmxDataSourceFactory implements DataSourceFactory
{
	@Autowired
	private OmxDataSource omxDataSource;

	@Override
	public String getUrlPrefix()
	{
		return "omx";
	}

	@Override
	public DataSource create(String url)
	{
		if (!url.startsWith(getUrlPrefix()))
		{
			throw new MolgenisDataException("No omx datasource [" + url + "]");
		}

		return omxDataSource;
	}
}
