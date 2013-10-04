package org.molgenis.data;

import org.molgenis.Entity;

public interface DataSource
{
	/**
	 * The url of this DataSource. Concrete subclasses know how to deal with this (like a driver). Every DataSource has
	 * a unique url
	 * 
	 * example: excel://Users/piet/Documents/matrix.xls
	 */
	String getUrl();

	String getDescription();

	Iterable<String> getEntityNames();

	Repository<? extends Entity> getRepositoryByEntityName(String entityName);
}
