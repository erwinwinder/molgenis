package org.molgenis.data.omx;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.Entity;
import org.molgenis.data.DataSource;
import org.molgenis.data.Repository;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Repository
public class OmxDataSource implements DataSource
{
	@Autowired
	private DataSetRepository dataSetRepository;

	@Override
	public String getUrl()
	{
		return "omx://";
	}

	@Override
	public String getDescription()
	{
		return "Observe omx datasource";
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		List<String> dataSetNames = new ArrayList<String>();
		for (DataSet dataSet : dataSetRepository)
		{
			dataSetNames.add(dataSet.getName());
		}

		return dataSetNames;
	}

	@Override
	public Repository<? extends Entity> getRepositoryByEntityName(String entityName)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
