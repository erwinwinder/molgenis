package org.molgenis.data.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.DataSource;
import org.molgenis.data.DataSourceFactory;
import org.molgenis.data.MolgenisDataException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class JpaDataSourceFactory implements DataSourceFactory, ApplicationContextAware
{
	private final Map<String, DataSource> jpaDataSourceMap = new HashMap<String, DataSource>();

	public Set<String> getUrls()
	{
		return jpaDataSourceMap.keySet();
	}

	@Override
	public String getUrlPrefix()
	{
		return "jpa";
	}

	@Override
	public DataSource create(String url)
	{
		DataSource dataSource = jpaDataSourceMap.get(url);
		if (dataSource == null)
		{
			throw new MolgenisDataException("Unknown jpa DataSource [" + url + "]");
		}

		return dataSource;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		// TODO is this method called after complete initialization of application context?
		// When not the JpaDataSource might not have already bean added to the context when this method is called
		Map<String, JpaDataSource> jpaDataSources = applicationContext.getBeansOfType(JpaDataSource.class);

		for (JpaDataSource jpaDataSource : jpaDataSources.values())
		{
			jpaDataSourceMap.put(jpaDataSource.getUrl(), jpaDataSource);
		}

	}

}
