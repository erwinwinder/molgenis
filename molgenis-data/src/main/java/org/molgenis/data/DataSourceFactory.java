package org.molgenis.data;

/**
 * Creates a DataSource based on an url
 * 
 */
public interface DataSourceFactory
{
	/**
	 * Returns the url prefix of the DataSource For example for excel DataSources the prefix is 'excel' The urls are
	 * like excel://Users/john/Documents/matrix.xls
	 */
	String getUrlPrefix();

	/**
	 * Creates a new DataSource
	 */
	DataSource create(String url);
}
