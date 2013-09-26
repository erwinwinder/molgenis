package org.molgenis.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.molgenis.Entity;

import com.google.common.collect.Iterables;

public class DataServiceImpl implements DataService
{
	private final Map<String, DataSourceFactory> dataSourceFactories = new HashMap<String, DataSourceFactory>();
	private final Map<String, Iterable<String>> entityNamesByDataSourceUrl = new HashMap<String, Iterable<String>>();

	@Override
	public void registerFactory(DataSourceFactory dataSourceFactory)
	{
		dataSourceFactories.put(dataSourceFactory.getUrlPrefix(), dataSourceFactory);
	}

	@Override
	public Iterable<String> getDataSourceUrls()
	{
		return entityNamesByDataSourceUrl.keySet();
	}

	@Override
	public DataSource getDataSource(String url)
	{
		int index = url.indexOf("://");
		if (index == -1)
		{
			throw new MolgenisDataException("Incorrect url format should be of format prefix://");
		}

		String prefix = url.substring(0, index);
		DataSourceFactory dataSourceFactory = dataSourceFactories.get(prefix);
		if (dataSourceFactory == null)
		{
			throw new MolgenisDataException("Unknown datasource driver [" + prefix + "]");
		}

		return dataSourceFactory.create(url);
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		Iterable<String> entityNames = new ArrayList<String>();

		for (Iterable<String> dataSourceEntityNames : entityNamesByDataSourceUrl.values())
		{
			entityNames = Iterables.concat(entityNames, dataSourceEntityNames);
		}

		return entityNames;
	}

	@Override
	public Repository<? extends Entity> getRepositoryByEntityName(String entityName)
	{
		for (String url : entityNamesByDataSourceUrl.keySet())
		{
			Iterable<String> entityNames = entityNamesByDataSourceUrl.get(url);
			if (Iterables.contains(entityNames, entityName))
			{
				DataSource dataSource = getDataSource(url);
				return dataSource.getRepositoryByEntityName(entityName);
			}
		}

		throw new MolgenisDataException("No repository found for entity [" + entityName + "]");
	}

	@Override
	public void registerDataSource(String url)
	{
		DataSource dataSource = getDataSource(url);
		Iterable<String> entityNames = dataSource.getEntityNames();
		entityNamesByDataSourceUrl.put(url, entityNames);
	}
}
