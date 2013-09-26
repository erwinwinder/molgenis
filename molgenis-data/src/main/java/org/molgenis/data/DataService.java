package org.molgenis.data;

import org.molgenis.Entity;

public interface DataService
{
	void registerFactory(DataSourceFactory dataSourceFactory);

	void registerDataSource(String url);

	Iterable<String> getDataSourceUrls();

	DataSource getDataSource(String url);

	Iterable<String> getEntityNames();

	Repository<? extends Entity> getRepositoryByEntityName(String entityName);
}
