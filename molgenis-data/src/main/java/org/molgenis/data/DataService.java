package org.molgenis.data;

import org.molgenis.Entity;

public interface DataService
{
	/**
	 * Register a new DataSourceFactory of a DataSource implementation
	 */
	void registerFactory(DataSourceFactory dataSourceFactory);

	/**
	 * Register a new DataSource
	 */
	void registerDataSource(String url);

	Iterable<String> getDataSourceUrls();

	DataSource getDataSource(String url);

	Iterable<String> getEntityNames();

	Repository<? extends Entity> getRepositoryByEntityName(String entityName);
}
