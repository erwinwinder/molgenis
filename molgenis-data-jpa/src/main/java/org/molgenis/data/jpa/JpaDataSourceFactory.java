package org.molgenis.data.jpa;

import org.molgenis.data.DataSource;
import org.molgenis.data.DataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JpaDataSourceFactory implements DataSourceFactory
{
	@Autowired
	private JpaDataSource jpaDataSource;

	@Override
	public String getUrlPrefix()
	{
		return "jpa";
	}

	@Override
	public DataSource create(String url)
	{
		return jpaDataSource;
	}

}
