package org.molgenis.data;

public interface DataSourceFactory
{
	String getUrlPrefix();

	DataSource create(String url);
}
